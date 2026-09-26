'use client';

import { useState, useEffect } from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';
import { api } from '@/lib/api';
import { formatCurrency } from '@/lib/utils';
import { LoadingSpinner } from '@/components/shared/loading-spinner';

interface RevenueDataPoint {
  date: string;
  revenue: number;
}

export function RevenueChart() {
  const [data, setData] = useState<RevenueDataPoint[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const metrics = await api.getDashboardMetrics();
        const breakdown = await api.getRevenueBreakdown('day');
        const chartData: RevenueDataPoint[] = Object.entries(breakdown).map(([date, revenue]) => ({
          date: date.slice(5, 10),
          revenue,
        }));
        setData(chartData);
      } catch {
        // fallback
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <LoadingSpinner />;
  if (!data.length) return <p className="text-sm text-muted-foreground">Nenhum dado disponível</p>;

  return (
    <div className="h-[300px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
          <XAxis dataKey="date" className="text-xs text-muted-foreground" tickLine={false} axisLine={false} />
          <YAxis
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
            formatter={(value: number) => [formatCurrency(value), 'Receita']}
          />
          <Bar dataKey="revenue" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
