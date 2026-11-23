# 05: Bulkhead Isolation Pattern (Concurrency Compartmentalization)

## Architectural Overview

Inspired by watertight ship hull compartments, the Bulkhead Pattern prevents a single slow or misbehaving downstream integration from exhausting shared JVM thread pools or connection resources:

- **Shared Pool Starvation Anti-Pattern:** When all endpoints share one Tomcat/Jetty worker pool, a slow third-party underwriting or analytics service consumes every thread, starving fast health checks and critical checkout endpoints.
- **Compartmentalized Quotas:** Each downstream dependency receives a strictly bounded concurrency quota via **Semaphore Bulkheads** (in-thread concurrency cap) or **Fixed Thread-Pool Bulkheads** (dedicated queue + worker pool).

```mermaid
flowchart LR
    Ingress["Incoming HTTP Traffic"]

    subgraph BulkheadCompartments ["Isolated Concurrency Bulkheads"]
        FastPool["Critical Payments Bulkhead<br/>(Max Concurrent = 20)"]
        SlowPool["External Credit Bureau Bulkhead<br/>(Max Concurrent = 5, Queue = 2)"]
    end

    PaySvc["Payment Gateway"]
    CreditSvc["Slow Credit API (2s Latency)"]
    Reject["BulkheadFullException<br/>(HTTP 429 / Fallback)"]

    Ingress --> FastPool --> PaySvc
    Ingress --> SlowPool --> CreditSvc
    SlowPool -.->|"6th Concurrent Call Rejected"| Reject

    style FastPool fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    style SlowPool fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    style Reject fill:#ffebee,stroke:#c62828,stroke-width:2px
```

---

## Key Trade-offs

| Dimension | Semaphore Bulkhead | Thread-Pool Bulkhead |
| :--- | :--- | :--- |
| **Execution Thread** | Runs on caller thread (zero context-switch overhead) | Offloads to dedicated bounded worker pool + queue |
| **Timeout Enforcement** | Relies on underlying non-blocking/socket timeout | Supports true async `TimeLimiter` cancellation |
| **Memory & CPU Overhead** | Minimal (atomic permit counter) | Higher (extra thread stack memory + context switching) |
| **Best Fit** | Reactive/Virtual-Thread apps & low-latency calls | Legacy blocking I/O & untrusted third-party APIs |

---

## Production Verification Tests Included
- [`BulkheadIsolationIntegrationTest.java`](src/test/java/com/arivubuilds/microservices/bulkhead/BulkheadIsolationIntegrationTest.java): Concurrent load test verifying that saturating the slow dependency bulkhead rejects excess calls immediately while leaving critical endpoints unaffected.

## Build & Run
```bash
mvn clean test -f 02-microservices-patterns-series/05-bulkhead-isolation-pattern/pom.xml -Dnet.bytebuddy.experimental=true
```
