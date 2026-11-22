powershell -NoProfile -Command "$md = @'
# 02: The 12-Day Microservices Architecture Series

Production-grade distributed systems patterns built with Java 21, Spring Boot 3.3.2, Spring Cloud, Resilience4j, Apache Kafka, Debezium CDC, and Envoy Service Mesh.

## Module Index & Delivery Status (12/12 Completed)

| Day | Submodule | Architecture Pattern & Focus | Status |
| :--- | :--- | :--- | :--- |
| **Day 01** | [`01-api-gateway-pattern`](01-api-gateway-pattern/) | Ingress routing, path predicates, header filters, and SSL termination | Completed ✅ |
| **Day 02** | [`02-service-discovery-registry-pattern`](02-service-discovery-registry-pattern/) | Service discovery & dynamic routing proxy (`01-legacy-monolith-service`, `02-strangler-routing-proxy`, `03-modern-order-service`) | Completed ✅ |
| **Day 03** | [`03-backend-for-frontend-bff`](03-backend-for-frontend-bff/) | Client-tailored payload aggregation and parallel fan-out | Completed ✅ |
| **Day 04** | [`04-circuit-breaker-fallback`](04-circuit-breaker-fallback/) | Resilience4j Closed/Open/Half-Open state machines and fallback degradation | Completed ✅ |
| **Day 05** | [`05-bulkhead-isolation-pattern`](05-bulkhead-isolation-pattern/) | Thread-pool vs. Semaphore concurrency isolation preventing cascading failures | Completed ✅ |
| **Day 06** | [`06-retry-exponential-backoff`](06-retry-exponential-backoff/) | Exponential backoff with decorrelated jitter against thundering-herd retries | Completed ✅ |
| **Day 07** | [`07-database-per-service-pattern`](07-database-per-service-pattern/) | Bounded-context persistence isolation and API-driven data federation | Completed ✅ |
| **Day 08** | [`08-cqrs-projection-engine`](08-cqrs-projection-engine/) | Command-Query Responsibility Segregation with async denormalized read views | Completed ✅ |
| **Day 09** | [`09-transactional-outbox-cdc`](09-transactional-outbox-cdc/) | Dual-write elimination via atomic Outbox table, Debezium CDC, and Kafka | Completed ✅ |
| **Day 10** | [`10-saga-orchestrator`](10-saga-orchestrator/) | Distributed Saga orchestration with deterministic LIFO compensating actions | Completed ✅ |
| **Day 11** | [`11-sidecar-and-service-mesh`](11-sidecar-and-service-mesh/) | Envoy L7 traffic interception, mutual TLS (mTLS), and zero-code observability | Completed ✅ |
| **Day 12** | [`12-strangler-fig`](12-strangler-fig/) | Incremental zero-downtime monolith migration via edge gateway routing | Completed ✅ |

## Build & Verification

```bash
mvn clean test -f 02-microservices-patterns-series/pom.xml