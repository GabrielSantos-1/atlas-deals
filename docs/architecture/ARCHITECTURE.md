# Arquitetura do Atlas Deals

## Visao Geral

O Atlas Deals e uma plataforma SaaS de monitoramento e distribuicao de promocoes construida sobre uma arquitetura de microsservicos. A comunicacao entre servicos e predominantemente assincrona, utilizando RabbitMQ como message broker, garantindo desacoplamento, resiliência e escalabilidade horizontal.

---

## Decisoes Arquiteturais

### Microsservicos vs Monolito

Optamos por microsservicos pelos seguintes motivos:

- **Escalabilidade independente:** Cada servico pode escalar conforme sua demanda especifica (ex: Collector precisa de mais recursos durante horarios de coleta)
- **Isolamento de falhas:** Um bug no Promotion Engine nao derruba o Gateway ou o Frontend
- **Times autononos:** Cada servico pode ser mantido por times diferentes
- **Deploys independentes:** Atualizacoes de um servico nao exigem rebuild de toda a plataforma
- **Tecnologia flexivel:** Cada servico pode usar a melhor ferramenta para seu proposito

### Comunicacao Assincrona (Event-Driven)

A pipeline de processamento de promocoes e totalmente orientada a eventos:

```
Coleta → Preco → Promocao → Afiliado → IA → Publicacao
```

Cada etapa publica eventos no RabbitMQ que sao consumidos pela etapa seguinte. Isso permite:
- Processamento em background sem bloquear requisicoes HTTP
- Filas de espera que absorvem picos de carga
- Reprocessamento automatico em caso de falha
- Rastreabilidade completa via OpenTelemetry

---

## Componentes

### Gateway (`atlas-deals-gateway`)

**Spring Cloud Gateway** atuando como ponto unico de entrada. Responsabilidades:
- Roteamento para servicos internos
- Rate limiting (Redis-based)
- Autenticacao JWT (delegada ao Auth Service)
- Agregacao de respostas para o frontend
- Circuit breaker (Resilience4j)

### Auth Service (`atlas-deals-auth`)

Gerenciamento de identidade e acesso:
- Login/registro com JWT + refresh tokens
- OAuth2 para provedores externos (Google, GitHub)
- Roles e permissoes (RBAC)
- Cache de sessoes no Redis

### User Service (`atlas-deals-user`)

Gerenciamento de usuarios e preferencias:
- CRUD de usuarios
- Preferencias de notificacao
- Historico de atividades

### Collector (`atlas-deals-collector`)

Responsavel pela coleta de promocoes de multiplas fontes:
- Web scraping de sites parceiros
- API de redes de afiliados (Awin, Lomadee, etc.)
- Feeds RSS/XML de cupons
- Processamento paralelo com backpressure

### Price History (`atlas-deals-price-history`)

Armazenamento e consulta de historico de precos:
- Serie temporal de precos por produto
- Deteccao de variacao de precos
- Suporte a queries temporais

### Promotion Engine (`atlas-deals-promotion-engine`)

Motor de regras para classificacao e validacao de promocoes:
- Regras configuráveis por cliente
- Deduplicacao de ofertas
- Validacao de periodo de vigencia
- Pontuacao de qualidade da promocao

### Affiliate Engine (`atlas-deals-affiliate-engine`)

Gerenciamento de links de afiliados:
- Geracao de links trackeados
- Atribuicao de comissoes
- Integracao com redes de afiliados
- Relatorios de performance

### AI Engine (`atlas-deals-ai-engine`)

Enriquecimento inteligente das promocoes:
- Categorizacao automatica (NLP)
- Extracao de entidades (produto, marca, loja)
- Deteccao de sentimentos em reviews
- Geracao de descricoes otimizadas para SEO
- Recomendacao de precos via ML

### Publisher (`atlas-deals-publisher`)

Distribuicao do conteudo para canais de saida:
- APIs REST para parceiros
- Webhooks configuráveis
- Exportacao em lote (CSV, JSON, XML)
- Sincronizacao com plataformas parceiras

### Notification (`atlas-deals-notification`)

Notificacoes multicanal:
- Email (SendGrid)
- Push notifications
- WebSocket para o frontend
- Templates customizaveis

### Analytics (`atlas-deals-analytics`)

Processamento de eventos de uso:
- Pipeline de eventos via RabbitMQ
- Agregacoes em tempo real
- Metricas de engajamento
- Exportacao para data warehouse

### Recommendation (`atlas-deals-recommendation`)

Sistema de recomendacao:
- Colaborativo (usuarios similares)
- Baseado em conteudo (categorias)
- Híbrido (ensemble dos dois modelos)
- Atualizacao periodica dos modelos

### Search (`atlas-deals-search`)

Busca全文 sobre promocoes:
- Indexacao no OpenSearch
- Busca facetada
- Filtros por categoria, preco, loja
- Sugestoes de busca (autocomplete)
- Busca semantica via embeddings

### Scheduler (`atlas-deals-scheduler`)

Agendamento de tarefas:
- Coletas periodicas
- Reindexacao de dados
- Limpeza de dados obsoletos
- Geracao de relatorios

---

## Fluxo de Dados

### Pipeline Principal

```
1. Scheduler dispara coleta
2. Collector busca promocoes das fontes
3. Evento "promotion.collected" publicado no RabbitMQ
4. Price History registra o preco atual
5. Evento "price.recorded" publicado
6. Promotion Engine valida e classifica
7. Affiliate Engine enriquece com links
8. AI Engine categoriza e otimiza
9. Publisher distribui para os canais
10. Notification avisa usuarios relevantes
```

### Pipeline Secundaria (Analytics)

```
1. Eventos de uso sao publicados via Gateway
2. Analytics consome e processa
3. Recommendation atualiza modelos
4. Search reindexa dados
```

---

## Banco de Dados

### PostgreSQL (Primario)

Cada servico tem seu proprio schema no PostgreSQL, seguindo o padrao **database-per-service**:

- `gateway_*` - Tabelas de roteamento e rate limiting
- `auth_*` - Usuarios, roles, sessoes
- `collector_*` - Fontes, configs de coleta
- `promotion_*` - Promocoes, regras, categorias
- `affiliate_*` - Links, comissoes, redes
- `analytics_*` - Eventos, metricas agregadas

### Redis (Cache/Sessao)

- Cache de respostas do Gateway (TTL: 60s)
- Sessoes JWT armazenadas
- Rate limiting counters (Sliding Window)
- Lock distribuido para schedulers

### OpenSearch (Busca)

- Index de promocoes para busca全文
- Indices separados por tenant
- Snapshot diario para backup

---

## Seguranca

### Autenticacao e Autorizacao

1. **Login:** O usuario autentica via Auth Service
2. **JWT:** Token JWT assinado com chave RSA
3. **Gateway:** Valida o token em cada requisicao
4. **Servicos:** Confiam no Gateway (token propagado via header)
5. **Refresh:** Tokens de longa duracao armazenados no Redis

### Rate Limiting

- **Global:** 1000 req/min por IP
- **Authenticated:** 5000 req/min por usuario
- **Endpoints criticos:** 100 req/min (ex: login, registro)

### Criptografia

- Dados em repouso: Criptografia AES-256 no PostgreSQL (pgcrypto)
- Dados em trânsito: TLS 1.3 entre todos os servicos
- Segredos: Hashicorp Vault (ou env vars criptografadas)

---

## Observabilidade

### Metricas (Prometheus)

Cada servico expoe metricas no formato Prometheus em `/actuator/prometheus`:
- Taxa de requisicoes (HTTP e eventos)
- Latencia (p50, p95, p99)
- Taxa de erro
- Utilizacao de recursos (CPU, memoria, thread pool)
- Status de conexoes (DB, Redis, RabbitMQ)

### Logs (Loki)

Logs estruturados no formato JSON, enviados via Loki:
- Correlation ID (rastreamento entre servicos)
- Nivel do log (ERROR, WARN, INFO, DEBUG)
- Componente e classe de origem
- `@timestamp` padrao ISO 8601

### Tracing (Tempo/Tempo)

Tracing distribuido via OpenTelemetry:
- Cada requisicao HTTP gera um trace
- Eventos do RabbitMQ sao vinculados ao trace pai
- Visualizacao de dependencias entre servicos
- Identificacao de gargalos de performance

### Dashboards (Grafana)

Dashboards pre-configurados:
1. **Visao Geral:** Saude geral do sistema
2. **Pipeline:** Status da pipeline de promocoes
3. **Servicos:** Metricas individuais por servico
4. **Infraestrutura:** Uso de recursos (CPU, RAM, disco)

---

## Resiliência

### Circuit Breaker (Resilience4j)

- Aplicado em chamadas síncronas entre servicos
- Configuracao: 50% de falhas em 10 requisicoes abre o circuito
- Half-open apos 30 segundos
- Fallback: retorno de dados em cache ou resposta parcial

### Retry com Backoff

- Retry automatico em operacoes de I/O (DB, Redis, RabbitMQ)
- Exponential backoff com jitter (100ms, 200ms, 400ms, 800ms)
- Maximo de 4 tentativas

### Dead Letter Queue (RabbitMQ)

Mensagens que falham apos 3 tentativas sao movidas para DLQ, permitindo:
- Auditoria de falhas
- Reprocessamento manual
- Alertas automaticos para mensagens na DLQ

### Health Checks

- Liveness: Verifica se o servico esta vivo (simples)
- Readiness: Verifica dependencias (DB, Redis, RabbitMQ)
- /actuator/health exposto para Kubernetes probes
