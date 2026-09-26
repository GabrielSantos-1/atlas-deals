export interface Product {
  id: string;
  title: string;
  description: string;
  currentPrice: number;
  originalPrice: number;
  currency: string;
  imageUrl: string;
  storeName: string;
  category: string;
  brand: string;
  model: string;
  inStock: boolean;
  url: string;
  discountPercentage: number;
}

export interface Promotion {
  id: string;
  productId: string;
  title: string;
  currentPrice: number;
  originalPrice: number;
  discountPercentage: number;
  score: number;
  category: string;
  storeName: string;
  imageUrl: string;
  inStock: boolean;
  status: 'active' | 'expired' | 'pending';
  tags: string[];
  reason: string;
  detectedAt: string;
}

export interface PriceRecord {
  id: string;
  price: number;
  originalPrice: number;
  recordedAt: string;
  inStock: boolean;
}

export interface PriceStats {
  currentPrice: number;
  lowestPrice: number;
  highestPrice: number;
  averagePrice: number;
  trend: 'up' | 'down' | 'stable';
  percentageChange: number;
}

export interface DashboardMetrics {
  totalRevenue: number;
  totalClicks: number;
  totalConversions: number;
  averageCTR: number;
  averageConversionRate: number;
  revenueChange: number;
  clickChange: number;
}

export interface Publication {
  id: string;
  platform: string;
  status: string;
  publishedAt: string;
  platformPostId: string;
}

export interface AnalyticsEvent {
  id: string;
  productId: string;
  platform: string;
  eventType: string;
  timestamp: string;
}

export interface User {
  id: string;
  name: string;
  email: string;
  avatar: string;
  roles: string[];
}

export interface Store {
  name: string;
  logo: string;
  url: string;
  reputation: number;
}

export interface PaginatedResponse<T> {
  data: T[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export type SortOption = 'biggest_discount' | 'lowest_price' | 'most_recent' | 'highest_score';

export interface PromotionFilters {
  category?: string;
  store?: string;
  minPrice?: number;
  maxPrice?: number;
  brand?: string;
  sort?: SortOption;
  page?: number;
  size?: number;
  search?: string;
}

export interface RevenueBreakdown {
  [key: string]: number;
}

export interface TopProduct {
  id: string;
  title: string;
  clicks: number;
  conversions: number;
  revenue: number;
  conversionRate: number;
}
