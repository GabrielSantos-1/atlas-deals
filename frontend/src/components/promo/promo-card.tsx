'use client';

import Image from 'next/image';
import Link from 'next/link';
import { DiscountBadge } from './discount-badge';
import { PriceTag } from './price-tag';
import { Badge } from '@/components/ui/badge';
import { formatRelativeTime } from '@/lib/utils';
import { getScoreColor } from '@/lib/utils';
import { cn } from '@/lib/utils';
import { Clock, ShoppingCart, Star, Store } from 'lucide-react';
import type { Promotion } from '@/types';

interface PromoCardProps {
  promotion: Promotion;
  className?: string;
}

export function PromoCard({ promotion, className }: PromoCardProps) {
  return (
    <div
      className={cn(
        'group relative flex flex-col overflow-hidden rounded-lg border bg-card shadow-sm transition-all hover:shadow-md animate-fade-in',
        className,
      )}
    >
      <Link href={`/promo/${promotion.id}`} className="absolute inset-0 z-10" />
      <div className="relative aspect-[4/3] overflow-hidden bg-muted">
        {promotion.imageUrl ? (
          <Image
            src={promotion.imageUrl}
            alt={promotion.title}
            fill
            className="object-cover transition-transform group-hover:scale-105"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-muted-foreground">
            <ShoppingCart className="h-12 w-12" />
          </div>
        )}
        <div className="absolute left-2 top-2 z-20">
          <DiscountBadge percentage={promotion.discountPercentage} />
        </div>
        {promotion.status === 'expired' && (
          <div className="absolute inset-0 z-20 flex items-center justify-center bg-black/50">
            <Badge variant="destructive" className="text-sm">Expirada</Badge>
          </div>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-2 p-4">
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <Store className="h-3 w-3" />
            <span>{promotion.storeName}</span>
          </div>
          <div className={cn('flex items-center gap-1 text-xs font-medium', getScoreColor(promotion.score))}>
            <Star className="h-3 w-3 fill-current" />
            <span>{promotion.score.toFixed(1)}</span>
          </div>
        </div>
        <h3 className="line-clamp-2 text-sm font-medium leading-snug">
          {promotion.title}
        </h3>
        <PriceTag currentPrice={promotion.currentPrice} originalPrice={promotion.originalPrice} />
        <div className="mt-auto flex items-center gap-1 text-xs text-muted-foreground">
          <Clock className="h-3 w-3" />
          <span>{formatRelativeTime(promotion.detectedAt)}</span>
        </div>
        <div className="relative z-20 mt-2">
          <Link
            href={`/promo/${promotion.id}`}
            className="inline-flex h-9 w-full items-center justify-center rounded-md bg-primary text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            Ver oferta
          </Link>
        </div>
      </div>
    </div>
  );
}
