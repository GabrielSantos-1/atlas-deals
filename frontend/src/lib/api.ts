import {
  type AuthResponse,
  type DashboardMetrics,
  type PaginatedResponse,
  type PriceRecord,
  type PriceStats,
  type Product,
  type Promotion,
  type PromotionFilters,
  type RevenueBreakdown,
  type TopProduct,
  type User,
  type Publication,
} from '@/types';
import { buildQueryString } from './utils';

class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

class ApiClient {
  private baseURL: string;

  constructor(baseURL?: string) {
    this.baseURL = baseURL || process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
  }

  private getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('atlas_token');
  }

  private async request<T>(
    method: string,
    path: string,
    body?: unknown,
    params?: Record<string, string | number | boolean | undefined | null>,
  ): Promise<T> {
    let url = `${this.baseURL}${path}`;
    if (params) {
      const qs = buildQueryString(params);
      if (qs) url += `?${qs}`;
    }

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });

    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}`;
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch {
        // keep default message
      }
      throw new ApiError(response.status, errorMessage);
    }

    if (response.status === 204) return undefined as T;
    return response.json();
  }

  async get<T>(path: string, params?: Record<string, string | number | boolean | undefined | null>): Promise<T> {
    return this.request<T>('GET', path, undefined, params);
  }

  async post<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>('POST', path, body);
  }

  async put<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>('PUT', path, body);
  }

  async patch<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>('PATCH', path, body);
  }

  async delete<T>(path: string): Promise<T> {
    return this.request<T>('DELETE', path);
  }

  async getPromotions(filters?: PromotionFilters): Promise<PaginatedResponse<Promotion>> {
    return this.get<PaginatedResponse<Promotion>>('/promotions', filters as Record<string, string | number | boolean | undefined | null>);
  }

  async getPromotion(id: string): Promise<Promotion> {
    return this.get<Promotion>(`/promotions/${id}`);
  }

  async getProduct(id: string): Promise<Product> {
    return this.get<Product>(`/products/${id}`);
  }

  async getPriceHistory(id: string, from?: string, to?: string): Promise<PriceRecord[]> {
    return this.get<PriceRecord[]>(`/products/${id}/price-history`, { from, to });
  }

  async getPriceStats(id: string): Promise<PriceStats> {
    return this.get<PriceStats>(`/products/${id}/price-stats`);
  }

  async getDashboardMetrics(from?: string, to?: string): Promise<DashboardMetrics> {
    return this.get<DashboardMetrics>('/dashboard/metrics', { from, to });
  }

  async getRevenueBreakdown(dimension: string = 'store'): Promise<RevenueBreakdown> {
    return this.get<RevenueBreakdown>('/dashboard/revenue-breakdown', { dimension });
  }

  async getTopProducts(limit: number = 10): Promise<TopProduct[]> {
    return this.get<TopProduct[]>('/dashboard/top-products', { limit });
  }

  async getRecentPromotions(limit: number = 10): Promise<Promotion[]> {
    return this.get<Promotion[]>('/dashboard/recent-promotions', { limit });
  }

  async getPublications(): Promise<Publication[]> {
    return this.get<Publication[]>('/dashboard/publications');
  }

  async login(email: string, password: string): Promise<AuthResponse> {
    return this.post<AuthResponse>('/auth/login', { email, password });
  }

  async register(name: string, email: string, password: string): Promise<AuthResponse> {
    return this.post<AuthResponse>('/auth/register', { name, email, password });
  }

  async getUserProfile(): Promise<User> {
    return this.get<User>('/auth/profile');
  }

  async approvePromotion(id: string): Promise<void> {
    return this.patch<void>(`/promotions/${id}/approve`);
  }

  async rejectPromotion(id: string): Promise<void> {
    return this.patch<void>(`/promotions/${id}/reject`);
  }
}

export const api = new ApiClient();
export { ApiError };
