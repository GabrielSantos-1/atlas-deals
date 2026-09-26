'use client';

import { useState, useEffect, useCallback } from 'react';
import { api } from '@/lib/api';
import type { DashboardMetrics, TopProduct, RevenueBreakdown, Promotion } from '@/types';

interface UseAnalyticsReturn {
  metrics: DashboardMetrics | null;
  topProducts: TopProduct[];
  revenueBreakdown: RevenueBreakdown | null;
  recentPromotions: Promotion[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export function useAnalytics(from?: string, to?: string): UseAnalyticsReturn {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [topProducts, setTopProducts] = useState<TopProduct[]>([]);
  const [revenueBreakdown, setRevenueBreakdown] = useState<RevenueBreakdown | null>(null);
  const [recentPromotions, setRecentPromotions] = useState<Promotion[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAnalytics = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [metricsData, topProductsData, revenueData, recentData] = await Promise.all([
        api.getDashboardMetrics(from, to),
        api.getTopProducts(10),
        api.getRevenueBreakdown('store'),
        api.getRecentPromotions(5),
      ]);
      setMetrics(metricsData);
      setTopProducts(topProductsData);
      setRevenueBreakdown(revenueData);
      setRecentPromotions(recentData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar analytics');
    } finally {
      setLoading(false);
    }
  }, [from, to]);

  useEffect(() => {
    fetchAnalytics();
  }, [fetchAnalytics]);

  return {
    metrics,
    topProducts,
    revenueBreakdown,
    recentPromotions,
    loading,
    error,
    refetch: fetchAnalytics,
  };
}
