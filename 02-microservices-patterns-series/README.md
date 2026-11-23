# 02: The 12-Day Microservices Architecture Series

Production-grade distributed systems patterns built with Java 21, Spring Boot 3.3.2, Spring Cloud, Resilience4j, Apache Kafka, Debezium CDC, and Envoy Service Mesh.

## Module Index & Delivery Status (12/12 Completed)

| Day | Submodule | Architecture Pattern & Focus | Status |
| :--- | :--- | :--- | :--- |
| **01** | [`01-api-gateway-pattern`](01-api-gateway-pattern/) | Ingress routing, path predicates, header filters, and SSL termination | Completed ✅ |
| **02** | [`02-service-discovery-registry-pattern`](02-service-discovery-registry-pattern/) | Service discovery & dynamic routing proxy (`01-legacy-monolith-service`, `02-strangler-routing-proxy`, `03-modern-order-service`) | Completed ✅ |
| **03** | [`03-backend-for-frontend-pattern`](03-backend-for-frontend-pattern/) | Client-tailored payload aggregation and parallel fan-out | Completed ✅ |
| **04** | [`04-circuit-breaker-pattern`](04-circuit-breaker-pattern/) | Resilience4j Closed/Open/Half-Open state machines and fallback degradation | Completed ✅ |
| **05** | [`05-bulkhead-isolation-pattern`](05-bulkhead-isolation-pattern/) | Thread-pool vs. Semaphore concurrency isolation preventing cascading failures | Completed ✅ |
| **06** | [`06-retry-exponential-backoff-pattern`](06-retry-exponential-backoff-pattern/) | Exponential backoff with decorrelated jitter against thundering-herd retries | Completed ✅ |
| **07** | [`07-database-per-service-pattern`](07-database-per-service-pattern/) | Bounded-context persistence isolation and API-driven data federation | Completed ✅ |
| **08** | [`08-cqrs-projection-pattern`](08-cqrs-projection-pattern/) | Command-Query Responsibility Segregation with async denormalized read views | Completed ✅ |
| **09** | [`09-transactional-outbox-pattern`](09-transactional-outbox-pattern/) | Dual-write elimination via atomic Outbox table, Debezium CDC, and Kafka | Completed ✅ |
| **10** | [`10-saga-orchestration-pattern`](10-saga-orchestration-pattern/) | Distributed Saga orchestration with deterministic LIFO compensating actions | Completed ✅ |
| **11** | [`11-sidecar-service-mesh-pattern`](11-sidecar-service-mesh-pattern/) | Envoy L7 traffic interception, mutual TLS (mTLS), and zero-code observability | Completed ✅ |
| **12** | [`12-strangler-fig-pattern`](12-strangler-fig-pattern/) | Incremental zero-downtime monolith migration via edge gateway routing | Completed ✅ |

## Build & Verification

```bash
# Build & test all Java / Spring Boot modules (Days 01–10)
mvn clean test -f 02-microservices-patterns-series/pom.xml -Dnet.bytebuddy.experimental=true

# Run infrastructure & mesh labs (Days 11–12)
docker compose -f 02-microservices-patterns-series/11-sidecar-service-mesh-pattern/docker-compose.yml config
docker compose -f 02-microservices-patterns-series/12-strangler-fig-pattern/docker-compose.yml config