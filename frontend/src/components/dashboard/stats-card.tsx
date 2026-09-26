'use client';

import { cn } from '@/lib/utils';
import { TrendingUp, TrendingDown } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';

interface StatsCardProps {
  icon: React.ReactNode;
  label: string;
  value: string;
  change?: number;
  changeLabel?: string;
  loading?: boolean;
  className?: string;
}

export function StatsCard({ icon, label, value, change, changeLabel, loading, className }: StatsCardProps) {
  if (loading) {
    return (
      <div className={cn('rounded-lg border bg-card p-6', className)}>
        <Skeleton className="h-4 w-20" />
        <Skeleton className="mt-2 h-8 w-32" />
        <Skeleton className="mt-2 h-4 w-24" />
      </div>
    );
  }

  const isPositive = change !== undefined && change >= 0;

  return (
    <div className={cn('rounded-lg border bg-card p-6', className)}>
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-muted-foreground">{label}</p>
        <div className="text-muted-foreground">{icon}</div>
      </div>
      <p className="mt-2 text-2xl font-bold">{value}</p>
      {change !== undefined && (
        <div className="mt-2 flex items-center gap-1">
          <div
            className={cn(
              'flex items-center text-xs font-medium',
              isPositive ? 'text-green-600' : 'text-red-600',
            )}
          >
            {isPositive ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
            <span className="ml-0.5">{Math.abs(change).toFixed(1)}%</span>
          </div>
          {changeLabel && <span className="text-xs text-muted-foreground">{changeLabel}</span>}
        </div>
      )}
    </div>
  );
}
