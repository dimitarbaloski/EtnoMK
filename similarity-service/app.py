from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image, ImageEnhance, ImageFilter, ImageOps
import io
import logging
import numpy as np
import torch
import torchvision.models as models
import torchvision.transforms as transforms
from math import ceil

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("etnomk-similarity")

app = FastAPI(title="EtnoMK Similarity Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

logger.info("Loading ResNet50 similarity model")
model = models.resnet50(weights=models.ResNet50_Weights.IMAGENET1K_V1)
model = torch.nn.Sequential(*list(model.children())[:-1])
model.eval()
logger.info("Similarity model loaded")

transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225]),
])

EMBEDDING_DIM = 2048
FULL_IMAGE_WEIGHT = 0.45
CROP_WEIGHT = 0.45
COLOR_WEIGHT = 0.10
LOW_RES_MIN_SIDE = 320


def l2_normalize(vector: np.ndarray) -> np.ndarray:
    norm = np.linalg.norm(vector)
    if norm == 0:
        return vector
    return vector / norm


def encode_patch(image: Image.Image) -> np.ndarray:
    tensor = transform(image).unsqueeze(0)
    with torch.no_grad():
        embedding = model(tensor).squeeze().cpu().numpy().astype(np.float32)
    return l2_normalize(embedding)


def build_patch_views(image: Image.Image) -> list[Image.Image]:
    width, height = image.size
    min_side = min(width, height)
    crop_size = max(int(min_side * 0.72), 64)

    if crop_size >= min_side:
        return [image]

    center_x = max((width - crop_size) // 2, 0)
    center_y = max((height - crop_size) // 2, 0)

    positions = [
        (0, 0),
        (width - crop_size, 0),
        (0, height - crop_size),
        (width - crop_size, height - crop_size),
        (center_x, center_y),
    ]

    views = []
    seen = set()
    for x, y in positions:
        left = int(max(x, 0))
        top = int(max(y, 0))
        right = int(min(left + crop_size, width))
        bottom = int(min(top + crop_size, height))
        box = (left, top, right, bottom)
        if box in seen:
            continue
        seen.add(box)
        views.append(image.crop(box))
    return views


def extract_color_signature(image: Image.Image) -> np.ndarray:
    hsv = np.asarray(image.convert("HSV").resize((128, 128)), dtype=np.float32)
    hue = hsv[:, :, 0] / 255.0
    sat = hsv[:, :, 1] / 255.0
    val = hsv[:, :, 2] / 255.0

    histogram, _ = np.histogramdd(
        sample=np.stack([hue.ravel(), sat.ravel(), val.ravel()], axis=1),
        bins=(8, 4, 4),
        range=((0.0, 1.0), (0.0, 1.0), (0.0, 1.0)),
    )

    signature = histogram.flatten().astype(np.float32)
    signature = l2_normalize(signature)

    repeats = int(np.ceil(EMBEDDING_DIM / signature.size))
    projected = np.tile(signature, repeats)[:EMBEDDING_DIM]
    return l2_normalize(projected.astype(np.float32))


def upscale_for_low_resolution(image: Image.Image) -> Image.Image:
    width, height = image.size
    min_side = min(width, height)
    if min_side >= LOW_RES_MIN_SIDE:
        return image

    scale = ceil(LOW_RES_MIN_SIDE / max(min_side, 1))
    resized = image.resize(
        (max(width * scale, LOW_RES_MIN_SIDE), max(height * scale, LOW_RES_MIN_SIDE)),
        Image.Resampling.LANCZOS,
    )
    enhanced = ImageOps.autocontrast(resized)
    enhanced = ImageEnhance.Contrast(enhanced).enhance(1.08)
    enhanced = enhanced.filter(ImageFilter.UnsharpMask(radius=1.6, percent=180, threshold=3))
    return enhanced


def build_image_variants(image: Image.Image) -> list[Image.Image]:
    base = ImageOps.exif_transpose(image).convert("RGB")
    variants = [base]

    low_res_variant = upscale_for_low_resolution(base)
    if low_res_variant.size != base.size:
        variants.append(low_res_variant)

    contrast_variant = ImageOps.autocontrast(base)
    contrast_variant = ImageEnhance.Sharpness(contrast_variant).enhance(1.15)
    variants.append(contrast_variant)

    return variants


def extract_embedding(image_bytes: bytes) -> list[float]:
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    variants = build_image_variants(image)

    variant_embeddings = []
    for variant in variants:
        global_embedding = encode_patch(variant)

        patch_views = build_patch_views(variant)
        patch_embeddings = [encode_patch(view) for view in patch_views]
        patch_embedding = l2_normalize(np.mean(patch_embeddings, axis=0))

        color_embedding = extract_color_signature(variant)

        combined = (
            FULL_IMAGE_WEIGHT * global_embedding
            + CROP_WEIGHT * patch_embedding
            + COLOR_WEIGHT * color_embedding
        )
        variant_embeddings.append(l2_normalize(combined.astype(np.float32)))

    final_embedding = l2_normalize(np.mean(variant_embeddings, axis=0).astype(np.float32))
    return final_embedding.tolist()


@app.get("/health")
def health():
    return {"status": "ok", "modelLoaded": True}


@app.post("/embed")
async def embed(image: UploadFile = File(...)):
    if not image.content_type or not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")
    image_bytes = await image.read()
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Empty image file")
    try:
        embedding = extract_embedding(image_bytes)
    except Exception as e:
        logger.exception("Embedding generation failed")
        raise HTTPException(status_code=500, detail=f"Embedding failed: {str(e)}")
    return {"embedding": embedding, "dimensions": len(embedding)}
