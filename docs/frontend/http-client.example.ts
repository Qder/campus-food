/**
 * Axios 封装示例（需自行安装 axios）。
 * 配合 Vite 代理：baseURL 使用 '/api'，见 frontend/README.md
 */
import axios, { type AxiosError } from 'axios';
import type { ApiErrorBody, LoginDTO, LoginVO } from './types';

const TOKEN_KEY = 'campus_food_token';

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (err: AxiosError<ApiErrorBody>) => {
    const status = err.response?.status;
    const body = err.response?.data;
    if (status === 401 || body?.code === 401) {
      localStorage.removeItem(TOKEN_KEY);
      // router.push('/login');
    }
    return Promise.reject(body ?? err);
  },
);

export function saveToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export async function login(dto: LoginDTO): Promise<LoginVO> {
  const { data } = await http.post<LoginVO>('/auth/login', dto);
  saveToken(data.token);
  return data;
}

/** 公开美食列表，可不登录 */
export async function listFoods(params: {
  categoryId?: number;
  campusArea?: string;
  page?: number;
  size?: number;
}) {
  const { data } = await http.get('/food/list', { params });
  return data;
}
