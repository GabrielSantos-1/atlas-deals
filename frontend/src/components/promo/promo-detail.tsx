'use client';

import Image from 'next/image';
import Link from 'next/link';
import { DiscountBadge } from './discount-badge';
import { PriceTag } from './price-tag';
import { PriceHistoryChart } from '@/components/product/price-history-chart';
import { ProductSpecs } from '@/components/product/product-specs';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatRelativeTime, formatDate, cn } from '@/lib/utils';
import { getScoreColor } from '@/lib/utils';
import { PLATFORMS, CATEGORY_LABELS } from '@/lib/constants';
import {
  ShoppingCart,
  ExternalLink,
  Share2,
  Star,
  Store,
  Clock,
  Tag,
  CheckCircle2,
  AlertTriangle,
  TrendingUp,
  BarChart3,
} from 'lucide-react';
import type { Promotion } from '@/types';

interface PromoDetailProps {
  promotion: Promotion;
}

const faqItems = [
  { q: 'Como funciona o cashback?', a: 'O cashback é creditado automaticamente na sua conta após a confirmação da compra através do nosso link.' },
  { q: 'O preço mostrado é atualizado?', a: 'Monitoramos os preços em tempo real, mas podem ocorrer alterações sem aviso prévio pela loja.' },
  { q: 'Como sei se a oferta ainda está ativa?', a: 'Verificamos o status das ofertas a cada 30 minutos. O selo "Ativa" indica que foi confirmada recentemente.' },
];

export function PromoDetail({ promotion }: PromoDetailProps) {
  const categoryLabel = CATEGORY_LABELS[promotion.category] || promotion.category;

  const handleShare = async (platform: string) => {
    const url = window.location.href;
    const text = `Confira: ${promotion.title} por ${promotion.currentPrice}`;
    const urls: Record<string, string> = {
      whatsapp: `https://wa.me/?text=${encodeURIComponent(text + ' ' + url)}`,
      telegram: `https://t.me/share/url?url=${encodeURIComponent(url)}&text=${encodeURIComponent(text)}`,
      twitter: `https://twitter.com/intent/tweet?text=${encodeURIComponent(text)}&url=${encodeURIComponent(url)}`,
      facebook: `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`,
    };
    if (urls[platform]) window.open(urls[platform], '_blank', 'noopener');
  };

  return (
    <div className="space-y-8">
      <div className="grid gap-8 lg:grid-cols-2">
        <div className="relative aspect-square overflow-hidden rounded-lg bg-muted">
          {promotion.imageUrl ? (
            <Image
              src={promotion.imageUrl}
              alt={promotion.title}
              fill
              className="object-cover"
              priority
              sizes="(max-width: 1024px) 100vw, 50vw"
            />
          ) : (
            <div className="flex h-full items-center justify-center">
              <ShoppingCart className="h-20 w-20 text-muted-foreground" />
            </div>
          )}
          <div className="absolute left-3 top-3">
            <DiscountBadge percentage={promotion.discountPercentage} size="lg" />
          </div>
          {promotion.status === 'active' && (
            <div className="absolute right-3 top-3">
              <Badge variant="success" className="flex items-center gap-1">
                <CheckCircle2 className="h-3 w-3" />
                Ativa
              </Badge>
            </div>
          )}
          {promotion.status === 'expired' && (
            <div className="absolute right-3 top-3">
              <Badge variant="destructive" className="flex items-center gap-1">
                <AlertTriangle className="h-3 w-3" />
                Expirada
              </Badge>
            </div>
          )}
        </div>

        <div className="flex flex-col gap-6">
          <div>
            <div className="mb-2 flex items-center gap-2 text-sm text-muted-foreground">
              <Link href={`/categorias/${promotion.category}`} className="hover:text-foreground transition-colors">
                {categoryLabel}
              </Link>
              <span>•</span>
              <div className="flex items-center gap-1">
                <Store className="h-3.5 w-3.5" />
                <span>{promotion.storeName}</span>
              </div>
            </div>
            <h1 className="text-2xl font-bold leading-tight lg:text-3xl">{promotion.title}</h1>
          </div>

          <PriceTag currentPrice={promotion.currentPrice} originalPrice={promotion.originalPrice} />

          {promotion.reason && (
            <div className="flex items-start gap-2 rounded-lg bg-muted p-3 text-sm">
              <TrendingUp className="mt-0.5 h-4 w-4 shrink-0 text-green-500" />
              <span>{promotion.reason}</span>
            </div>
          )}

          <div className="flex items-center gap-4">
            <Link href={promotion.productId ? `/produtos/${promotion.productId}` : '#'}>
              <Button variant="outline" className="gap-2">
                <BarChart3 className="h-4 w-4" />
                Histórico de Preços
              </Button>
            </Link>
            <a
              href={`/produtos/${promotion.productId}`}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
            >
              Ver oferta
              <ExternalLink className="h-4 w-4" />
            </a>
          </div>

          <div className={cn('flex items-center gap-2 text-lg font-semibold', getScoreColor(promotion.score))}>
            <Star className="h-5 w-5 fill-current" />
            <span>{promotion.score.toFixed(1)}</span>
            <span className="text-sm font-normal text-muted-foreground">/ 10</span>
          </div>

          <div className="flex flex-wrap gap-2">
            {promotion.tags?.map((tag) => (
              <Badge key={tag} variant="secondary" className="text-xs">{tag}</Badge>
            ))}
          </div>

          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <Clock className="h-3.5 w-3.5" />
            <span>Detectada {formatRelativeTime(promotion.detectedAt)}</span>
          </div>

          <div>
            <p className="mb-2 text-sm font-medium">Compartilhar</p>
            <div className="flex gap-2">
              {PLATFORMS.slice(0, 4).map((platform) => (
                <Button
                  key={platform.id}
                  variant="outline"
                  size="icon"
                  onClick={() => handleShare(platform.id)}
                  title={platform.label}
                >
                  <Share2 className="h-4 w-4" />
                </Button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {promotion.productId && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Histórico de Preços (30 dias)</CardTitle>
          </CardHeader>
          <CardContent>
            <PriceHistoryChart productId={promotion.productId} />
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Perguntas Frequentes</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {faqItems.map((item, i) => (
              <div key={i}>
                <h4 className="font-medium">{item.q}</h4>
                <p className="mt-1 text-sm text-muted-foreground">{item.a}</p>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
