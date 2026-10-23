# 🌐 Java & Distributed Systems Playbook

An enterprise-grade architecture and code playbook covering low-level Java 21 internals, resilient microservices distributed patterns, edge API architecture, and high-scale LLM Infrastructure.

Maintained by: **ArivuBuilds** (`@ArivuBuilds`)

---

## 📚 Repository Architecture & Modules

```text
main root
├── 01-java-internals-mastery/          # JMM, AQS, lock-free algorithms, virtual threads
├── 02-microservices-patterns-series/   # 12-Day Distributed Systems Architecture Series
│   ├── [x] 08-cqrs-projection-engine/  # CQRS, event-sourcing, denormalized read-views
│   ├── [x] 09-transactional-outbox-cdc/# Transactional Outbox, Debezium CDC, Kafka
│   └── [x] 10-saga-orchestrator/       # Distributed saga orchestration & LIFO compensations
├── 03-api-architecture-series/         # 12-Day API Architecture & Protocols Series
│   └── Resource Modeling, gRPC, WebSockets, SSE, Rate Limiting, Idempotency
└── 04-ai-system-design/                # 9-Day LLM Infrastructure, vLLM, Semantic Cache
