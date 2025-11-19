import http from './http';

export interface RegisterPayload {
  username: string;
  password: string;
  age?: number;
  gender?: boolean;
  tags_json?: string;
}

export const login = (username: string, password: string) =>
  http.post<{ token: string }>('/auth/login', null, { params: { username, password } });

export const register = (payload: RegisterPayload) =>
  http.post<{ token: string }>('/auth/register', payload);

export const logout = (token: string) =>
  http.post<void>('/auth/logout', null, { headers: { Authorization: token } });
