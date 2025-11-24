import os
import mysql.connector

DB = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "recommendation_system",
}
THUMBS_DIR = r"D:\Code\JAVA\demo\Video\thumbs"  # 缩略图目录
THUMB_EXTS = {".jpg", ".jpeg", ".png", ".webp"}

conn = mysql.connector.connect(**DB)
conn.autocommit = False
cur = conn.cursor()

def find_item_id_by_title(title: str):
    cur.execute("SELECT id FROM items WHERE title = %s", (title,))
    row = cur.fetchone()
    return row[0] if row else None

def upsert_thumb(item_id: int, path: str):
    cur.execute(
        """
        INSERT INTO item_thumbnails (item_id, thumb_path)
        VALUES (%s, %s)
        ON DUPLICATE KEY UPDATE thumb_path = VALUES(thumb_path)
        """,
        (item_id, path),
    )

def main():
    count = 0
    for fname in os.listdir(THUMBS_DIR):
        base, ext = os.path.splitext(fname)
        if ext.lower() not in THUMB_EXTS:
            continue
        item_id = find_item_id_by_title(base)
        if not item_id:
            print(f"[skip] 无匹配 item，文件: {fname}")
            continue
        thumb_path = os.path.join(THUMBS_DIR, fname)
        upsert_thumb(item_id, thumb_path)
        count += 1
        print(f"[ok] item_id={item_id} thumb={thumb_path}")
    conn.commit()
    print(f"done, 写入 {count} 条")

if __name__ == "__main__":
    try:
        main()
    finally:
        cur.close()
        conn.close()
