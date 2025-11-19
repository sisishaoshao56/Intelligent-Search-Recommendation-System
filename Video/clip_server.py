from fastapi import FastAPI, UploadFile, File
from transformers import CLIPProcessor, CLIPModel
import torch
from PIL import Image
import io

app = FastAPI()

print("⏳ Loading CLIP model...")
model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")
print("✅ CLIP model loaded!")


@app.get("/encode_text")
def encode_text(q: str):
    inputs = processor(text=q, return_tensors="pt", padding=True)
    with torch.no_grad():
        embedding = model.get_text_features(**inputs)

    emb_list = embedding[0].tolist()
    return {"vector": emb_list, "dim": len(emb_list)}


@app.post("/encode_image")
async def encode_image(file: UploadFile = File(...)):
    img_bytes = await file.read()
    img = Image.open(io.BytesIO(img_bytes))

    inputs = processor(images=img, return_tensors="pt")
    with torch.no_grad():
        embedding = model.get_image_features(**inputs)

    emb_list = embedding[0].tolist()
    return {"vector": emb_list, "dim": len(emb_list)}
