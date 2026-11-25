# Day 01: RESTful Resource Modeling

## Architectural Overview

Well-structured REST APIs model domain entities as nouns rather than RPC verbs, use standard HTTP methods and status codes consistently, and report errors in a predictable machine-readable structure:

- **Ad-Hoc Error & Verb-URI Anti-Pattern:** Exposing `/getOrder`, `/createPayment`, or returning inconsistent ad-hoc JSON error bodies forces clients into brittle custom parsing and breaks HTTP caching and uniform interface semantics.
- **Resource-Oriented REST with RFC 7807/9457 `ProblemDetail`:** Models canonical resource collections and sub-resources with proper HTTP status semantics (`200`, `201`, `204`, `400`, `404`, `409`, `422`) and standardized `ProblemDetail` error payloads.

```mermaid
flowchart LR
    Client["API Consumer / Client"]
    Controller["REST Resource Controller<br/>(Nouns & HTTP Semantics)"]
    Domain["Domain & Validation Layer"]
    Problem["RFC 7807/9457 ProblemDetail<br/>(Standardized Error Envelope)"]

    Client -->|"HTTP Request (GET/POST/PUT/PATCH/DELETE)"| Controller --> Domain
    Domain -->|"Success + Status Code"| Client
    Domain -.->|"Validation / Domain Error"| Problem --> Client

    style Controller fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style Problem fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
```

---

## Key Trade-offs

| Dimension | RPC-Style / Ad-Hoc REST | Resource-Oriented REST + `ProblemDetail` |
| :--- | :--- | :--- |
| **Client Ergonomics** | Endpoint proliferation & custom error shapes | Predictable resource URIs & uniform `ProblemDetail` errors |
| **HTTP Semantics & Caching** | Inconsistent status codes & cacheability | Leverages HTTP status codes, headers & idempotent verbs |
| **Complex Mutations / Queries** | Awkward multi-resource or graph queries | Better paired with GraphQL, gRPC, or Saga workflows |

---

## Build & Run
```bash
mvn test -f 03-api-architecture-series/01-rest-resource-modeling/pom.xml -Dnet.bytebuddy.experimental=true
```
