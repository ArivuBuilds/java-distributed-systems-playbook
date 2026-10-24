# Module 01: Java Internals Mastery

A deep-dive architectural playbook on low-level Java 21 synchronization primitives, memory barriers, hardware-level cache line mechanics, and user-mode thread scheduling.

---

## Module Curriculum & Submodules

- [`01-memory-model` — Java Memory Model (JMM)](01-memory-model/)
  * Volatile semantics, Acquire-Release barriers, reordering prevention, and happens-before graph guarantees.
- [`02-locks-and-aqs` — Synchronization Primitives](02-locks-and-aqs/)
  * Custom mutexes, spinlocks, and backoff strategies preventing thread starvation.
- [`03-aqs-internals` — AbstractQueuedSynchronizer (AQS)](03-aqs-internals/)
  * AQS CLH wait-queue node linking, CAS state transitions, exclusive vs. shared modes, and Condition await/signal.
- [`04-lock-free-primitives` — Lock-Free Algorithms](04-lock-free-primitives/)
  * Michael-Scott non-blocking FIFO queue introducing Java 9+ `VarHandle` CAS atomic operations.
- [`05-false-sharing-padding` — CPUCache Line Mechanical Sympathy](05-false-sharing-padding/)
  * LLK/L2/L3 64-byte cache line alignment, MESI invalidation storms, manual field padding, and `@JDK.internal.vm.annotation.Contended`.
- [`06-virtual-threads-internals` — Project Loom & Virtual Threads](06-virtual-threads-internals/)
  * ForkJoinPool carrier thread scheduling, continuation frame unmounting, monitor pinning risks, and `ReentrantLock` mitigations.

---

## Verification

Run the entire module test suite from the repository root:

```bash
mvn test -pl :01-java-internals-mastery
```
