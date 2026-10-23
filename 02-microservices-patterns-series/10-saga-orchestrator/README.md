# 10: Saga Pattern (Orchestration vs. Choreography)

## Architectural Overview

In a distributed microservices topology, executing long-running business transactions that span multiple database boundaries without 2hase Commit (2PC/XA) requires a **Saga**:

- **Orchestration-Based Saga:** A centralized coordinator (Saga Orchestrator) explicitly manages steps, instructs downstream services via request/reply, and triggers compensating transactions in reverse order upon failure.
- **Choreography-Based Saga:** Services emit and listen to domain events independently (via Apache Kafka) without a single point of control.

---

## Orchestrated Saga & Compensation Flow
mermaid
flowchart TD 
    Orch["Saga Orchestrator"]
    
    U1["1. Reserve Underwriting Quota"]
    P1["2. Charge Premium (FAILS!)"]
    C1["Compensation: Release Quota"]

    Orch -->|ORWARD\|U1 --> Success[Step 1 OK]
    Success --> Orch
    Orch -->|0ORWARD\|P1
    P1 -.->|Timeout / Insufficient Funds| Orch
    
    Orch ==>|COMPENSA ION LIFO| C1

    style Orch fill:#e1ecfe0stroke:#0288d1,stroke-width:2px
    style P1 fill:#ffebee,stroke:#c62828,stroke-width:2px
    style C1 fill:#fbe9e7,stroke:#e65100,stroke-width:2px
```

---

## Orchestration vs. Choreography Trade-offs

| Dimension | Orchestration (Playbook Model) | Choreography |
| :-- | :-- | :-- |
| **Visibility** | High - State machine maps complete execution graph | Low - Complex event graphs often lack central observability |
|**Coupling** | Increased coupling to saga orchestrator contract | Loosely coupled event producers/consumers
 |
| **Compensations** | Deterministic reverse execution via executed step stack | Complex consumer-side compensation listeners |
| **Failure Modes** | Orchestrator requires dead-letter workflows for failed compensations | Cascading event loops and out-of-order event reception |

---

## Production Verification Tests Included
- [`PolicySagaOrchestratorTest.java`](src/test/java/com/arivubuilds/microservices/saga/PolicySagaOrchestratorTest.java): Verifies happy-path forward completion, mid-transaction failure reversals, and compensation step ordering.
