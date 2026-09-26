'use client';

import { useState } from 'react';
import { usePromotions } from '@/hooks/use-promotions';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Select } from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Pagination } from '@/components/shared/pagination';
import { DiscountBadge } from '@/components/promo/discount-badge';
import { formatCurrency, formatRelativeTime } from '@/lib/utils';
import { api } from '@/lib/api';
import { CheckCircle2, XCircle, Search } from 'lucide-react';
import { toast } from 'sonner';
import type { Promotion } from '@/types';

function StatusBadge({ status }: { status: Promotion['status'] }) {
  const map = {
    active: { label: 'Ativa', variant: 'success' as const },
    expired: { label: 'Expirada', variant: 'destructive' as const },
    pending: { label: 'Pendente', variant: 'warning' as const },
  };
  const info = map[status];
  return <Badge variant={info.variant}>{info.label}</Badge>;
}

export default function DashboardPromotionsPage() {
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const filters = {
    search: search || undefined,
    ...(status ? { status: status as Promotion['status'] } : {}),
    size: 20,
  };

  const { promotions, totalPages, page, loading, error, setPage, refetch } = usePromotions(filters);

  const handleApprove = async (id: string) => {
    setActionLoading(id);
    try {
      await api.approvePromotion(id);
      toast.success('Promoção aprovada');
      refetch();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Erro ao aprovar');
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (id: string) => {
    setActionLoading(id);
    try {
      await api.rejectPromotion(id);
      toast.success('Promoção rejeitada');
      refetch();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Erro ao rejeitar');
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Promoções</h1>
          <p className="text-muted-foreground">Gerencie as promoções do sistema</p>
        </div>
        <Button>Nova Promoção</Button>
      </div>

      <div className="flex items-center gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Buscar..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9"
          />
        </div>
        <Select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          placeholder="Status"
          options={[
            { value: '', label: 'Todos' },
            { value: 'active', label: 'Ativas' },
            { value: 'pending', label: 'Pendentes' },
            { value: 'expired', label: 'Expiradas' },
          ]}
          className="w-40"
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
              <TableHead>Status</TableHead>
              <TableHead>Detectada</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                  Carregando...
                </TableCell>
              </TableRow>
            ) : error ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-destructive">
                  {error}
                </TableCell>
              </TableRow>
            ) : promotions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                  Nenhuma promoção encontrada
                </TableCell>
              </TableRow>
            ) : (
              promotions.map((promo) => (
                <TableRow key={promo.id}>
                  <TableCell className="font-medium max-w-[250px] truncate">{promo.title}</TableCell>
                  <TableCell>{formatCurrency(promo.currentPrice)}</TableCell>
                  <TableCell>
                    <DiscountBadge percentage={promo.discountPercentage} size="sm" />
                  </TableCell>
                  <TableCell>{promo.storeName}</TableCell>
                  <TableCell><StatusBadge status={promo.status} /></TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {formatRelativeTime(promo.detectedAt)}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-1">
                      {promo.status === 'pending' && (
                        <>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleApprove(promo.id)}
                            loading={actionLoading === promo.id}
                          >
                            <CheckCircle2 className="h-4 w-4 text-green-500" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleReject(promo.id)}
                            loading={actionLoading === promo.id}
                          >
                            <XCircle className="h-4 w-4 text-red-500" />
                          </Button>
                        </>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {!loading && !error && (
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      )}
    </div>
  );
}
