'use client';

import type { TopProduct } from '@/types';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { formatCurrency } from '@/lib/utils';
import { Skeleton } from '@/components/ui/skeleton';

interface TopProductsTableProps {
  products: TopProduct[];
  loading?: boolean;
}

export function TopProductsTable({ products, loading }: TopProductsTableProps) {
  if (loading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 5 }).map((_, i) => (
          <Skeleton key={i} className="h-10 w-full" />
        ))}
      </div>
    );
  }

  if (!products.length) {
    return <p className="text-sm text-muted-foreground py-4 text-center">Nenhum dado disponível</p>;
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Produto</TableHead>
          <TableHead className="text-right">Cliques</TableHead>
          <TableHead className="text-right">Conversões</TableHead>
          <TableHead className="text-right">Receita</TableHead>
          <TableHead className="text-right">Taxa Conv.</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {products.map((product) => (
          <TableRow key={product.id}>
            <TableCell className="font-medium max-w-[200px] truncate">{product.title}</TableCell>
            <TableCell className="text-right">{product.clicks.toLocaleString()}</TableCell>
            <TableCell className="text-right">{product.conversions.toLocaleString()}</TableCell>
            <TableCell className="text-right">{formatCurrency(product.revenue)}</TableCell>
            <TableCell className="text-right">{(product.conversionRate * 100).toFixed(2)}%</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
