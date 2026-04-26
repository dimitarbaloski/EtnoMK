from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import numpy as np
from PIL import Image
import torch
import torchvision.models as models
import torchvision.transforms as transforms
import io

app = FastAPI(title="EtnoMK Similarity Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load pre-trained ResNet50, remove the final classification layer → 2048-dim vector
model = models.resnet50(weights=models.ResNet50_Weights.IMAGENET1K_V1)
model = torch.nn.Sequential(*list(model.children())[:-1])
model.eval()

transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225]),
])


def extract_embedding(image_bytes: bytes) -> list[float]:
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    tensor = transform(image).unsqueeze(0)
    with torch.no_grad():
        embedding = model(tensor).squeeze().numpy()
    return embedding.tolist()


@app.get("/health")
def health():
    return {"status": "ok"}


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
        raise HTTPException(status_code=500, detail=f"Embedding failed: {str(e)}")
    return {"embedding": embedding, "dimensions": len(embedding)}