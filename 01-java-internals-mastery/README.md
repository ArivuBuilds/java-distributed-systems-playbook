# 01: Java Internals & OO Design Mastery

A deep-dive technical track focused on JVM mechanical sympathy, the Java Memory Model, lock-free concurrency primitives, Project Loom virtual thread scheduling, and clean enterprise software architecture with Spring Boot.

## Module Index & Delivery Status

| Submodule | Topic & Architecture Focus | Status | Test Coverage |
| :--- | :--- | :--- | :--- |
| `01-memory-model` | JMM, `volatile` memory barriers, instruction reordering, visibility | Published ✅ | Unit / JMM Tests |
| `02-locks-and-aqs` | `ReentrantLock`, `Condition` queues, and AQS state manipulation | Published ✅ | Concurrency Tests |
| `03-aqs-internals` | Custom synchronizer implementation, FIFO wait-queues, CAS loops | Published ✅ | Multi-Thread Stress Tests |
| `04-lock-free-primitives` | Lock-free queue algorithms using Java 9+ `VarHandle` atomic CAS | Published ✅ | Multi-producer / Consumer Tests |
| `05-false-sharing-padding` | L1/L2/L3 64-byte cache line contention, manual padding, and `@Contended` | Published ✅ | Cache-Line Contention Suite |
| `06-virtual-threads-internals` | Project Loom carrier unmounting, monitor pinning risks, and AQS | Published ✅ | Pinning Diagnostics Suite |


## Deep Dive: 07 - SOLID Design Principles Series

Structured under `com.arivubuilds.internals.solid` with isolated anti-pattern (`bad`) and enterprise refactored (`good`) packages:

- **[Published] `p01_overview`**: Demonstrates the fallacy of annotation bloat (`@Transactional`, `@Service`) hiding procedural spaghetti; decouples side-effects using Spring `ApplicationEventPublisher`.
- **[Published] `p02_srp`**: Deconstructs an `OrderGodService` having 4 distinct reasons to change into single-purpose collaborators (`InventoryReservationService`, `PaymentExecutionService`, `CustomerNotificationService`) orchestrated via an `OrderCoordinator`.
- **[Published] `p03_ocp`**: Eliminates fragile cascading `if/else` checks for payment types using Spring bean map dynamic strategy dispatch (`PaymentRoutingService`).
- **[Published] `p04_lsp`**: Enforces behavioral subtyping contracts across `CheckingAccount`, `SavingsAccount`, and `EscrowAccount` without throwing `UnsupportedOperationException`.
- **[Published] `p05_isp`**: Segregates bloated kitchen-sink device interfaces into focused `Printable`, `Scannable`, and `Faxable` role contracts.
- **[Published] `p06_dip`**: Hexagonal ports and adapters (`SettlementLedgerPort`, `SettlementEventPort`) protecting core business logic from database and broker coupling.
- **[Published] `p07_capstone`**: End-to-end refactoring of an enterprise legacy claim settlement method unifying all five SOLID principles (`ClaimPayoutCoordinator`).

## Verification

To compile and verify all submodules inside this track:

```bash
mvn clean test -f 01-java-internals-mastery/pom.xml -Dnet.bytebuddy.experimental=true
```
