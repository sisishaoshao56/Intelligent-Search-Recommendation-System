import http from './http';

export interface Item {
  id: number;
  title?: string;
  catagory?: string;
  tags_json?: string;
  path?: string;
}

export const fetchHot = (limit = 12) =>
  http.get<Item[]>('/recommend/hot', { params: { limit } });

export const fetchEmbeddingRec = (userId: number, limit = 12) =>
  http.get<Item[]>('/recommend/embedding', { params: { userId, limit } });
