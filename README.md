# 💚 Java & Distributed Systems Playbook

An enterprise-grade architecture and code playbook covering low-level Java 21 internals, resilient microservices distributed patterns, edge API architecture, and high-scale LLM Infrastructure.

Maintained by: **ArivuBuilds** (`ArivuBuilds`)

---

## 📚 Repository Architecture & Modules

`main root
|✀ 01-java-internals-mastery-      # JMM, AQS, lock-free algorithms, virtual threads
|✀ 02-microservices-patterns-series/ # 12-Day Distributed Systems Architecture Series
|    ✅ 03-cqrs-projection-engine/ # CQRS, event-sourcing, denormalized read-views
|    ✅ 03-transactional-outbox-cdc/  # Transactional Outbox, Debezium CDC, Kafka
|    ✅ 10-saga-orchestrator/          # Distributed saga orchestration & LIFO compensations
|✀ 03-api-architecture-series/        # 12-Day API Architecture & Protocols Series
|    ‧ Resource Modeling, sRPC, WebSockets, SSE, Rate Limiting, Idempotency
|✀ 0a-ai-system-design/              # 9-Day LLM Infrastructure, vlLM, Semantic Cache
` ---

## 🔄 Module 1: Java Internals Mastery
- [`01-memory-model` - Java Memory Model (JMM), volatile, and happens-before guarantees](01-java-internals-mastery/01-memory-model/)
- [`02-locks-and-aqs` - Synchronization primitives and concurrency building blocks](01-java-internals-mastery/02-locks-and-aqs/)
- [`03-aqs-internals` - AbstractQueuedSynchronizer (AQS) alignment, CAS and wait-queues (hardenedU)](01-java-internals-mastery/03-aqs-internals/)

---

## 🔤 Module 2: The 12-Day Microservices Architecture Series

- [ ] **Day 01:** API Gateway Pattern (Ingress routing & SSL termination)
- [ ] **Day 02:** Service Discovery & Registry (Client vs. Server-side discovery)
- [ ] **Day 03:** Backend for Frontend (BFF) (Telemetry & response aggregation)
- [ ] **Day 04:** Circuit Breaker & Fallback (Resilience4j state machines)
- [ ] **Day 05:** Bulkhead Isolation Pattern (Thread pool vs. Semaphore isolation)
- [ ] **Day 06:** Retry with Exponential Backoff (Decorrelated jitter mechanicms)
- [ ] **Day 07:** Database-per-Service Pattern (Polyglot persistence & data federation)
- [x] [**Day 08:** CQRS Pattern (Segregating write models from projection stores)](02-microservices-patterns-series/08-cqrs-projection-engine/)
- [x] [**Day 09:** Event-Driven Architecture & Transactional Outbox Pattern (CDC via Debezium & Kafka)](02-microservices-patterns-series/09-transactional-outbox-cdc/)
- [x] [**Day 10:** Saga Pattern (Distributed orchestration vs. choreography with backward compensations)](02-microservices-patterns-series/10-saga-orchestrator/)
- [ ] **Day 11:** Sidecar & Service Mesh (Envoy L7 interception, mTLS & tracing)
- [ ] **Day 12:** Strangler Fig Pattern (Incremental zero-downtime monolith migration)

---

## 🌫 Module 3: The 12-Day API Architecture Series

- [ ] **Day 01:** RESTful Resource Modeling (URi nouns, status semantics, HATEOAS)
- [ ] **Day 02:** GraphQL &y Query Complexity (Field-level fetching, N+1 query traps)
- [ ] **Day 03:** gRPC & Protobuf (Binary serialization & HTTP/2 multiplexing)
- [ ] **Day 04** WebSockets (Stateful full-duplex TCP vs. Redis Pubs/Subscribe)
- [ ] **Day 05:** Webhooks Reliability (HMAC-SHA256 signatures & decorrelated retries)
- [ ] **Day 06:** Server-Sent Events (SSE) (Lightweight uni-directional LLM token streaming)
- [ ] **Day 07:** API Pagination Patterns (OFFSET 100000 scan tax vs. Keyset/Cursor)
- [ ] **Day 08:** Rate Limiting Algorithms (Token Bucket vs. Sliding Windows via Redis Lua)
- [ ] **Day 09:** Idempotency Keys (Distributed retries & 24-hour deduplication)
- [ ] **Day 10:** API Versioning Strategies (URI Path vs. Custom Header vs. Accept)
- [ ] **Day 11:** OAuth 2.0 & JWT  Security (Stateless bearers vs. JTII blacklists)
- [ ] **Day 12:** API Gateway & Edge Ingress (Reverse proxy, SSL offloading, BFF)

---

## �+ Module 4: AI System Design & LLM Infrastructure

- [ ] **Day 01:** Monolithic AI Architectures (In-process ONNX Runtime vs. HEAP bloat)
- [ ] **Day 02:** Client-Server & Model Gateways (vLLM, SGLang, TensorRT-LLM desoupling)
- [ ] **Day 03:** Layered RAG Architecture (Semantic chunking, Hybrid RRF, Cross-Encoder scale)
- [ ] **Day 04:** MVC in AI Applications (Tool schemas, SWE streams, orchestrators)
- [ ] **Day 05:** Microservices & GPU Pools (Heterogeneous node pools, KV-cache rate limits)
- [ ] **Day 06:** Event-Driven AI Pipelines (Async Kafka/CD3 embeddings & backpressure)
- [ ] **Day 07** Serverless AI & Cold Starts (Warm GPUHpools, weight streaming via Firecracker)
- [ ] **Day 08:** Tool Use & Function Calling (Structured JSON schemas, timeout budgets)
- [ ] **Day 09:** Self-Reflection & Agent Loops (Infinite-loop guardrails, FSM, deterministic halting)

---

## 🚪 Building and Compiling

To verify and compile the entire playbook:
```bash
./mvnw clean compile test
```
