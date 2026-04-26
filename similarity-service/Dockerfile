FROM python:3.11-slim
 
WORKDIR /app
 
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

RUN python -c "import torchvision.models as models; models.resnet50(weights=models.ResNet50_Weights.IMAGENET1K_V1)"
 
COPY app.py .
 
EXPOSE 5000
 
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "5000"]
