'use client';

import { useState, useEffect } from 'react';
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
import { Button } from '@/components/ui/button';
import { PLATFORMS } from '@/lib/constants';
import { api } from '@/lib/api';
import { formatRelativeTime } from '@/lib/utils';
import { LoadingSpinner } from '@/components/shared/loading-spinner';
import { Send, RefreshCw } from 'lucide-react';
import { toast } from 'sonner';
import type { Publication } from '@/types';

const statusColors: Record<string, 'success' | 'destructive' | 'warning' | 'secondary'> = {
  published: 'success',
  failed: 'destructive',
  pending: 'warning',
  scheduled: 'secondary',
};

export default function DashboardPublicationsPage() {
  const [publications, setPublications] = useState<Publication[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchPublications = async () => {
    setLoading(true);
    try {
      const data = await api.getPublications();
      setPublications(data);
    } catch {
      toast.error('Erro ao carregar publicações');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPublications();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Publicações</h1>
          <p className="text-muted-foreground">Histórico de publicações nas plataformas</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={fetchPublications} className="gap-2">
            <RefreshCw className="h-4 w-4" />
            Atualizar
          </Button>
          <Button className="gap-2">
            <Send className="h-4 w-4" />
            Nova Publicação
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Histórico</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <LoadingSpinner />
          ) : publications.length === 0 ? (
            <div className="flex flex-col items-center gap-2 py-8 text-muted-foreground">
              <Send className="h-8 w-8" />
              <p>Nenhuma publicação encontrada</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Plataforma</TableHead>
                  <TableHead>Post ID</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Publicado em</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {publications.map((pub) => {
                  const platform = PLATFORMS.find((p) => p.id === pub.platform);
                  return (
                    <TableRow key={pub.id}>
                      <TableCell className="font-medium">
                        <div className="flex items-center gap-2">
                          <div
                            className="h-2.5 w-2.5 rounded-full"
                            style={{ backgroundColor: platform?.color || '#6366f1' }}
                          />
                          {platform?.label || pub.platform}
                        </div>
                      </TableCell>
                      <TableCell className="font-mono text-sm">{pub.platformPostId || '-'}</TableCell>
                      <TableCell>
                        <Badge variant={statusColors[pub.status] || 'secondary'}>
                          {pub.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {pub.publishedAt ? formatRelativeTime(pub.publishedAt) : '-'}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
