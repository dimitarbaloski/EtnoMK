import io
import logging

import numpy as np
import torch
import torch.nn.functional as F
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image, ImageEnhance, ImageFilter, ImageOps
from transformers import AutoImageProcessor, AutoModel

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("etnomk-dinov2")

app = FastAPI(title="EtnoMK Similarity Service")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

DEVICE = "cpu"
LOW_RES_MIN_SIDE = 320
PATCH_SIZE = 256
PATCH_STRIDE = 128
MAX_PATCHES = 49

logger.info("Loading DINOv2 model...")
processor = AutoImageProcessor.from_pretrained("facebook/dinov2-base")
model = AutoModel.from_pretrained("facebook/dinov2-base")
model.eval()
model.to(DEVICE)
logger.info("DINOv2 loaded successfully")


def l2_normalize(x: np.ndarray) -> np.ndarray:
    norm = np.linalg.norm(x)
    if norm == 0:
        return x
    return x / norm


def upscale_for_low_resolution(image: Image.Image) -> Image.Image:
    width, height = image.size
    min_side = min(width, height)

    if min_side >= LOW_RES_MIN_SIDE:
        return image

    scale = LOW_RES_MIN_SIDE / min_side
    resized = image.resize(
        (int(width * scale), int(height * scale)),
        Image.Resampling.LANCZOS,
    )
    enhanced = ImageOps.autocontrast(resized)
    enhanced = ImageEnhance.Contrast(enhanced).enhance(1.08)
    return enhanced.filter(
        ImageFilter.UnsharpMask(radius=1.6, percent=180, threshold=3)
    )


def build_variants(image: Image.Image) -> list[Image.Image]:
    base = ImageOps.exif_transpose(image).convert("RGB")
    variants = [base]

    enhanced = upscale_for_low_resolution(base)
    if enhanced.size != base.size:
        variants.append(enhanced)

    contrast = ImageOps.autocontrast(base)
    variants.append(ImageEnhance.Sharpness(contrast).enhance(1.15))
    return variants


def build_multi_scale_views(image: Image.Image) -> list[Image.Image]:
    width, height = image.size
    views = [image]

    for scale in (0.9, 0.7, 0.5):
        crop_w = int(width * scale)
        crop_h = int(height * scale)
        positions = [
            (0, 0),
            (width - crop_w, 0),
            (0, height - crop_h),
            (width - crop_w, height - crop_h),
            ((width - crop_w) // 2, (height - crop_h) // 2),
        ]

        for x, y in positions:
            x = max(x, 0)
            y = max(y, 0)
            views.append(image.crop((x, y, x + crop_w, y + crop_h)))

    return views


def extract_patch_embedding(image: Image.Image) -> np.ndarray:
    inputs = processor(images=image, return_tensors="pt").to(DEVICE)

    with torch.no_grad():
        outputs = model(**inputs)
        patch_tokens = outputs.last_hidden_state[:, 1:, :]
        mean_pool = patch_tokens.mean(dim=1)
        max_pool = patch_tokens.max(dim=1).values
        embedding = torch.cat([mean_pool, max_pool], dim=1)
        embedding = F.normalize(embedding, p=2, dim=1)

    return embedding.squeeze().cpu().numpy().astype(np.float32)


def extract_fft_features(image: Image.Image) -> np.ndarray:
    gray = image.convert("L").resize((128, 128))
    arr = np.asarray(gray).astype(np.float32)
    fft = np.fft.fft2(arr)
    fft_shift = np.fft.fftshift(fft)
    magnitude = np.abs(fft_shift).flatten()[:512].astype(np.float32)
    return l2_normalize(magnitude)


def combine_visual_and_texture_features(image: Image.Image, patch_embedding: np.ndarray) -> np.ndarray:
    fft_features = extract_fft_features(image)
    fft_padded = np.pad(fft_features, (0, len(patch_embedding) - len(fft_features)))
    return l2_normalize((0.90 * patch_embedding) + (0.10 * fft_padded))


def extract_pattern_embedding(image: Image.Image) -> np.ndarray:
    patch_embedding = extract_patch_embedding(image)
    return combine_visual_and_texture_features(image, patch_embedding)


def build_pattern_patches(image: Image.Image) -> list[dict]:
    base = ImageOps.exif_transpose(image).convert("RGB")
    base = upscale_for_low_resolution(base)
    width, height = base.size

    if width <= PATCH_SIZE or height <= PATCH_SIZE:
        return [{
            "image": base,
            "x": 0,
            "y": 0,
            "width": width,
            "height": height,
        }]

    xs = list(range(0, max(width - PATCH_SIZE, 0) + 1, PATCH_STRIDE))
    ys = list(range(0, max(height - PATCH_SIZE, 0) + 1, PATCH_STRIDE))

    if xs[-1] != width - PATCH_SIZE:
        xs.append(width - PATCH_SIZE)
    if ys[-1] != height - PATCH_SIZE:
        ys.append(height - PATCH_SIZE)

    patches = []
    for y in ys:
        for x in xs:
            patches.append({
                "image": base.crop((x, y, x + PATCH_SIZE, y + PATCH_SIZE)),
                "x": x,
                "y": y,
                "width": PATCH_SIZE,
                "height": PATCH_SIZE,
            })

    if len(patches) <= MAX_PATCHES:
        return patches

    step = len(patches) / MAX_PATCHES
    return [patches[int(i * step)] for i in range(MAX_PATCHES)]


def extract_embedding(image_bytes: bytes) -> list[float]:
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    embeddings = []

    for variant in build_variants(image):
        patch_embeddings = [
            extract_patch_embedding(view)
            for view in build_multi_scale_views(variant)
        ]
        patch_embedding = l2_normalize(np.mean(patch_embeddings, axis=0))
        embeddings.append(combine_visual_and_texture_features(variant, patch_embedding))

    final_embedding = l2_normalize(np.mean(embeddings, axis=0))
    return final_embedding.tolist()


def extract_patch_embeddings(image_bytes: bytes) -> list[dict]:
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    patches = []

    for patch in build_pattern_patches(image):
        embedding = extract_pattern_embedding(patch["image"])
        patches.append({
            "x": patch["x"],
            "y": patch["y"],
            "width": patch["width"],
            "height": patch["height"],
            "embedding": embedding.tolist(),
        })

    return patches


async def read_image_upload(image: UploadFile) -> bytes:
    if not image.content_type or not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    image_bytes = await image.read()
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Empty image")

    return image_bytes


@app.get("/health")
def health():
    return {
        "status": "ok",
        "model": "DINOv2-base",
        "patch_tokens": True,
    }


@app.post("/embed")
async def embed(image: UploadFile = File(...)):
    image_bytes = await read_image_upload(image)

    try:
        embedding = extract_embedding(image_bytes)
    except Exception as e:
        logger.exception("Embedding extraction failed")
        raise HTTPException(status_code=500, detail=str(e)) from e

    return {
        "embedding": embedding,
        "dimensions": len(embedding),
    }


@app.post("/embed-patches")
async def embed_patches(image: UploadFile = File(...)):
    image_bytes = await read_image_upload(image)

    try:
        patches = extract_patch_embeddings(image_bytes)
    except Exception as e:
        logger.exception("Patch embedding extraction failed")
        raise HTTPException(status_code=500, detail=str(e)) from e

    return {
        "patches": patches,
        "dimensions": len(patches[0]["embedding"]) if patches else 0,
    }
