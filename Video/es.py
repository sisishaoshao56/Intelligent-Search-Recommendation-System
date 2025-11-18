import json, pymysql
from elasticsearch import Elasticsearch, helpers

mysql = pymysql.connect(host="localhost", user="root", password="123456",
                        database="recommendation_system", charset="utf8mb4")
es = Elasticsearch("http://localhost:9200")
index = "items"

with mysql.cursor() as cur:
    cur.execute("SELECT id, title, tags_json, path, embedding_vector FROM items")
    rows = cur.fetchall()

actions = []
for id_, title, tags, path, emb_str in rows:
    if not emb_str:
        continue
    emb = json.loads(emb_str)
    actions.append({
        "_index": index,
        "_id": id_,
        "_source": {
            "title": title or "",
            "tags": tags or "",
            "path": path or "",
            "embedding": emb
        }
    })
helpers.bulk(es, actions)
print("done, indexed", len(actions))
mysql.close()
