import { cn } from '@/lib/utils';

interface DiscountBadgeProps {
  percentage: number;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

export function DiscountBadge({ percentage, className, size = 'md' }: DiscountBadgeProps) {
  const sizeClasses = {
    sm: 'text-[10px] px-1.5 py-0.5',
    md: 'text-xs px-2 py-1',
    lg: 'text-sm px-3 py-1.5',
  };

  const colorClass =
    percentage >= 40
      ? 'bg-green-500 text-white'
      : percentage >= 20
        ? 'bg-yellow-500 text-white'
        : percentage >= 5
          ? 'bg-red-500 text-white'
          : 'bg-gray-500 text-white';

  return (
    <span
      className={cn(
        'inline-flex items-center rounded font-bold leading-none',
        sizeClasses[size],
        colorClass,
        className,
      )}
    >
      -{percentage}%
    </span>
  );
}
