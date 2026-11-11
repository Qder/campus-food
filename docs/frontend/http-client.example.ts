/**
 * Axios 封装示例，拷进前端工程后按项目改 import 路径。
 * Vite：baseURL 用 '/api'，代理见 README.md。
 * 成功 body 就是 VO；失败看 HTTP 400 / 401 / 500，不要再解 data.data。
 */
import axios, { type AxiosError } from 'axios';
import type {
  ApiErrorBody,
  FoodItemVO,
  FoodRecommendVO,
  LoginDTO,
  LoginVO,
  PageVO,
  PlaceOrderDTO,
  OrderPlaceVO,
  RegisterDTO,
  RegisterVO,
} from './types';

const TOKEN_KEY = 'campus_food_token';
const USER_KEY = 'campus_food_user';

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
      localStorage.removeItem(USER_KEY);
      // router.push('/login');
    }
    return Promise.reject(body ?? err);
  },
);

export function saveSession(vo: LoginVO) {
  localStorage.setItem(TOKEN_KEY, vo.token);
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({ id: vo.id, nickname: vo.nickname, roleId: vo.roleId }),
  );
}

export async function register(dto: RegisterDTO): Promise<RegisterVO> {
  const { data } = await http.post<RegisterVO>('/auth/register', dto);
  return data;
}

export async function login(dto: LoginDTO): Promise<LoginVO> {
  const { data } = await http.post<LoginVO>('/auth/login', dto);
  saveSession(data);
  return data;
}

export async function listFoods(params: {
  categoryId?: number;
  campusArea?: string;
  page?: number;
  size?: number;
}): Promise<PageVO<FoodItemVO>> {
  const { data } = await http.get<PageVO<FoodItemVO>>('/food/list', { params });
  return data;
}

export async function recommend(limit = 10): Promise<FoodRecommendVO[]> {
  const { data } = await http.get<FoodRecommendVO[]>('/food/recommend', {
    params: { limit },
  });
  return data;
}

export async function placeOrder(dto: PlaceOrderDTO): Promise<OrderPlaceVO> {
  const { data } = await http.post<OrderPlaceVO>('/order/orders', dto);
  return data;
}
