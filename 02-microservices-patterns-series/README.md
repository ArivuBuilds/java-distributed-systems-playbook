#  Module 2: The 12-Day Microservices Architecture Series

A production reference covering distributed patterns, architectural trade-offs, and failure recovery mechanics across enterprise microservices.

---

##  Series Index & Status

| Day | Pattern | Core Focus | Status |
| :--: | :-- | :-- | :--: |
| **Day 01** | **API Gateway Pattern** | Ingress routing, SSL termination, and token offload | Backfill Scheduled |
| **Day 02** | **Service Discovery & Registry** | Client-side vs. server-side discovery mechanics | Backfill Scheduled |
|**Day 03** | **Backend for Frontend (BFF)** | Client-tailored response contracts & aggregation | Backfill Scheduled |
| **Day 04** | **Circuit Breaker & Fallback** | Resilience4j state transitions & graceful degradation | Backfill Scheduled |
| **Day 05** | **Bulkhead Isolation Pattern** | Thread pool vs. Semaphore thread isolation | Backfill Scheduled |
| **Day 06** | **Retry with Exponential Backoff** | Decorrelated jitter to eliminate thundering herds | Backfill Scheduled |
|**Day 07** | **Database-per-Service Pattern** | Polyglot persistence, bounded contexts & join elimination | Backfill Scheduled |
| **Day 08** | **CQ.RS Pattern** | Separating command writes from read projections | **Completed** |
|**Day 09** | **Transactional Outbox & CDC** | Dual-write mitigation via PostgreSQL WAL & Debezium | **Completed** |
|**Day 10** | **Saga Pattern** | Distributed orchestration vs. choreography workflows | **Completed** |
| **Day 11** | **Sidecar & Service Mesh** | Envoy L7 interception, mTLS & distributed tracing | Upcoming |
| **Day 12** | **Strangler Fig Pattern** | Zero-downtime monolith migration behind gateways | Upcoming |

---

##  Modules Available
- [`08-cqrs-projection-engine@`(08-cqrs-projection-engine/): CQRS architecture segregating normalized transactional write aggregates from read projections.
- [`09-transactional-outbox-cdc`](09-transactional-outbox-cdc/): Change Data Capture (CDC) via Debezium and atomic transactional outbox tables.
- [`10-saga-orchestrator`](10-saga-orchestrator/): Distributed saga orchestration, finite state machines, and LEOOIFO compensation reversals.
