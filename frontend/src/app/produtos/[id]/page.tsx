'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { PriceHistoryChart } from '@/components/product/price-history-chart';
import { ProductSpecs } from '@/components/product/product-specs';
import { PriceTag } from '@/components/promo/price-tag';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { LoadingSpinner } from '@/components/shared/loading-spinner';
import { api } from '@/lib/api';
import { formatCurrency } from '@/lib/utils';
import { ArrowLeft, ExternalLink, BarChart3, Package } from 'lucide-react';
import Link from 'next/link';
import type { Product, PriceStats } from '@/types';

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<Product | null>(null);
  const [priceStats, setPriceStats] = useState<PriceStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProduct = async () => {
      setLoading(true);
      setError(null);
      try {
        const [productData, statsData] = await Promise.all([
          api.getProduct(id),
          api.getPriceStats(id),
        ]);
        setProduct(productData);
        setPriceStats(statsData);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao carregar produto');
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  if (loading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Skeleton className="h-8 w-64" />
        <div className="mt-8 grid gap-8 lg:grid-cols-2">
          <Skeleton className="aspect-square rounded-lg" />
          <div className="space-y-4">
            <Skeleton className="h-6 w-3/4" />
            <Skeleton className="h-10 w-32" />
            <Skeleton className="h-40 w-full" />
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="flex flex-col items-center gap-4 py-16">
          <p className="text-destructive">{error}</p>
          <Button variant="outline" onClick={() => window.location.reload()}>
            Tentar novamente
          </Button>
        </div>
      </div>
    );
  }

  if (!product) return null;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <Link href="/produtos" className="mb-6 inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" />
        Voltar para produtos
      </Link>

      <div className="grid gap-8 lg:grid-cols-2">
        <div className="relative aspect-square overflow-hidden rounded-lg bg-muted">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.title} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full items-center justify-center">
              <Package className="h-20 w-20 text-muted-foreground" />
            </div>
          )}
        </div>

        <div className="flex flex-col gap-6">
          <div>
            <p className="text-sm text-muted-foreground">{product.storeName}</p>
            <h1 className="text-2xl font-bold lg:text-3xl">{product.title}</h1>
          </div>

          <PriceTag currentPrice={product.currentPrice} originalPrice={product.originalPrice} />

          {priceStats && (
            <div className="grid grid-cols-3 gap-4 rounded-lg border bg-card p-4">
              <div>
                <p className="text-xs text-muted-foreground">Menor preço</p>
                <p className="text-sm font-semibold text-green-600">{formatCurrency(priceStats.lowestPrice)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Maior preço</p>
                <p className="text-sm font-semibold text-red-600">{formatCurrency(priceStats.highestPrice)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Preço médio</p>
                <p className="text-sm font-semibold">{formatCurrency(priceStats.averagePrice)}</p>
              </div>
            </div>
          )}

          <div className="flex items-center gap-3">
            <Badge variant={product.inStock ? 'success' : 'destructive'}>
              {product.inStock ? 'Em estoque' : 'Indisponível'}
            </Badge>
          </div>

          <ProductSpecs product={product} />

          {product.description && (
            <p className="text-sm text-muted-foreground">{product.description}</p>
          )}

          <div className="flex gap-3">
            <a
              href={product.url}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
            >
              Comprar agora
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>
        </div>
      </div>

      <Card className="mt-8">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            <BarChart3 className="h-5 w-5" />
            Histórico de Preços
          </CardTitle>
        </CardHeader>
        <CardContent>
          <PriceHistoryChart productId={product.id} />
        </CardContent>
      </Card>
    </div>
  );
}
