'use client';

import Link from 'next/link';
import type { Promotion } from '@/types';
import { formatCurrency, formatRelativeTime } from '@/lib/utils';
import { DiscountBadge } from '@/components/promo/discount-badge';
import { Skeleton } from '@/components/ui/skeleton';

interface RecentPromotionsProps {
  promotions: Promotion[];
  loading?: boolean;
}

export function RecentPromotions({ promotions, loading }: RecentPromotionsProps) {
  if (loading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="flex items-center gap-3">
            <Skeleton className="h-12 w-12 rounded" />
            <div className="flex-1 space-y-1">
              <Skeleton className="h-4 w-3/4" />
              <Skeleton className="h-3 w-1/2" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (!promotions.length) {
    return <p className="text-sm text-muted-foreground py-4 text-center">Nenhuma promoção recente</p>;
  }

  return (
    <div className="space-y-3">
      {promotions.map((promo) => (
        <Link
          key={promo.id}
          href={`/promo/${promo.id}`}
          className="flex items-center gap-3 rounded-lg border p-3 transition-colors hover:bg-muted/50"
        >
          <div className="h-12 w-12 flex-shrink-0 overflow-hidden rounded bg-muted">
            {promo.imageUrl ? (
              <img src={promo.imageUrl} alt="" className="h-full w-full object-cover" />
            ) : (
              <div className="flex h-full items-center justify-center text-muted-foreground text-xs">
                ?
              </div>
            )}
          </div>
          <div className="flex-1 min-w-0">
            <p className="truncate text-sm font-medium">{promo.title}</p>
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <span>{formatCurrency(promo.currentPrice)}</span>
              <span>{promo.storeName}</span>
              <span>{formatRelativeTime(promo.detectedAt)}</span>
            </div>
          </div>
          <DiscountBadge percentage={promo.discountPercentage} size="sm" />
        </Link>
      ))}
    </div>
  );
}
