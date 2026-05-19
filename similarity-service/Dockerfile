FROM python:3.11-slim

WORKDIR /app

# Prevent python buffering (useful for logs)
ENV PYTHONUNBUFFERED=1

# Upgrade pip early (important for modern torch wheels)
RUN pip install --upgrade pip

# Install CPU-only PyTorch FIRST (prevents CUDA resolution issues)
RUN pip install --no-cache-dir \
    torch==2.2.2+cpu \
    torchvision==0.17.2+cpu \
    --index-url https://download.pytorch.org/whl/cpu

# Copy requirements AFTER torch (so pip doesn't try to re-resolve torch)
COPY requirements.txt .

# Install remaining dependencies (no torch here anymore)
RUN pip install --no-cache-dir -r requirements.txt

# Optional: warm up model download (faster runtime startup)
RUN python -c "import torchvision.models as models; models.resnet50(weights=models.ResNet50_Weights.IMAGENET1K_V1)"

# Copy application code
COPY app.py .

# Expose FastAPI port
EXPOSE 5000

# Run service
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "5000"]