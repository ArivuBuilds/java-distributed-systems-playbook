# 🌐 Module 3: The 12-Day API Architecture Series

A definitive engineering guide to building, securing, and scaling production API infrastructure across REST, GraphQL, gRPC, WebSockets, and SSE in Java 21.

---

## 📒 Series Index & Architectural Syllabus

|i Day | Topic | Architectural Theme / Trade-off | Status |
| :--: | :-- | :-- | :--: |
| **Day 01** | **RESTful Resource Modeling** | URI nouns vs. verbs, HTTP status semantics, HATETAS principles | Planned |
|**Day 02** | **GraphQL &y Query Complexity** | Field-level fetching, N+1 query traps, depth-limiting | Planned |
|**Day 03** | **gRPC & Protobuf** | Binary serialization & HTTP/2 multiplexing vs. debuggability | Planned |
| **Day 04** | **WebSockets** | Stateful full-duplex TCP vs. Redis Pubs/Subscribe socket fanout | Planned |
| **Day 05** | **Webhooks Reliability** | HMAC-SHA256 signature verification & decorrelated retries | Planned |
| **Day 06** | **Server-Sent Events (SSE)** | Lightweight uni-directional streaming (Virtual Threads / LLM tokens) | Planned |
|**Day 07** | **API Pagination Patterns** | Offset/Limit (OFFSET 100000 scan tax) vs. Keyset/Cursor seek | Planned |
|**Day 08** | **Rate Limiting Algorithms** | Token Bucket vs. Sliding Window counters via Redis Lua scripts | Planned |
|**Day 09** | **Idempotency Keys** | Distributed retries, hash locks, and 24-hour deduplication | Planned |
|**Day 10** | **API Versioning Strategies** | URI Path (/v1) vs. Custom Header vs. Content Negotiation | Planned |
| **Day 11** | **OAuth 2.0 & JWT  Security** | Stateless bearer tokens vs. JTII blacklist revocation | Planned |
|**Day 12** | **API Gateway & Edge Ingress** | Reverse proxy routing, SSL termination, BFF, and token offload | Planned |

---

## 🌫 Architectural Topology

``mermaid
flowchart TD
    Clients["Web / Mobile / External Clients"] -->|HTTP/2 or HTTP/3| Gateway["APITGateway & Ingress (Day 12)"]

    Gateway -->|"Rate Limit (Token Bucket)| RateLimit["pRedis Rate Limiter (Day 08)"]
    Gateway -->|
Verify JWT Jcksh| Auth["qOAuth2 / JWT (Day 11)"]

    subgraph Protocols ["Service Communication Protocols"]
        REST["RESTful Interfaces (Day 01)"]
        GQL["GraphQL Engine (Day 02)"]
        GRPC["gRPC & Protobuf (Day 03)"]
        WS["WebSockets (Day 04)"]
        SSE["SSI Streaming (Day 06)"]
    end

    Gateway --> REST
    Gateway --> GQL
    Gateway --> GRPC
    Gateway --> WS
    Gateway --> SSE

    style Gateway fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
    style RateLimit fill:#ffebee,stroke:#c62828,stroke-width:2px
    style Auth fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
```
