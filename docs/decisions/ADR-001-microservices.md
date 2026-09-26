# ADR-001: Adocao de Arquitetura de Microsservicos

**Status:** Aceito
**Data:** 2024-01-15
**Autor:** Equipe de Arquitetura

---

## Contexto

O Atlas Deals tem como objetivo ser uma plataforma SaaS de monitoramento e distribuicao de promocoes. Os requisitos do sistema incluem:

- Coleta de promocoes de centenas de fontes simultaneamente
- Processamento em pipeline com multiplas etapas (validacao, enriquecimento, publicacao)
- Capacidade de adicionar novos canais de coleta e distribuicao sem afetar componentes existentes
- Escalabilidade independente para cada etapa do pipeline
- Deploys frequentes sem downtime
- Times de desenvolvimento independentes

A arquitetura inicial considerou tanto uma abordagem monolítica quanto microsservicos.

---

## Decisao

Optamos por uma arquitetura de microsservicos com as seguintes caracteristicas:

1. **14 servicos independentes**, cada um com responsabilidade unica
2. **Comunicacao assincrona via RabbitMQ** para a pipeline principal
3. **API Gateway (Spring Cloud Gateway)** como ponto unico de entrada para requisicoes síncronas
4. **Banco de dados compartilhado (PostgreSQL)** com schemas separados por servico
5. **Containers Docker** com deploy em Kubernetes
6. **Observabilidade integrada** (Prometheus, Grafana, Loki, Tempo)

### Servicos Criados

| Servico | Responsabilidade |
|---------|-----------------|
| API Gateway | Roteamento, rate limiting, autenticacao |
| Auth | Autenticacao e autorizacao |
| User | Gerenciamento de usuarios |
| Collector | Coleta de promocoes de fontes externas |
| Price History | Historico de precos |
| Promotion Engine | Validacao e classificacao de promocoes |
| Affiliate Engine | Gerenciamento de links de afiliados |
| AI Engine | Enriquecimento com IA (NLP, ML) |
| Publisher | Distribuicao para canais de saida |
| Notification | Notificacoes multicanal |
| Analytics | Processamento de eventos de uso |
| Recommendation | Sistema de recomendacao |
| Search | Busca全文 via OpenSearch |
| Scheduler | Agendamento de tarefas |

---

## Consequencias

### Positivas

- **Escalabilidade granular:** Collector escala durante horarios de coleta, enquanto outros servicos permanecem com recursos normais
- **Isolamento de falhas:** Uma falha no AI Engine nao impede a coleta ou publicacao de promocoes
- **Deploys independentes:** Atualizacoes no Promotion Engine nao exigem rebuild de outros servicos
- **Time autonomy:** Cada servico pode ser mantido por um time diferente
- **Tecnologia flexivel:** Possibilidade de usar diferentes linguagens ou frameworks no futuro

### Negativas

- **Complexidade operacional:** 14 servicos + infraestrutura requerem orquestracao robusta (Kubernetes)
- **Latencia de rede:** Comunicacao entre servicos adiciona latencia comparado a chamadas em processo
- **Consistência eventual:** A pipeline assincrona significa que dados podem ficar inconsistentes por alguns segundos
- **Debug distribuido:** Rastrear uma requisicao atraves de varios servicos exige tracing distribuido
- **Overhead inicial:** Setup da infraestrutura (Kubernetes, service mesh, monitoring) e significativo

### Mitigacoes

- **Kubernetes** para orquestracao e auto-recuperacao
- **OpenTelemetry + Tempo** para tracing distribuido
- **RabbitMQ** com dead letter queues para garantia de entrega
- **API Gateway** para reduzir complexidade no frontend
- **Docker Compose** para desenvolvimento local simplificado

---

## Alternativas Consideradas

### Monolito Modular

**Prós:** Simplicidade operacional, baixa latencia, deploy unico
**Contras:** Escalabilidade limitada, acoplamento entre modulos, risco de falha total
**Rejeitado por:** Impossibilidade de escalar componentes individualmente e risco de downtime generalizado

### Serverless (AWS Lambda)

**Prós:** Escalabilidade automatica, sem gerenciamento de infraestrutura
**Contras:** Cold starts, limite de 15min de execucao, vendor lock-in, custo imprevisivel em escala
**Rejeitado por:** Limite de tempo de execucao inadequado para pipelines longas e cold starts impactariam experiencia do usuario

### Event-Driven com Monolito

**Prós:** Simplicidade do monólito com beneficios de eventos assincronos
**Contras:** Ainda mantem acoplamento no banco de dados, escalabilidade limitada
**Rejeitado por:** Acoplamento de banco de dados impede escalabilidade independente

---

## Referencias

- [ADR Template](https://github.com/joelparkerhenderson/architecture-decision-record)
- [Microservices Pattern - Martin Fowler](https://martinfowler.com/articles/microservices.html)
- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [RabbitMQ Best Practices](https://www.rabbitmq.com/documentation.html)
