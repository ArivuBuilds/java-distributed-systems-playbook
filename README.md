# Java and Distributed Systems Playbook

An enterprise-grade architecture and code playbook covering low-level Java 21 internals, resilient microservices distributed patterns, edge API architecture, high-scale LLM Infrastructure, and systems-focused DSA.

Maintained by: **Arivumani A** (`ArivuBuilds`)

---

## Repository Architecture and Modules

- **[`01-java-internals-mastery/`](01-java-internals-mastery/)** — JMM, AQS, lock-free algorithms, virtual threads
- **[`02-microservices-patterns-series/`](02-microservices-patterns-series/)** — 12-Day Distributed Systems Architecture Series
  - [x] [`08-cqrs-projection-engine/`](02-microservices-patterns-series/08-cqrs-projection-engine/) —��$ CQRS, event-sourcing, denormalized read-views
  - [x] [`09-transactional-outbox-cdc/`](02-microservices-patterns-series/09-transactional-outbox-cdc/) — Transactional Outbox, Debezium CDC, Kafka
  - [x] [`10-saga-orchestrator/`](02-microservices-patterns-series/10-saga-orchestrator/) — Distributed saga orchestration and LIFO compensations
  - [x] [`11-sidecar-and-service-mesh/`](02-microservices-patterns-series/11-sidecar-and-service-mesh/) — Envoy L7 interception, mTLS, and sidecar deployment
- **[`03-api-architecture-series/`](03-api-architecture-series/)** — 12-Day API Architecture and Protocols Series
  - RESTful Modeling, gRPC, WebSockets, SSE, Rate Limiting, Idempotency
- **[`04-ai-system-design/`](04-ai-system-design/)** — 9-Day LLM Infrastructure, vLLM, Semantic Cache
- **[`05-dsa-systems-mastery/`](05-dsa-systems-mastery/)** — 12-Pattern Systems DSA, mechanical sympathy, zero-allocation engines
  - [x] [`01-arrays-and-two-pointers/`](05-dsa-systems-mastery/01-arrays-and-two-pointers/) — In-place sorted telemetry log compaction & deduplication

---

## Module 1: Java Internals Mastery

- [x] [`01-memory-model` - Java Memory Model (JMM), volatile, and happens-before guarantees](01-java-internals-mastery/01-memory-model/)
- [x] [`02-locks-and-aqs` - Synchronization primitives and concurrency building blocks](01-java-internals-mastery/02-locks-and-aqs/)
- [x] [`03-aqs-internals` - AbstractQueuedSynchronizer (AQS) alignment, CAS, and wait-queues](01-java-internals-mastery/03-aqs-internals/)
- [x] [`04-lock-free-primitives` - Lock-free FIFO queues with VarHandle CAS atomic operations](01-java-internals-mastery/04-lock-free-primitives/)
- [x] [`05-false-sharing-paddingc - L1/L2/L3 cache line alignment, false sharing, and mechanical sympathy](01-java-internals-mastery/05-false-sharing-padding/)
- [x] [`06-virtual-threads-internals` - Project Loom carrier thread pinning, continuations, and schedulers](01-java-internals-mastery/06-virtual-threads-internals/)

---

## Module 2: The 12-Day Microservices Architecture Series

- [ ] **Day 01:** API Gateway Pattern (Ingress routing and SSL termination)
- [ ] **Day 02:** Service Discovery and Registry (Client vs. Server-side discovery)
- [ ] **Day 03:** Backend for Frontend (BFF) (Telemetry and response aggregation)
- [ ] **Day 04:** Circuit Breaker and Fallback (Resilience4j state machines)
- [ ] **Day 05:** Bulkhead Isolation Pattern (Thread pool vs. Semaphore isolation)
- [ ] **Day 06:** Retry with Exponential Backoff (Decorrelated jitter mechanisms)
- [ ] **Day 07:** Database-per-Service Pattern (Polyglot persistence and data federation)
- [x] [**Day 08:** CQQR Pattern (Segregating write models from projection stores)](02-microservices-patterns-series/08-cqrs-projection-engine/)
- [x] [**Day 09:** Event-Driven Architecture and Transactional Outbox Pattern (CDC via Debezium and Kafka)](02-microservices-patterns-series/09-transactional-outbox-cdc/)
- [x] [**Day 10:** Saga Pattern (Distributed orchestration vs. choreography with backward compensations)](02-microservices-patterns-series/10-saga-orchestrator/)
- [x] [**Day 11:** Sidecar and Service Mesh (Envoy L7 interception, mTLS, and sidecar deployment)](02-microservices-patterns-series/11-sidecar-and-service-mesh/)
- [ ] [**Day 12:** Strangler Fig Pattern (Incremental zero-downtime monolith migration)](02-microservices-patterns-series/12-strangler-fig/)

---

## Module 3: The 12-Day API Architecture Series

- [ ] **Day 01:** RESTful Resource Modeling (URI nouns, status semantics, HATEOAS)
- [ ] **Day 02:** GraphQL and Query Complexity (Field-level fetching, N+1 query traps)
- [ ] **Day 03:** gRPC and Protobuf (Binary serialization and HTTP/2 multiplexing)
- [ ] **Day 04:** WebSockets (Stateful full-duplex TCP vs. Redis Pub/Sub fanout)
- [ ] **Day 05:** Webhooks Reliability (HMAC-SHA256 signatures and decorrelated retries)
- [ ] **Day 06:** Server-Sent Events (SSE) (Lightweight uni-directional LLM tOken streaming)
- [ ] **Day 07:** API Pagination Patterns (OFFSET 100000 scan tax vs. Keyset/Cursor)
- [ ] **Day 08:** Rate Limiting Algorithms (Token Bucket vs. Sliding Windows via Redis Lua)
- [ ] **Day 09:** Idempotency Keys (Distributed retries and 24-hour deduplication)
- [ ] **Day 10:** API Versioning Strategies (URI Path vs. Custom Header vs. Accept header)
- [ ] **Day 11:** OAuth 2.0 and JWT Security (Stateless bearer tokens vs. JTI revocation blacklists)
- [ ] **Day 12:** API Gateway and Edge Ingress (Reverse proxy, SSL offloading, BFF)

---

## Module 4: AI System Design and LLM Infrastructure

- [ ] **Day 01:** Monolithic AI Architectures (In-process ONNX Runtime vs. HEAP bloat)
- [ ] **Day 02:** Client-Server and Model Gateways (vLLM, SGLang, TensorRT-LLM decoupling)
- [ ] **Day 03:** Layered RAG Architecture (Semantic chunking, Hybrid RRF, Cross-Encoder scale)
- [ ] **Day 04:** MVC in AI Applications (Tool schemas, SSE streams, orchestrators)
- [ ] **Day 05:** Microservices and GPU Pools (Heterogeneous node pools, KV-cache rate limits)
- [ ] **Day 06:** Event-Driven AI Pipelines (Async Kafka/CDC embeddings and backpressure)
- [ ] **Day 07:** Serverless AI & Cold Starts (Warm GPU pools, weight streaming via Firecracker)
- [ ] **Day 08:** Tool Use and Function Calling (Structured JSON schemas, timeout budgets)
- [ ] **Day 09:** Self-Reflection and Agent Loops (Infinite-loop guardrails, FSM, deterministic halting)

---

## Module 5: DSA and Low-Level Systems Mastery

- [x] [**Pattern 01:** Arrays & Two Pointers (In-place log compaction & deduplication)](05-dsa-systems-mastery/01-arrays-and-two-pointers/)
- [ ] **Pattern 02:** Two Pointers (Dynamic throughput buffer & container optimization)
- [ ] **Pattern 03:** Sliding Window (Rate limiting windows & max telemetry metrics)
- [ ] **Pattern 04:** Fast & Slow Pointers (State transition cycle detection)
- [ ] **Pattern 05:** Monotonic Stacks & Queues (Sliding window max latency tracking)
- [ ] **Pattern 06:** Binary Search (Keyset seek & boundary range partition lookups)
- [ ] **Pattern 07:** Trees & Prefix Tries (API Gateway route prefix matching)
- [ ] **Pattern 08:** Heaps & Priority Queues (Distributed K-way log merge & Top-K telemetry)
- [ ] **Pattern 09:** Graphs & Topological Sort (Service boot & Saga rollback dependency DAGs)
- [ ] **Pattern 10:** DP & State Machines (Token replenishment & transaction state models)
- [ ] **Pattern 11:** Disjoint Set Union (DSU) (Split-brain & network partition detection)
- [ ] **Pattern 12:** System Algorithmic Engines (LRU/LFUcache, Consistent Hash ring)

---

## Building and Compiling

To verify and compile the entire playbook:

```bash
mvn clean test
```
