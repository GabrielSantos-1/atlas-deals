'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { RevenueChart } from '@/components/dashboard/revenue-chart';
import { PlatformBreakdown } from '@/components/dashboard/platform-breakdown';
import { TopProductsTable } from '@/components/dashboard/top-products-table';
import { useAnalytics } from '@/hooks/use-analytics';
import { StatsCard } from '@/components/dashboard/stats-card';
import { formatCurrency } from '@/lib/utils';
import { DollarSign, MousePointerClick, ShoppingCart, TrendingUp } from 'lucide-react';

export default function AnalyticsPage() {
  const { metrics, topProducts, loading } = useAnalytics();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Analytics</h1>
        <p className="text-muted-foreground">Métricas detalhadas de desempenho</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard icon={<DollarSign className="h-5 w-5" />} label="Receita Total" value={metrics ? formatCurrency(metrics.totalRevenue) : 'R$ 0'} change={metrics?.revenueChange} loading={loading} />
        <StatsCard icon={<MousePointerClick className="h-5 w-5" />} label="Cliques" value={metrics ? metrics.totalClicks.toLocaleString() : '0'} change={metrics?.clickChange} loading={loading} />
        <StatsCard icon={<ShoppingCart className="h-5 w-5" />} label="Conversões" value={metrics ? metrics.totalConversions.toLocaleString() : '0'} loading={loading} />
        <StatsCard icon={<TrendingUp className="h-5 w-5" />} label="Taxa de Conversão" value={metrics ? `${(metrics.averageConversionRate * 100).toFixed(2)}%` : '0%'} loading={loading} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Receita</CardTitle>
          </CardHeader>
          <CardContent>
            <RevenueChart />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Plataformas</CardTitle>
          </CardHeader>
          <CardContent>
            <PlatformBreakdown />
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Produtos</CardTitle>
        </CardHeader>
        <CardContent>
          <TopProductsTable products={topProducts} loading={loading} />
        </CardContent>
      </Card>
    </div>
  );
}
