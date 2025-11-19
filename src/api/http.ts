import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  timeout: 8000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = token;
  }
  return config;
});

export default http;
