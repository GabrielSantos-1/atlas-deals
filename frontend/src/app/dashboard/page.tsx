'use client';

import { useAnalytics } from '@/hooks/use-analytics';
import { StatsCard } from '@/components/dashboard/stats-card';
import { RevenueChart } from '@/components/dashboard/revenue-chart';
import { TopProductsTable } from '@/components/dashboard/top-products-table';
import { PlatformBreakdown } from '@/components/dashboard/platform-breakdown';
import { RecentPromotions } from '@/components/dashboard/recent-promotions';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCurrency } from '@/lib/utils';
import { DollarSign, MousePointerClick, ShoppingCart, Megaphone } from 'lucide-react';

export default function DashboardPage() {
  const { metrics, topProducts, recentPromotions, loading } = useAnalytics();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Visão Geral</h1>
        <p className="text-muted-foreground">Dashboard administrativo do Atlas Deals</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          icon={<DollarSign className="h-5 w-5" />}
          label="Receita Total"
          value={metrics ? formatCurrency(metrics.totalRevenue) : 'R$ 0'}
          change={metrics?.revenueChange}
          loading={loading}
        />
        <StatsCard
          icon={<MousePointerClick className="h-5 w-5" />}
          label="Total de Cliques"
          value={metrics ? metrics.totalClicks.toLocaleString() : '0'}
          change={metrics?.clickChange}
          loading={loading}
        />
        <StatsCard
          icon={<ShoppingCart className="h-5 w-5" />}
          label="Conversões"
          value={metrics ? metrics.totalConversions.toLocaleString() : '0'}
          change={metrics ? metrics.averageConversionRate * 100 : undefined}
          loading={loading}
        />
        <StatsCard
          icon={<Megaphone className="h-5 w-5" />}
          label="CTR Médio"
          value={metrics ? `${(metrics.averageCTR * 100).toFixed(2)}%` : '0%'}
          loading={loading}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Receita (30 dias)</CardTitle>
          </CardHeader>
          <CardContent>
            <RevenueChart />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Distribuição por Plataforma</CardTitle>
          </CardHeader>
          <CardContent>
            <PlatformBreakdown />
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Produtos Mais Vendidos</CardTitle>
        </CardHeader>
        <CardContent>
          <TopProductsTable products={topProducts} loading={loading} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Promoções Recentes</CardTitle>
        </CardHeader>
        <CardContent>
          <RecentPromotions promotions={recentPromotions} loading={loading} />
        </CardContent>
      </Card>
    </div>
  );
}
