import { cn } from '@/lib/utils';
import type { Product } from '@/types';

interface ProductSpecsProps {
  product: Product;
  className?: string;
}

export function ProductSpecs({ product, className }: ProductSpecsProps) {
  const specs = [
    { label: 'Marca', value: product.brand },
    { label: 'Modelo', value: product.model },
    { label: 'Categoria', value: product.category },
    { label: 'Loja', value: product.storeName },
    { label: 'Disponibilidade', value: product.inStock ? 'Em estoque' : 'Indisponível' },
  ].filter((s) => s.value);

  if (!specs.length) return null;

  return (
    <div className={cn('rounded-lg border', className)}>
      <div className="divide-y">
        {specs.map((spec) => (
          <div key={spec.label} className="flex justify-between px-4 py-3 text-sm">
            <span className="text-muted-foreground">{spec.label}</span>
            <span className="font-medium">{spec.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
