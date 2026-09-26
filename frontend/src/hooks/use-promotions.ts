'use client';

import { useState, useEffect, useCallback } from 'react';
import { api } from '@/lib/api';
import type { Promotion, PromotionFilters, PaginatedResponse } from '@/types';

interface UsePromotionsReturn {
  promotions: Promotion[];
  totalPages: number;
  total: number;
  page: number;
  loading: boolean;
  error: string | null;
  setPage: (page: number) => void;
  refetch: () => Promise<void>;
}

export function usePromotions(filters?: PromotionFilters): UsePromotionsReturn {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(filters?.page || 1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPromotions = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result: PaginatedResponse<Promotion> = await api.getPromotions({
        ...filters,
        page,
      });
      setPromotions(result.data);
      setTotalPages(result.totalPages);
      setTotal(result.total);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar promoções');
    } finally {
      setLoading(false);
    }
  }, [filters, page]);

  useEffect(() => {
    fetchPromotions();
  }, [fetchPromotions]);

  return {
    promotions,
    totalPages,
    total,
    page,
    loading,
    error,
    setPage,
    refetch: fetchPromotions,
  };
}
