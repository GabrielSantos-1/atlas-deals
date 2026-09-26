'use client';

import Image from 'next/image';
import Link from 'next/link';
import { PriceTag } from '@/components/promo/price-tag';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { ShoppingCart } from 'lucide-react';
import type { Product } from '@/types';

interface ProductCardProps {
  product: Product;
  className?: string;
}

export function ProductCard({ product, className }: ProductCardProps) {
  const discount = product.discountPercentage;

  return (
    <div className={cn('group relative flex flex-col overflow-hidden rounded-lg border bg-card shadow-sm transition-all hover:shadow-md', className)}>
      <Link href={`/produtos/${product.id}`} className="absolute inset-0 z-10" />
      <div className="relative aspect-[4/3] overflow-hidden bg-muted">
        {product.imageUrl ? (
          <Image
            src={product.imageUrl}
            alt={product.title}
            fill
            className="object-cover transition-transform group-hover:scale-105"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-muted-foreground">
            <ShoppingCart className="h-12 w-12" />
          </div>
        )}
        {discount > 0 && (
          <div className="absolute left-2 top-2 z-20">
            <span className="inline-flex items-center rounded bg-green-500 px-2 py-1 text-xs font-bold text-white">
              -{discount}%
            </span>
          </div>
        )}
        {!product.inStock && (
          <div className="absolute inset-0 z-20 flex items-center justify-center bg-black/50">
            <Badge variant="destructive">Indisponível</Badge>
          </div>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-2 p-4">
        <p className="text-xs text-muted-foreground">{product.storeName}</p>
        <h3 className="line-clamp-2 text-sm font-medium leading-snug">{product.title}</h3>
        <PriceTag currentPrice={product.currentPrice} originalPrice={product.originalPrice} />
        <div className="mt-auto flex items-center gap-2 text-xs text-muted-foreground">
          {product.brand && <span>{product.brand}</span>}
          {product.model && <span>• {product.model}</span>}
        </div>
        <div className="relative z-20 mt-2">
          <span className="inline-flex h-9 w-full items-center justify-center rounded-md bg-primary text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90">
            Ver produto
          </span>
        </div>
      </div>
    </div>
  );
}
