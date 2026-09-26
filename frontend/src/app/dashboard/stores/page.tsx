'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { STORE_THEMES } from '@/lib/constants';
import { Store, ExternalLink } from 'lucide-react';

const storesData = Object.entries(STORE_THEMES).map(([name, theme]) => ({
  name,
  color: theme.primary,
  promotions: Math.floor(Math.random() * 50),
  active: Math.floor(Math.random() * 30),
  conversion: (Math.random() * 10).toFixed(2),
}));

export default function DashboardStoresPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Lojas</h1>
        <p className="text-muted-foreground">Métricas por loja parceira</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {storesData.map((store) => (
          <Card key={store.name}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">{store.name}</CardTitle>
              <div
                className="flex h-8 w-8 items-center justify-center rounded-full"
                style={{ backgroundColor: store.color + '20' }}
              >
                <Store className="h-4 w-4" style={{ color: store.color }} />
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-3 gap-4 text-center">
                <div>
                  <p className="text-2xl font-bold">{store.promotions}</p>
                  <p className="text-xs text-muted-foreground">Promoções</p>
                </div>
                <div>
                  <p className="text-2xl font-bold">{store.active}</p>
                  <p className="text-xs text-muted-foreground">Ativas</p>
                </div>
                <div>
                  <p className="text-2xl font-bold">{store.conversion}%</p>
                  <p className="text-xs text-muted-foreground">Conversão</p>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Detalhamento</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Loja</TableHead>
                <TableHead className="text-right">Promoções</TableHead>
                <TableHead className="text-right">Ativas</TableHead>
                <TableHead className="text-right">Taxa Conv.</TableHead>
                <TableHead className="text-right">Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {storesData.map((store) => (
                <TableRow key={store.name}>
                  <TableCell className="font-medium">{store.name}</TableCell>
                  <TableCell className="text-right">{store.promotions}</TableCell>
                  <TableCell className="text-right">{store.active}</TableCell>
                  <TableCell className="text-right">{store.conversion}%</TableCell>
                  <TableCell className="text-right">
                    <Badge variant="success" className="gap-1">
                      <span className="h-1.5 w-1.5 rounded-full bg-white" />
                      Ativa
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
