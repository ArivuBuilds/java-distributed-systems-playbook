# Java and Distributed Systems Playbook

An enterprise-grade architecture and code playbook covering low-level Java 21 internals, SOLID OO design, resilient microservices distributed patterns, edge API architecture, high-scale LLM infrastructure, and systems-focused DSA.

Maintained by: **Arivumani A** ([`@ArivuBuilds(X)`](https://x.com/ArivuBuilds) | [`@ArivuBuilds(LinkedIn)`](https://www.linkedin.com/in/arivubuilds/))

---

## Repository Architecture and Modules

- **[`01-java-internals-mastery/`](01-java-internals-mastery/)** — JMM, AQS, lock-free algorithms, cache-line padding, Project Loom virtual threads, and 7-day SOLID mastery
- **[`02-microservices-patterns-series/`](02-microservices-patterns-series/)** — 12-Day Distributed Systems Architecture Series (Gateway, Discovery, Resilience4j, CQRS, Outbox CDC, Saga, Service Mesh, Strangler Fig)
- **[`03-api-architecture-series/`](03-api-architecture-series/)** — 12-Day API Architecture and Protocols Series (REST, GraphQL, gRPC, WebSockets, Webhooks, SSE, Cursor Pagination, Redis Rate Limiting, Idempotency, JWT)
- **[`04-ai-system-design-series/`](04-ai-system-design-series/)** — 9-Day AI System Design & LLM Infrastructure Series (ONNX, Model Gateways, Layered RAG, SSE, GPU Pools, Event Pipelines, Serverless, Tools, Agents)
- **[`05-dsa-systems-mastery/`](05-dsa-systems-mastery/)** — 12-Pattern Systems DSA, mechanical sympathy, and zero-allocation engines

---

## Module 1: Java Internals & OO Design Mastery (7/7 Completed)

- [x] [`01-memory-model`](01-java-internals-mastery/01-memory-model/) — Java Memory Model (JMM), `volatile`, and happens-before guarantees
- [x] [`02-locks-and-aqs`](01-java-internals-mastery/02-locks-and-aqs/) — `ReentrantLock`, dual `Condition` queues, and concurrency building blocks
- [x] [`03-aqs-internals`](01-java-internals-mastery/03-aqs-internals/) — AbstractQueuedSynchronizer (AQS) state alignment, CAS loops, and FIFO wait-queues
- [x] [`04-lock-free-primitives`](01-java-internals-mastery/04-lock-free-primitives/) — Lock-free FIFO queues with Java 9+ `VarHandle` CAS atomic operations
- [x] [`05-false-sharing-padding`](01-java-internals-mastery/05-false-sharing-padding/) — L1/L2/L3 64-byte cache-line alignment, `@Contended`, and mechanical sympathy
- [x] [`06-virtual-threads-internals`](01-java-internals-mastery/06-virtual-threads-internals/) — Project Loom carrier thread unmounting, monitor pinning diagnostics, and schedulers
- [x] [`07-solid-design-principles`](01-java-internals-mastery/07-solid-design-principles/) — 7-Day Enterprise SOLID Masterclass (`p01_overview` through `p07_capstone`) with Spring Boot dynamic strategy registries and hexagonal ports

---

## Module 2: The 12-Day Microservices Architecture Series (12/12 Completed)

- [x] [**Day 01:** API Gateway Pattern (Ingress routing, path predicates, header filters, and SSL termination)](02-microservices-patterns-series/01-api-gateway-pattern/)
- [x] [**Day 02:** Service Discovery & Registry Pattern (Dynamic routing proxy and service discovery)](02-microservices-patterns-series/02-service-discovery-registry-pattern/)
- [x] [**Day 03:** Backend for Frontend (BFF) Pattern (Client-tailored payload aggregation and parallel fan-out)](02-microservices-patterns-series/03-backend-for-frontend-pattern/)
- [x] [**Day 04:** Circuit Breaker & Fallback Pattern (Resilience4j Closed/Open/Half-Open state machines and fallback degradation)](02-microservices-patterns-series/04-circuit-breaker-pattern/)
- [x] [**Day 05:** Bulkhead Isolation Pattern (Thread-pool vs. Semaphore concurrency isolation preventing cascading failures)](02-microservices-patterns-series/05-bulkhead-isolation-pattern/)
- [x] [**Day 06:** Retry & Exponential Backoff Pattern (Exponential backoff with decorrelated jitter against thundering-herd retries)](02-microservices-patterns-series/06-retry-exponential-backoff-pattern/)
- [x] [**Day 07:** Database-per-Service Pattern (Bounded-context persistence isolation and API-driven data federation)](02-microservices-patterns-series/07-database-per-service-pattern/)
- [x] [**Day 08:** CQRS Projection Pattern (Command-Query Responsibility Segregation with async denormalized read views)](02-microservices-patterns-series/08-cqrs-projection-pattern/)
- [x] [**Day 09:** Transactional Outbox & CDC Pattern (Dual-write elimination via atomic Outbox table, Debezium CDC, and Kafka)](02-microservices-patterns-series/09-transactional-outbox-pattern/)
- [x] [**Day 10:** Saga Orchestration Pattern (Distributed Saga orchestration with deterministic LIFO compensating actions)](02-microservices-patterns-series/10-saga-orchestration-pattern/)
- [x] [**Day 11:** Sidecar & Service Mesh Pattern (Envoy L7 traffic interception, mutual TLS (mTLS), and zero-code observability)](02-microservices-patterns-series/11-sidecar-service-mesh-pattern/)
- [x] [**Day 12:** Strangler Fig Pattern (Incremental zero-downtime monolith migration via edge gateway routing)](02-microservices-patterns-series/12-strangler-fig-pattern/)

---

## Module 3: The 12-Day API Architecture Series (12/12 Completed)

- [x] [**Day 01:** RESTful Resource Modeling (RFC 7807/9457 `ProblemDetail`, URI nouns vs. verbs, HTTP semantics)](03-api-architecture-series/01-rest-resource-modeling/)
- [x] [**Day 02:** GraphQL Query Complexity (DoS mitigation, query depth limiting, `@BatchMapping` N+1 resolution)](03-api-architecture-series/02-graphql-query-complexity/)
- [x] [**Day 03:** gRPC & Protobuf Multiplexing (Proto3 contracts, HTTP/2 multiplexing, unary & streaming RPCs)](03-api-architecture-series/03-grpc-protobuf-multiplexing/)
- [x] [**Day 04:** WebSockets STOMP Messaging (Full-duplex TCP streams, in-memory broker, session interceptors)](03-api-architecture-series/04-websocket-bidirectional-events/)
- [x] [**Day 05:** Webhooks HMAC Security (HMAC-SHA256 signature verification, timing attack & replay defense)](03-api-architecture-series/05-webhooks-hmac-verification/)
- [x] [**Day 06:** Server-Sent Events (SSE) (Unidirectional reactive streaming, backpressure buffer, `Last-Event-ID`)](03-api-architecture-series/06-sse-unidirectional-streaming/)
- [x] [**Day 07:** Keyset & Cursor Pagination ($O(1)$ cursor seeking vs. high-offset degradation (`LIMIT/OFFSET`))](03-api-architecture-series/07-api-pagination-cursor-seek/)
- [x] [**Day 08:** Distributed Rate Limiting (Token Bucket vs. Sliding Window counters via Redis Lua scripts)](03-api-architecture-series/08-rate-limiting-redis-token-bucket/)
- [x] [**Day 09:** Idempotency Keys (Distributed mutation locks, payload hashing, 24-hour deduplication)](03-api-architecture-series/09-idempotency-keys-deduplication/)
- [x] [**Day 10:** API Versioning Strategies (URI path vs. custom header vs. vendor MIME content negotiation)](03-api-architecture-series/10-api-versioning-content-negotiation/)
- [x] [**Day 11:** OAuth 2.0 & JWT Security (Stateless RS256 validation vs. fast-path Redis token blacklist)](03-api-architecture-series/11-oauth2-jwt-revocation-blacklist/)
- [x] [**Day 12:** API Gateway & Edge Ingress (Reverse proxy routing, W3C TraceContext propagation, circuit breakers)](03-api-architecture-series/12-api-gateway-edge-ingress/)

---

## Module 4: AI System Design and LLM Infrastructure (1/9 Published)

- [x] [**Day 01:** Monolithic AI Architectures (In-process ONNX Runtime & Semaphore bulkhead)](04-ai-system-design-series/01-monolithic-ai-onnx-runtime/)
- [x] [**Day 02:** Client-Server and Model Gateways (vLLM, SGLang, TensorRT-LLM decoupling)]
(04-ai-system-design-series/02-client-server-model-gateway/)
- [x] [**Day 03:** Layered RAG Architecture (Semantic chunking, Hybrid RRF, Cross-Encoder scale)]
(04-ai-system-design-series/03-layered-rag-architecture/)
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
- [ ] **Pattern 12:** System Algorithmic Engines (LRU/LFU cache, Consistent Hash ring)

---

## Building and Compiling

To verify and compile the entire playbook:

```bash
mvn clean test -Dnet.bytebuddy.experimental=true
