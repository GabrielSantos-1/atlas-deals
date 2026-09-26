'use client';

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { api } from '@/lib/api';
import { formatCurrency } from '@/lib/utils';
import { LoadingSpinner } from '@/components/shared/loading-spinner';
import type { Product } from '@/types';
import { Search, Plus } from 'lucide-react';

export default function DashboardProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const fetch = async () => {
      setLoading(true);
      try {
        const result = await api.getPromotions({ size: 50 });
        const uniqueProducts = result.data.map((p) => ({
          id: p.productId,
          title: p.title,
          description: '',
          currentPrice: p.currentPrice,
          originalPrice: p.originalPrice,
          currency: 'BRL',
          imageUrl: p.imageUrl,
          storeName: p.storeName,
          category: p.category,
          brand: '',
          model: '',
          inStock: p.inStock,
          url: '',
          discountPercentage: p.discountPercentage,
        }));
        setProducts(uniqueProducts.filter((p, i, a) => a.findIndex((x) => x.id === p.id) === i));
      } catch {
        // fallback
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  const filtered = products.filter((p) =>
    p.title.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Produtos</h1>
          <p className="text-muted-foreground">Catálogo de produtos monitorados</p>
        </div>
        <Button className="gap-2">
          <Plus className="h-4 w-4" />
          Novo Produto
        </Button>
      </div>

      <div className="relative max-w-sm">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Buscar produtos..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-9"
        />
      </div>

      <div className="rounded-lg border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Produto</TableHead>
              <TableHead>Preço</TableHead>
              <TableHead>Desconto</TableHead>
              <TableHead>Loja</TableHead>
              <TableHead>Disponível</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} className="py-8"><LoadingSpinner /></TableCell>
              </TableRow>
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-8 text-muted-foreground">
                  Nenhum produto encontrado
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((product) => (
                <TableRow key={product.id}>
                  <TableCell className="font-medium max-w-[300px] truncate">{product.title}</TableCell>
                  <TableCell>{formatCurrency(product.currentPrice)}</TableCell>
                  <TableCell>{product.discountPercentage > 0 ? `${product.discountPercentage}%` : '-'}</TableCell>
                  <TableCell>{product.storeName}</TableCell>
                  <TableCell>
                    <Badge variant={product.inStock ? 'success' : 'destructive'}>
                      {product.inStock ? 'Sim' : 'Não'}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
