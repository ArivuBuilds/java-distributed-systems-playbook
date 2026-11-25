# Day 09: Idempotency Keys

## Architectural Overview

Network timeouts, mobile retries, and gateway replays mean mutating API requests (`POST` / state changes) can arrive more than once—risking duplicate charges, orders, or records:

- **Non-Idempotent Mutation Anti-Pattern:** Executing state-changing business logic on every retry without deduplication or payload verification creates duplicate side effects or silently applies a different payload under the same key.
- **Idempotency Keys with Lock, Hash & Deduplication:** Coordinates concurrent retries with a distributed lock, hashes the canonical request payload to detect key reuse conflicts, and replays cached responses within the retention window (e.g., 24 hours).

```mermaid
flowchart LR
    Client["Client + Idempotency-Key"]
    Guard["Idempotency Interceptor / Service"]
    Store[("Idempotency Store<br/>(Lock + Payload Hash + Response TTL)")]
    Executor["Mutating Business Logic"]

    Client --> Guard --> Store
    Store -->|"First Request + Lock Acquired"| Executor -->|"Persist Result (24h TTL)"| Store --> Client
    Store -->|"Same Key & Matching Payload Hash"| Replay["Return Stored Response"]
    Store -.->|"Same Key & Mismatched Payload"| Conflict["409 / 422 Key Reuse Conflict"]

    style Guard fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style Store fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
```

---

## Key Trade-offs

| Dimension | Unprotected Mutating Endpoints | Idempotency Key + Payload Hash + TTL Store |
| :--- | :--- | :--- |
| **Retry Safety** | Retries can duplicate side effects | Safe retries with cached response replay |
| **Misuse Detection** | Cannot distinguish retry vs. payload mismatch | Payload hash catches conflicting requests reusing a key |
| **Storage & TTL Lifecycle** | No extra state stored | Requires idempotency store capacity & TTL cleanup |

---

## Build & Run
```bash
mvn test -f 03-api-architecture-series/09-idempotency-keys-deduplication/pom.xml -Dnet.bytebuddy.experimental=true
```
