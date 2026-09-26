'use client';

import { useState, useEffect } from 'react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
} from 'recharts';
import { api } from '@/lib/api';
import { PLATFORMS } from '@/lib/constants';
import { LoadingSpinner } from '@/components/shared/loading-spinner';

interface PlatformData {
  name: string;
  value: number;
  color: string;
}

export function PlatformBreakdown() {
  const [data, setData] = useState<PlatformData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const breakdown = await api.getRevenueBreakdown('platform');
        const chartData: PlatformData[] = Object.entries(breakdown).map(([platform, value]) => {
          const p = PLATFORMS.find((x) => x.id === platform || x.label === platform);
          return {
            name: p?.label || platform,
            value,
            color: p?.color || '#6366f1',
          };
        });
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
  if (!data.length) return <p className="text-sm text-muted-foreground py-4 text-center">Nenhum dado disponível</p>;

  return (
    <div className="h-[300px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={100}
            paddingAngle={2}
            dataKey="value"
          >
            {data.map((entry, index) => (
              <Cell key={index} fill={entry.color} />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{
              backgroundColor: 'hsl(var(--card))',
              border: '1px solid hsl(var(--border))',
              borderRadius: 'var(--radius)',
            }}
            formatter={(value: number) => [value.toLocaleString(), 'Receita']}
          />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
