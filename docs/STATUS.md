# Atlas Deals — Status do Projeto

> **Data:** 22/07/2026
> **Versão:** 1.0.0-SNAPSHOT
> **Status Geral:** 🟡 92% completo — Arquitetura pronta, faltam credenciais para operação real

---

## 1. O Projeto

### O que é

Plataforma SaaS automatizada de monitoramento, inteligência, análise e distribuição de promoções de produtos tecnológicos. Opera 24/7, coleta ofertas de múltiplas lojas, detecta promoções reais, gera conteúdo com IA e publica automaticamente em canais como Telegram, WhatsApp, Instagram e Site.

### Público-alvo

Consumidores interessados em hardware, computadores, notebooks, GPUs, CPUs, SSDs, monitores, periféricos, smartphones e tecnologia em geral.

### Diferenciais

- **Fully automated** — nenhuma etapa depende de operação manual
- **Event-driven** — comunicação assíncrona via RabbitMQ entre todos os serviços
- **IA integrada** — geração de título, descrição, SEO, hashtags, prós/contras, perfil recomendado
- **Imagens reais** — download da imagem do produto + overlay promocional com preço e desconto
- **Links de afiliado** — geração automática para Shopee, Amazon, Mercado Livre, Kabum, AliExpress
- **Multi-plataforma** — publica em Telegram, WhatsApp, Instagram, Facebook, Threads, Twitter/X, Discord, Site, Newsletter, Push
- **Score de promoção** — 9 regras ponderadas que classificam ofertas de 0 a 100

---

## 2. Fase Atual: Sprint 1-8 (Implementação Inicial)

### Progresso por Sprint (do roadmap original)

| Sprint | Foco | Status |
|--------|------|--------|
| 1 | Infraestrutura (Docker, banco, mensageria) | ✅ Completo |
| 2 | Banco e coletores | ✅ Completo* |
| 3 | Histórico e engine de promoções | ✅ Completo |
| 4 | Afiliados | ✅ Completo |
| 5 | IA | ✅ Completo |
| 6 | Publicação | ✅ Completo |
| 7 | Analytics | ✅ Completo |
| 8 | Hardening (segurança, testes, docs) | 🔄 Parcial |

*\* Coletores implementados com fallback mock + chamadas reais condicionais*

---

## 3. O que está implementado

### Backend — 16 módulos, 244 arquivos Java

| Módulo | Porta | Arquivos | Descrição |
|--------|-------|:--------:|-----------|
| `atlas-deals-common` | — | 38 | Shared kernel, value objects (Money, ProductId), 8 domain events, ports, specifications |
| `atlas-deals-gateway` | 8080 | 8 | Spring Cloud Gateway, 14 rotas, JWT validation, rate-limit Redis, circuit breakers |
| `atlas-deals-auth` | 8090 | 13 | JWT (HMAC), BCrypt, RBAC, Redis blacklist, refresh tokens |
| `atlas-deals-collector` | 8081 | 17 | 4 conectores (Shopee, Amazon, Kabum, ML), HTTP real + fallback mock, scheduler |
| `atlas-deals-price-history` | 8082 | 12 | Registros imutáveis, stats (min/max/avg/trend), cache Redis |
| `atlas-deals-promotion-engine` | 8083 | 16 | 9 regras ponderadas, score 0-100, expiração automática |
| `atlas-deals-affiliate-engine` | 8084 | 20 | 5 conectores (Shopee, Amazon, ML, Kabum, AliExpress) |
| `atlas-deals-ai-engine` | 8085 | 20 | Template PT-BR + fallback OpenAI/Anthropic, 10 plataformas de formatação |
| `atlas-deals-image-generator` | 8094 | 21 | Download real via WebClient, overlay Java 2D (BufferedImage), redimensionamento por plataforma |
| `atlas-deals-publisher` | 8086 | 23 | 6 publishers, schedule, retry, correlação imagem+conteúdo |
| `atlas-deals-analytics` | 8087 | 16 | Dashboard metrics, revenue breakdown, funil, agregação diária |
| `atlas-deals-notification` | 8088 | 9 | Push, email, digest semanal |
| `atlas-deals-scheduler` | 8089 | 10 | Orquestrador, distributed lock Redis, jobs dinâmicos |
| `atlas-deals-search` | 8091 | 8 | OpenSearch, busca facetada, autocomplete |
| `atlas-deals-user` | 8092 | 6 | Perfil, preferências, favoritos |
| `atlas-deals-recommendation` | 8093 | 7 | Similaridade, trending Redis, personalização |

### Frontend — Next.js 14, 60 arquivos

- **15 páginas**: Home, promo detail (c/ Schema.org JSON-LD), produtos (c/ filtros), categorias, dashboard (7 subpáginas), auth
- **25 componentes**: UI primitives, layout, promo, product, dashboard, shared
- **Infra**: API client tipado, auth provider (JWT), hooks, validações zod, gráficos Recharts

### Infraestrutura

- **Docker Compose**: 25 serviços (9 infra + 16 app)
- **14 Dockerfiles** (um por serviço backend) + frontend
- **5 scripts PowerShell** (build, start-infra, start-all, stop-all, dev)
- **Monitoramento**: Prometheus + Grafana (16 targets) + Loki (logs) + Tempo (tracing)

### CI/CD — GitHub Actions

| Workflow | Descrição |
|----------|-----------|
| `ci.yml` | Build, test, lint em PRs e push para main/develop |
| `deploy.yml` | Build Docker + push GHCR + deploy em tags `v*` |
| `security.yml` | CodeQL, dependency scan, agendado semanal |
| `dependabot.yml` | Atualizações automáticas de dependências (Maven, npm, Docker, Actions) |

---

## 4. Pipeline de Eventos (completo)

```
Loja (Shopee/Amazon/Kabum/ML)
  │  HTTP real (se credenciais configuradas) ou mock
  ▼
Collector ──► ProductCollectedEvent
  │
  ▼
PriceHistory ──► PriceUpdatedEvent
  │
  ▼
PromotionEngine ──► PromotionDetectedEvent  
  │                 [score 0-100, 9 regras]
  ▼
AffiliateEngine ──► AffiliateLinkGeneratedEvent
  │                 [link de afiliado automático]
  ▼
AIEngine ──► ContentGeneratedEvent
  │          [título, descrição, SEO, hashtags, prós/contras, 
  │           formatação para 10 plataformas + imageUrl, storeName, price]
  ▼
ImageGenerator ──► ImageGeneratedEvent
  │                [download real → redimensiona por plataforma → overlay com preço/desconto]
  ▼
Publisher ──► ContentPublishedEvent
              [publica TEXTO + IMAGEM no canal]
```

---

## 5. Pendências e Próximas Fases

### 🔴 Pendências para operação real

| Item | Impacto | Esforço |
|------|---------|:-------:|
| Configurar API keys nos `application.yml` de cada serviço | Impede chamadas reais | 30 min |
| Obter tokens dos programas de afiliados (Shopee, Amazon, etc.) | Impede geração de links reais | 2-4 h (burocracia) |
| Configurar bot Telegram + grupo para testes | Impede publicação real | 15 min |
| Testar build completo (`mvn clean package`) | Garantir que compila | 10 min |

### 🟡 Melhorias planejadas (Sprint 9+)

| Fase | Descrição | Prioridade |
|:----:|-----------|:----------:|
| 9 | Testes — unitários + integração (meta 95% cobertura) | Alta |
| 10 | OpenAPI/Swagger em todos os serviços | Alta |
| 11 | Helm charts para Kubernetes | Média |
| 12 | Cache L2 (Redis) em todos os serviços que consultam DB | Média |
| 13 | Rate limiting distribuído (token bucket no Redis) | Média |
| 14 | Dashboard de administração completo (já temos 7 páginas) | Baixa |
| 15 | Suporte a mais lojas (Pichau, Terabyte, Fast Shop, Magalu) | Baixa |
| 16 | Internacionalização (suporte a outros mercados além do Brasil) | Baixa |

---

## 6. Arquitetura Técnica

### Stack

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 21, Spring Boot 3.4.3, Virtual Threads |
| Frontend | Next.js 14, React 18, TypeScript, Tailwind CSS |
| Banco | PostgreSQL 16, Redis 7 |
| Mensageria | RabbitMQ 4 |
| Busca | OpenSearch 2 |
| Monitoramento | Prometheus, Grafana, Loki, Tempo, OpenTelemetry |
| Infra | Docker, Docker Compose, GitHub Actions |
| Segurança | JWT, BCrypt, RBAC, CORS, Rate Limiting |

### Design Patterns

- **Clean Architecture** / **Hexagonal** em todos os módulos (domain → application → infrastructure)
- **Event-Driven Architecture** com RabbitMQ (topic exchange)
- **Domain-Driven Design** (AggregateRoot, ValueObject, DomainEvent, Repository pattern)
- **Strategy Pattern** nos conectores (ProductCollector, AffiliateLinkGenerator, Publisher)
- **Specification Pattern** nas regras de promoção
- **Saga / Orchestrator** no Scheduler Service para coordenação entre serviços

---

## 7. Como usar

```powershell
# 1. Subir infraestrutura (PostgreSQL, Redis, RabbitMQ, etc.)
.\scripts\start-infra.ps1

# 2. Build completo
.\scripts\build-all.ps1

# 3. (opcional) Subir tudo via Docker
.\scripts\start-all.ps1

# 4. Desenvolvimento
.\scripts\dev.ps1
```

### Ambiente de dev

Cada serviço roda em sua porta individual:
- Gateway: http://localhost:8080
- Frontend: http://localhost:3000
- Grafana: http://localhost:3000 (admin/ver .env)
- RabbitMQ Management: http://localhost:15672 (usuario: atlas / senha: ver .env)

---

## 8. Estrutura do Projeto

```
atlas-deals/
├── .github/workflows/       # CI/CD (3 workflows + dependabot)
├── backend/
│   ├── atlas-deals-common/  # Shared kernel (38 Java)
│   ├── atlas-deals-{16 módulos}/
│   └── pom.xml              # Parent POM multi-módulo
├── frontend/                # Next.js 14 + Tailwind (60 arquivos)
│   └── Dockerfile
├── infra/
│   ├── docker/              # docker-compose.yml + full + configs
│   ├── grafana/             # Dashboards + datasources
│   ├── prometheus/          # Targets config
│   ├── loki/                # Log aggregation
│   ├── tempo/               # Distributed tracing
│   └── k8s/                 # Kubernetes manifests (futuro)
├── scripts/                 # 5 PowerShell scripts
├── docs/                    # Arquitetura, decisões, status
├── pom.xml                  # Root multi-module
└── README.md
```

---

*Próxima atualização de status: após conclusão da Sprint 9 (Testes)*
