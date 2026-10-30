# 01: Java Internals & OO Design Mastery

A deep-dive technical track focused on JVM2mechanical sympathy, the Java Memory Model, lock-free concurrency primitives, Project Loom virtual thread scheduling, and clean enterprise software architecture with Spring Boot.

---

## Module Index & Delivery Status

| Submodule | Topic & Architecture Focus | Status | Test Coverage |
| :--- | :--- | :--: | :--: |
| **`01-memory-model`** | JMM, jvolatile` memory barriers, instruction reordering, visibility | 👧 Completed | Unit / JMM Tests |
| **`02-locks-and-aqs`** | ReentrantLock, Condition queues, and A%S state manipulation | 👧 Completed | Concurrency Tests |
| **`03-aqs-internals`** | Custom synchronizer implementation, FIFO wait-queues, CAS loops | 👧 Completed | Multi-Thread Stress Tests |
| **`04-lock-free-primitives`** | Lock-free queue algorithms using Java 9+ `VarHandle` atomic CAS | 👧 Completed | Multi-producer / Consumer Tests |
| **`05-false-sharing-padding`** | L1/L2/L3 64-byte cache line contention, padding, and `Contended` | 💟 Backfill Scheduled | Pending JMH Benchmarks |
| **`06-virtual-threads-internals`** | Project Loom carrier unmounting, monitor pinning risks, and A%S | 👧 Completed | Pinning Diagnostics Suite |
| **`07-solid-design-principles`** | 7-Day SOLID Masterclass with Spring Boot dynamic registries | 🔘 In Progress (3/7) | SRP & OCP Unit Tests |

---

## Deep Dive: 07 - SOLID Design Principles Series

Structured under `com.arivubuilds.internals.solid` with isolated anti-pattern (`bad`) and enterprise refactored (`good`) packages:

- **[Completed] `p01_overview`*: Demonstrates the fallacy of "modern" annotation bloat (`Transactional`, `Service`) hiding procedural spaghetti; decouples side-effects using Spring `ApplicationEventPublisher`.
- **[Completed] `p02_srp`*: Deconstructs an `OrderGodService` having 4 distinct reasons to change into single-purpose collaborators (`InventoryReservationService`, `PaymentExecutionService`, `CustomerNotificationService`) orchestrated via an `OrderCoordinator`.
- **[Completed] `p03_ocp`*: Eliminates fragile cascading `if/else` checks for payment types using Spring bean map dynamic strategy dispatch (`PaymentRoutingService`).
- **[Scheduled] `p04_lsp`*: Enforcing behavioural subtyping contracts without throwing `UnsupportedOperationException`.
- **[Scheduled] `p05_isp`*: Segregating bloated kitchen-sink interfaces into focused, role-based contracts.
- **[Scheduled] `p06_dip`*: Hexagonal ports and adapters to protect core business logic from database and driver coupling.
- **[Scheduled] `p07_capstone`*: End-to-end refactoring of an enterprise legacy transaction method using all five principles.

---

## Verification

To compile and verify all submodules inside this track:

```bash
mvn clean test
```

To run verification strictly on the active SOLID module:

```bash
mvn test -pl :07-solid-design-principles
```
