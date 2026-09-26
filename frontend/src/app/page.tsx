'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { PromoGrid } from '@/components/promo/promo-grid';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { CATEGORIES } from '@/lib/constants';
import { api } from '@/lib/api';
import { useRouter } from 'next/navigation';
import { Search, TrendingUp, Zap, Award, ArrowRight } from 'lucide-react';
import type { Promotion } from '@/types';

export default function HomePage() {
  const [trendingPromotions, setTrendingPromotions] = useState<Promotion[]>([]);
  const [recentPromotions, setRecentPromotions] = useState<Promotion[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const router = useRouter();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [trendingData, recentData] = await Promise.all([
          api.getPromotions({ sort: 'highest_score', size: 12, page: 1 }),
          api.getPromotions({ sort: 'most_recent', size: 8, page: 1 }),
        ]);
        setTrendingPromotions(trendingData.data);
        setRecentPromotions(recentData.data);
      } catch {
        // Handle error silently
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (search.trim()) {
      router.push(`/produtos?search=${encodeURIComponent(search.trim())}`);
    }
  };

  return (
    <div>
      <section className="relative overflow-hidden bg-gradient-to-br from-primary/5 via-background to-primary/10">
        <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 sm:py-24 lg:px-8">
          <div className="mx-auto max-w-2xl text-center">
            <div className="mb-4 inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-1.5 text-sm font-medium text-primary">
              <Zap className="h-4 w-4" />
              Promoções monitoradas em tempo real
            </div>
            <h1 className="text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl">
              As melhores ofertas de{' '}
              <span className="text-primary">tecnologia</span>
            </h1>
            <p className="mt-6 text-lg leading-8 text-muted-foreground">
              Monitoramos os preços das principais lojas para você encontrar os melhores descontos em hardware, notebooks, periféricos e muito mais.
            </p>
            <form onSubmit={handleSearch} className="mt-8">
              <div className="relative mx-auto max-w-xl">
                <Search className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="O que você está procurando?"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="h-12 pl-12 text-base"
                />
                <Button type="submit" size="sm" className="absolute right-1.5 top-1/2 -translate-y-1/2">
                  Buscar
                </Button>
              </div>
            </form>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
          {CATEGORIES.map((cat) => (
            <Link
              key={cat.slug}
              href={`/categorias/${cat.slug}`}
              className="flex flex-col items-center gap-2 rounded-lg border bg-card p-4 text-center transition-all hover:border-primary hover:shadow-sm"
            >
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
                <TrendingUp className="h-5 w-5 text-primary" />
              </div>
              <span className="text-xs font-medium">{cat.label}</span>
            </Link>
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="mb-8 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <TrendingUp className="h-5 w-5 text-primary" />
            <h2 className="text-2xl font-bold">Trending</h2>
          </div>
          <Link href="/produtos?sort=highest_score">
            <Button variant="ghost" size="sm" className="gap-1">
              Ver todas <ArrowRight className="h-4 w-4" />
            </Button>
          </Link>
        </div>
        <PromoGrid promotions={trendingPromotions} loading={loading} />
      </section>

      <section className="bg-muted/30">
        <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
          <div className="mb-8 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Award className="h-5 w-5 text-primary" />
              <h2 className="text-2xl font-bold">Recentes</h2>
            </div>
            <Link href="/produtos?sort=most_recent">
              <Button variant="ghost" size="sm" className="gap-1">
                Ver todas <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
          </div>
          <PromoGrid promotions={recentPromotions} loading={loading} />
        </div>
      </section>
    </div>
  );
}
