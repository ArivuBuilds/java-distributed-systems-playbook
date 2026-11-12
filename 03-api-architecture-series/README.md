# 🌐 Module 3: The 12-Day API Architecture Series

A definitive engineering guide to building, securing, and scaling production API infrastructure across REST, GraphQL, gRPC, WebSockets, and SSE in **Java 21** and **Spring Boot 3.3.2**.

---

## 📒 Series Index & Architectural Syllabus

| Day | Module Directory | Topic | Architectural Theme / Trade-off | Status |
|:---:|---|---|---|:---:|
| **01** | [`01-rest-resource-modeling`](./01-rest-resource-modeling) | RESTful Resource Modeling | RFC 7807/9457 `ProblemDetail`, URI nouns vs. verbs, HTTP semantics | `Completed` ✅ |
| **02** | [`02-graphql-query-complexity`](./02-graphql-query-complexity) | GraphQL Query Complexity | DoS mitigation, query depth limiting, `@BatchMapping` N+1 resolution | `Completed` ✅ |
| **03** | [`03-grpc-protobuf-multiplexing`](./03-grpc-protobuf-multiplexing) | gRPC & Protobuf Multiplexing | Proto3 contracts, HTTP/2 multiplexing, unary & streaming RPCs | `Completed` ✅ |
| **04** | [`04-websocket-bidirectional-events`](./04-websocket-bidirectional-events) | WebSockets STOMP Messaging | Full-duplex TCP streams, in-memory broker, session interceptors | `Completed` ✅ |
| **05** | [`05-webhooks-hmac-verification`](./05-webhooks-hmac-verification) | Webhooks HMAC Security | HMAC-SHA256 signature verification, timing attack & replay defense | `Completed` ✅ |
| **06** | [`06-sse-unidirectional-streaming`](./06-sse-unidirectional-streaming) | Server-Sent Events (SSE) | Unidirectional reactive streaming, backpressure buffer, `Last-Event-ID` | `Completed` ✅ |
| **07** | [`07-api-pagination-cursor-seek`](./07-api-pagination-cursor-seek) | Keyset & Cursor Pagination | $O(1)$ cursor seeking vs. high-offset degradation (`LIMIT/OFFSET`) | `Completed` ✅ |
| **08** | `08-rate-limiting-redis-token-bucket` | Distributed Rate Limiting | Token Bucket vs. Sliding Window counters via Redis Lua scripts | `Planned` ⏳ |
| **09** | `09-idempotency-keys-deduplication` | Idempotency Keys | Distributed mutation locks, payload hashing, 24-hour deduplication | `Planned` ⏳ |
| **10** | `10-api-versioning-content-negotiation` | API Versioning Strategies | URI path vs. custom header vs. vendor MIME content negotiation | `Planned` ⏳ |
| **11** | `11-oauth2-jwt-revocation-blacklist` | OAuth 2.0 & JWT Security | Stateless RS256 validation vs. fast-path Redis token blacklist | `Planned` ⏳ |
| **12** | `12-api-gateway-edge-ingress` | API Gateway & Edge Ingress | Reverse proxy routing, W3C TraceContext propagation, circuit breakers | `Planned` ⏳ |

---

## 🌫 Architectural Topology

```mermaid
flowchart TD
    Clients["Web / Mobile / External Clients"] -->|HTTP/1.1, HTTP/2, WS| Gateway["API Gateway & Ingress (Day 12)"]

    Gateway -->|"Rate Limit Check (Lua)"| RateLimit["Redis Token Bucket (Day 08)"]
    Gateway -->|"Verify JWT & Blacklist"| Auth["OAuth2 / JWT Revocation (Day 11)"]

    Gateway -->|"v1/v2 Routing & Negotiation"| Versioning["Versioning Layer (Day 10)"]

    Versioning -->|"Idempotency-Key Lock"| Idempotency["Idempotency Deduplicator (Day 09)"]
    Idempotency -->|"Sync REST / Cursor Page"| RestSvc["REST & Cursor Pagination (Days 01, 07)"]
    Idempotency -->|"Batch Query / Graph"| GraphQLSvc["GraphQL Complexity Engine (Day 02)"]

    Gateway -->|"Binary HTTP/2 Framing"| GrpcSvc["gRPC Protobuf Services (Day 03)"]
    Gateway -->|"TCP Full-Duplex"| WsBroker["WebSocket STOMP Broker (Day 04)"]
    Gateway -->|"text/event-stream"| SseStream["SSE Reactive Telemetry (Day 06)"]
    Clients -->|"Webhook POST Ingress"| WebhookIngress["HMAC-SHA256 Signature Verifier (Day 05)"]