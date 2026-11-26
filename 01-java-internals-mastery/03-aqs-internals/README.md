# 03: AQS Internals & Custom Synchronizers

## Architectural Overview

Building custom latches, semaphores, or bounded synchronizers on top of `AbstractQueuedSynchronizer` requires careful coordination between atomic `state` transitions and the FIFO wait queue:

- **Broken Spin / Ad-Hoc Wait-Queue Anti-Pattern:** Hand-rolling custom wait lists or spinning without backoff/parking wastes CPU cycles, risks lost wakeups, or violates memory ordering.
- **AQS FIFO Queue & CAS State Loops:** Delegates queue management, node parking/unparking, and cancellation handling to AQS while implementing clean `tryAcquire`/`tryRelease` (or shared-mode) CAS transitions on `state`.

```mermaid
flowchart LR
    Caller["Worker Thread"]
    CAS["CAS Loop on AQS state<br/>(compareAndSetState)"]
    Queue["AQS FIFO Wait Queue<br/>(Park / Unpark / Cancel)"]
    Resource["Guarded Resource / Permit"]

    Caller --> CAS
    CAS -->|"Fast-Path Win"| Resource
    CAS -.->|"Contended"| Queue -->|"Unparked & Retried"| CAS

    style CAS fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style Queue fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
```

---

## Key Trade-offs

| Dimension | Custom Ad-Hoc Synchronizer / Spin Loop | AQS-Backed Synchronizer (`state` + FIFO Queue) |
| :--- | :--- | :--- |
| **Queue & Wakeup Correctness** | High risk of race bugs, lost signals & spin burn | Battle-tested FIFO queuing, park/unpark & cancellation |
| **Flexibility** | Tailored to a single bespoke pattern | Supports exclusive (`tryAcquire`) and shared (`tryAcquireShared`) modes |
| **Implementation Complexity** | Deceptively tricky under concurrency stress | Requires clear `state` invariants & mode semantics |

---

## Build & Run

```bash
mvn test -f 01-java-internals-mastery/03-aqs-internals/pom.xml -Dnet.bytebuddy.experimental=true
```
