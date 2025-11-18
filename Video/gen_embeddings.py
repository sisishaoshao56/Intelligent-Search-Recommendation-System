import os, json, cv2, numpy as np, pymysql
from sentence_transformers import SentenceTransformer
from PIL import Image

BASE = r"D:\Code\JAVA\demo\Video\data\tiny-Kinetics-400"
DB_URL = {"host":"localhost","user":"root","password":"123456","database":"recommendation_system","charset":"utf8mb4"}

model = SentenceTransformer("clip-ViT-B-32")

def extract_frame(video_path, sec=1.0):
    cap = cv2.VideoCapture(video_path)
    cap.set(cv2.CAP_PROP_POS_MSEC, sec * 1000)
    ok, frame = cap.read()
    cap.release()
    if not ok:
        return None
    return Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))

def embed_frame(img, tags, w_img=0.7, w_txt=0.3):
    img_vec = model.encode([img], convert_to_numpy=True, normalize_embeddings=True)[0]
    text = " ".join(tags) if isinstance(tags, (list, tuple)) else str(tags)
    txt_vec = model.encode([text], convert_to_numpy=True, normalize_embeddings=True)[0]
    vec = w_img * img_vec + w_txt * txt_vec
    vec = vec / np.linalg.norm(vec)
    return vec.tolist()

conn = pymysql.connect(**DB_URL)
cur = conn.cursor()
sql = """INSERT INTO items (title, tags_json, embedding_vector, path)
         VALUES (%s, %s, %s, %s)"""

batch = []
for root, _, files in os.walk(BASE):
    tag = os.path.basename(root)
    for f in files:
        if not f.lower().endswith((".mp4", ".avi", ".mov", ".mkv")):
            continue
        path = os.path.join(root, f)
        img = extract_frame(path, sec=1.0) or extract_frame(path, sec=0.1)
        if img is None:
            continue
        vec = embed_frame(img, [tag])
        title = os.path.splitext(f)[0]
        batch.append((title, json.dumps([tag]) , json.dumps(vec), path))
        if len(batch) >= 200:
            cur.executemany(sql, batch); conn.commit(); batch.clear()

if batch:
    cur.executemany(sql, batch); conn.commit()
cur.close(); conn.close()
print("done")
