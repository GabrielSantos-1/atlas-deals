'use client';

import { useState, useEffect } from 'react';
import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';
import { api } from '@/lib/api';
import { formatCurrency, formatDate } from '@/lib/utils';
import { LoadingSpinner } from '@/components/shared/loading-spinner';
import type { PriceRecord } from '@/types';

interface PriceHistoryChartProps {
  productId: string;
  days?: number;
}

interface ChartDataPoint {
  date: string;
  price: number;
  originalPrice: number;
}

export function PriceHistoryChart({ productId, days = 30 }: PriceHistoryChartProps) {
  const [data, setData] = useState<ChartDataPoint[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        const from = new Date(Date.now() - days * 24 * 60 * 60 * 1000).toISOString();
        const records: PriceRecord[] = await api.getPriceHistory(productId, from);
        const chartData = records
          .map((r) => ({
            date: formatDate(r.recordedAt, 'dd/MM'),
            price: r.price,
            originalPrice: r.originalPrice,
          }))
          .reverse();
        setData(chartData);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao carregar histórico');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [productId, days]);

  if (loading) return <LoadingSpinner text="Carregando gráfico..." />;
  if (error) return <p className="text-sm text-destructive">{error}</p>;
  if (!data.length) return <p className="text-sm text-muted-foreground">Nenhum dado disponível</p>;

  const lowestPrice = Math.min(...data.map((d) => d.price));
  const minPrice = lowestPrice * 0.95;
  const maxPrice = Math.max(...data.map((d) => d.price)) * 1.05;

  return (
    <div className="h-[300px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <ComposedChart data={data}>
          <defs>
            <linearGradient id="priceGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.3} />
              <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
          <XAxis
            dataKey="date"
            className="text-xs text-muted-foreground"
            tickLine={false}
            axisLine={false}
            interval="preserveStartEnd"
          />
          <YAxis
            domain={[minPrice, maxPrice]}
            tickFormatter={(v) => formatCurrency(v)}
            className="text-xs text-muted-foreground"
            tickLine={false}
            axisLine={false}
            width={80}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: 'hsl(var(--card))',
              border: '1px solid hsl(var(--border))',
              borderRadius: 'var(--radius)',
            }}
            formatter={(value: number) => [formatCurrency(value), 'Preço']}
          />
          <Area
            type="monotone"
            dataKey="price"
            fill="url(#priceGradient)"
            stroke="hsl(var(--primary))"
            strokeWidth={2}
          />
          {data.some((d) => d.originalPrice > d.price) && (
            <Line
              type="monotone"
              dataKey="originalPrice"
              stroke="hsl(var(--muted-foreground))"
              strokeWidth={1}
              strokeDasharray="5 5"
              dot={false}
            />
          )}
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
