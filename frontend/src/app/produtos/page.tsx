'use client';

import { useState, useCallback } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { PromoGrid } from '@/components/promo/promo-grid';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { Pagination } from '@/components/shared/pagination';
import { usePromotions } from '@/hooks/use-promotions';
import { useDebounce } from '@/hooks/use-debounce';
import { CATEGORIES } from '@/lib/constants';
import { SlidersHorizontal, X } from 'lucide-react';
import type { SortOption } from '@/types';

export default function ProductsPage() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [category, setCategory] = useState(searchParams.get('category') || '');
  const [sort, setSort] = useState<SortOption>((searchParams.get('sort') as SortOption) || 'highest_score');
  const [store, setStore] = useState(searchParams.get('store') || '');
  const [minPrice, setMinPrice] = useState(searchParams.get('minPrice') || '');
  const [maxPrice, setMaxPrice] = useState(searchParams.get('maxPrice') || '');
  const [showFilters, setShowFilters] = useState(false);

  const debouncedSearch = useDebounce(search, 400);

  const filters = {
    search: debouncedSearch || undefined,
    category: category || undefined,
    sort,
    store: store || undefined,
    minPrice: minPrice ? Number(minPrice) : undefined,
    maxPrice: maxPrice ? Number(maxPrice) : undefined,
  };

  const { promotions, totalPages, page, loading, error, setPage, refetch } = usePromotions(filters);

  const clearFilters = useCallback(() => {
    setSearch('');
    setCategory('');
    setSort('highest_score');
    setStore('');
    setMinPrice('');
    setMaxPrice('');
  }, []);

  const hasFilters = search || category || store || minPrice || maxPrice;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold">Produtos</h1>
        <p className="mt-2 text-muted-foreground">
          Encontre as melhores ofertas em tecnologia
        </p>
      </div>

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <div className="relative flex-1 max-w-md">
          <Input
            placeholder="Buscar produtos..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <Select
          value={sort}
          onChange={(e) => setSort(e.target.value as SortOption)}
          options={[
            { value: 'highest_score', label: 'Melhor pontuação' },
            { value: 'biggest_discount', label: 'Maior desconto' },
            { value: 'lowest_price', label: 'Menor preço' },
            { value: 'most_recent', label: 'Mais recentes' },
          ]}
          className="w-44"
        />
        <Button
          variant="outline"
          size="sm"
          onClick={() => setShowFilters(!showFilters)}
          className="gap-2"
        >
          <SlidersHorizontal className="h-4 w-4" />
          Filtros
          {hasFilters && <span className="flex h-2 w-2 rounded-full bg-primary" />}
        </Button>
        {hasFilters && (
          <Button variant="ghost" size="sm" onClick={clearFilters} className="gap-1">
            <X className="h-4 w-4" />
            Limpar
          </Button>
        )}
      </div>

      {showFilters && (
        <div className="mb-6 grid grid-cols-2 gap-4 rounded-lg border bg-card p-4 sm:grid-cols-4 animate-fade-in">
          <Select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            placeholder="Categoria"
            options={[{ value: '', label: 'Todas' }, ...CATEGORIES.map((c) => ({ value: c.slug, label: c.label }))]}
          />
          <Input
            placeholder="Preço mínimo"
            type="number"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
          />
          <Input
            placeholder="Preço máximo"
            type="number"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
          />
          <Input
            placeholder="Loja"
            value={store}
            onChange={(e) => setStore(e.target.value)}
          />
        </div>
      )}

      <PromoGrid promotions={promotions} loading={loading} error={error} />

      {!loading && !error && (
        <div className="mt-8">
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
