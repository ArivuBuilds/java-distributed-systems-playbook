# 01: Java Internals & OO Design Mastery

A deep-dive technical track focused on JVM mechanical sympathy, the Java Memory Model, lock-free concurrency primitives, Project Loom virtual thread scheduling, and clean enterprise software architecture with Spring Boot.

## Module Index & Delivery Status

| Submodule | Topic & Architecture Focus | Status | Test Coverage |
| :--- | :--- | :--- | :--- |
| `01-memory-model` | JMM, `volatile` memory barriers, instruction reordering, visibility | Completed ✅ | Unit / JMM Tests |
| `02-locks-and-aqs` | `ReentrantLock`, `Condition` queues, and AQS state manipulation | Completed ✅ | Concurrency Tests |
| `03-aqs-internals` | Custom synchronizer implementation, FIFO wait-queues, CAS loops | Completed ✅ | Multi-Thread Stress Tests |
| `04-lock-free-primitives` | Lock-free queue algorithms using Java 9+ `VarHandle` atomic CAS | Completed ✅ | Multi-producer / Consumer Tests |
| `05-false-sharing-padding` | L1/L2/L3 64-byte cache line contention, manual padding, and `@Contended` | Completed ✅ | Cache-Line Contention Suite |
| `06-virtual-threads-internals` | Project Loom carrier unmounting, monitor pinning risks, and AQS | Completed ✅ | Pinning Diagnostics Suite |
| `07-solid-design-principles` | 7-Day SOLID Masterclass with Spring Boot dynamic registries | Completed ✅ (7/7) | Full SOLID Principle Test Suite |

## Deep Dive: 07 - SOLID Design Principles Series

Structured under `com.arivubuilds.internals.solid` with isolated anti-pattern (`bad`) and enterprise refactored (`good`) packages:

* **[Completed] `p01_overview`**: Demonstrates the fallacy of annotation bloat (`@Transactional`, `@Service`) hiding procedural spaghetti; decouples side-effects using Spring `ApplicationEventPublisher`.
* **[Completed] `p02_srp`**: Deconstructs an `OrderGodService` having 4 distinct reasons to change into single-purpose collaborators (`InventoryReservationService`, `PaymentExecutionService`, `CustomerNotificationService`) orchestrated via an `OrderCoordinator`.
* **[Completed] `p03_ocp`**: Eliminates fragile cascading `if/else` checks for payment types using Spring bean map dynamic strategy dispatch (`PaymentRoutingService`).
* **[Completed] `p04_lsp`**: Enforces behavioral subtyping contracts across `CheckingAccount`, `SavingsAccount`, and `EscrowAccount` without throwing `UnsupportedOperationException`.
* **[Completed] `p05_isp`**: Segregates bloated kitchen-sink device interfaces into focused `Printable`, `Scannable`, and `Faxable` role contracts.
* **[Completed] `p06_dip`**: Hexagonal ports and adapters (`SettlementLedgerPort`, `SettlementEventPort`) protecting core business logic from database and broker coupling.
* **[Completed] `p07_capstone`**: End-to-end refactoring of an enterprise legacy claim settlement method unifying all five SOLID principles (`ClaimPayoutCoordinator`).

## Verification

To compile and verify all submodules inside this track:

```bash
mvn clean test -f 01-java-internals-mastery/pom.xml