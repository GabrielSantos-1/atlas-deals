'use client';

import { PromoCard } from './promo-card';
import { LoadingSpinner } from '@/components/shared/loading-spinner';
import { EmptyState } from '@/components/shared/empty-state';
import type { Promotion } from '@/types';

interface PromoGridProps {
  promotions: Promotion[];
  loading?: boolean;
  error?: string | null;
  title?: string;
}

export function PromoGrid({ promotions, loading, error, title }: PromoGridProps) {
  if (loading) {
    return <LoadingSpinner text="Carregando promoções..." />;
  }

  if (error) {
    return (
      <EmptyState
        title="Erro ao carregar"
        description={error}
      />
    );
  }

  if (!promotions.length) {
    return (
      <EmptyState
        title="Nenhuma promoção encontrada"
        description="Tente ajustar os filtros ou voltar mais tarde."
      />
    );
  }

  return (
    <div>
      {title && <h2 className="mb-6 text-2xl font-bold">{title}</h2>}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {promotions.map((promo) => (
          <PromoCard key={promo.id} promotion={promo} />
        ))}
      </div>
    </div>
  );
}
