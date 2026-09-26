'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Select } from '@/components/ui/select';
import { LoadingSpinner } from '@/components/shared/loading-spinner';
import { ScrollText, Search } from 'lucide-react';
import { formatDate } from '@/lib/utils';

interface LogEntry {
  id: string;
  level: 'info' | 'warn' | 'error';
  message: string;
  source: string;
  timestamp: string;
}

const generateLogs = (): LogEntry[] => {
  const levels: LogEntry['level'][] = ['info', 'warn', 'error'];
  const sources = ['api', 'crawler', 'auth', 'system', 'publication'];
  const messages = [
    'Promoção #1234 detectada',
    'Falha ao conectar na Shopee',
    'Usuário admin fez login',
    'Cache atualizado com sucesso',
    'Erro ao publicar no Telegram',
    'Preço atualizado: Produto XYZ',
    'Nova loja integrada: Amazon',
    'Relatório semanal gerado',
    'Timeout na requisição do crawler',
    'Banco de dados sincronizado',
  ];
  const logs: LogEntry[] = [];
  for (let i = 0; i < 50; i++) {
    const date = new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000);
    logs.push({
      id: `log-${i}`,
      level: levels[Math.floor(Math.random() * levels.length)],
      message: messages[Math.floor(Math.random() * messages.length)],
      source: sources[Math.floor(Math.random() * sources.length)],
      timestamp: date.toISOString(),
    });
  }
  return logs.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
};

const levelColors: Record<string, 'destructive' | 'warning' | 'secondary'> = {
  error: 'destructive',
  warn: 'warning',
  info: 'secondary',
};

export default function DashboardLogsPage() {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [level, setLevel] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => {
      setLogs(generateLogs());
      setLoading(false);
    }, 600);
    return () => clearTimeout(timer);
  }, []);

  const filtered = logs.filter((log) => {
    const matchesSearch = log.message.toLowerCase().includes(search.toLowerCase());
    const matchesLevel = !level || log.level === level;
    return matchesSearch && matchesLevel;
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Logs</h1>
        <p className="text-muted-foreground">Registro de eventos do sistema</p>
      </div>

      <div className="flex items-center gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Buscar nos logs..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9"
          />
        </div>
        <Select
          value={level}
          onChange={(e) => setLevel(e.target.value)}
          placeholder="Nível"
          options={[
            { value: '', label: 'Todos' },
            { value: 'info', label: 'Info' },
            { value: 'warn', label: 'Warning' },
            { value: 'error', label: 'Error' },
          ]}
          className="w-36"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            <ScrollText className="h-5 w-5" />
            Eventos
          </CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <LoadingSpinner />
          ) : filtered.length === 0 ? (
            <div className="flex flex-col items-center gap-2 py-8 text-muted-foreground">
              <ScrollText className="h-8 w-8" />
              <p>Nenhum log encontrado</p>
            </div>
          ) : (
            <div className="space-y-2">
              {filtered.map((log) => (
                <div
                  key={log.id}
                  className="flex items-center gap-3 rounded-lg border bg-card px-4 py-2.5 text-sm"
                >
                  <Badge variant={levelColors[log.level]} className="w-14 justify-center text-[10px] uppercase">
                    {log.level}
                  </Badge>
                  <span className="flex-1">{log.message}</span>
                  <span className="text-xs text-muted-foreground">{log.source}</span>
                  <span className="text-xs text-muted-foreground">
                    {formatDate(log.timestamp, 'dd/MM HH:mm')}
                  </span>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
