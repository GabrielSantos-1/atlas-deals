'use client';

import { useParams } from 'next/navigation';
import { PromoGrid } from '@/components/promo/promo-grid';
import { Pagination } from '@/components/shared/pagination';
import { usePromotions } from '@/hooks/use-promotions';
import { CATEGORY_LABELS } from '@/lib/constants';
import { Package } from 'lucide-react';

export default function CategoryPage() {
  const { slug } = useParams<{ slug: string }>();
  const categoryLabel = CATEGORY_LABELS[slug] || slug;

  const { promotions, totalPages, page, loading, error, setPage } = usePromotions({
    category: slug,
    size: 20,
  });

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
            <Package className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold">{categoryLabel}</h1>
            <p className="text-muted-foreground">
              Melhores ofertas em {categoryLabel.toLowerCase()}
            </p>
          </div>
        </div>
      </div>

      <PromoGrid promotions={promotions} loading={loading} error={error} />

      {!loading && !error && (
        <div className="mt-8">
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
