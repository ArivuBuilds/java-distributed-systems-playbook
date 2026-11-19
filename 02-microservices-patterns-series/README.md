#  Module 2: The 12-Day Microservices Architecture Series

A production reference covering distributed patterns, architectural trade-offs, and failure recovery mechanics across enterprise microservices.

---

##  Series Index & Status

| Day | Pattern | Core Focus | Status |
| :--: | :-- | :-- | :--: |
| **01** | **API Gateway Pattern** | Ingress routing, SSL termination, and token offload | `Completed` ✅ |
| **02** | **Service Discovery & Registry** | Client-side vs. server-side discovery mechanics | `Completed` ✅ |
| **03** | **Backend for Frontend (BFF)** | Client-tailored response contracts & aggregation | `Completed` ✅ |
| **04** | **Circuit Breaker & Fallback** | Resilience4j state transitions & graceful degradation | `Completed` ✅ |
| **05** | **Bulkhead Isolation Pattern** | Thread pool vs. Semaphore thread isolation | `Completed` ✅ |
| **06** | **Retry with Exponential Backoff** | Decorrelated jitter to eliminate thundering herds  | `Completed` ✅ |
| **07** | **Database-per-Service Pattern** | Polyglot persistence, bounded contexts & join elimination | Backfill Scheduled ⏳ |
| **08** | **CQ.RS Pattern** | Separating command writes from read projections | `Completed` ✅ |
| **09** | **Transactional Outbox & CDC** | Dual-write mitigation via PostgreSQL WAL & Debezium | `Completed` ✅ |
| **10** | **Saga Pattern** | Distributed orchestration vs. choreography workflows | `Completed` ✅ |
| **11** | **Sidecar & Service Mesh** | Envoy L7 interception, mTLS & distributed tracing | `Completed` ✅ |
| **12** | **Strangler Fig Pattern** | Zero-downtime monolith migration behind gateways | `Completed` ✅ |

---

##  Modules Available
- [`08-cqrs-projection-engine`](08-cqrs-projection-engine/): CQRS architecture segregating normalized transactional write aggregates from read projections.
- [`09-transactional-outbox-cdc`](09-transactional-outbox-cdc/): Change Data Capture (CDC) via Debezium and atomic transactional outbox tables.
- [`10-saga-orchestrator`](10-saga-orchestrator/): Distributed saga orchestration, finite state machines, and LEOOIFO compensation reversals.
