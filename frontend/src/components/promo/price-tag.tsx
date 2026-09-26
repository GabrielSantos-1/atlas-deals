import { formatCurrency } from '@/lib/utils';
import { cn } from '@/lib/utils';

interface PriceTagProps {
  currentPrice: number;
  originalPrice?: number;
  currency?: string;
  className?: string;
}

export function PriceTag({ currentPrice, originalPrice, currency = 'BRL', className }: PriceTagProps) {
  return (
    <div className={cn('flex flex-col', className)}>
      {originalPrice && originalPrice > currentPrice && (
        <span className="text-sm text-muted-foreground line-through">
          {formatCurrency(originalPrice, currency)}
        </span>
      )}
      <span className="text-xl font-bold text-foreground">
        {formatCurrency(currentPrice, currency)}
      </span>
    </div>
  );
}
