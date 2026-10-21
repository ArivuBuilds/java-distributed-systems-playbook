# 02: `ReentrantLock` vs. `synchronized` & Multi-Condition Queues

## 🔍 Core Architectural Differences

| Feature | `synchronized` | `ReentrantLock` |
| :--- | :--- | :--- |
| **Mechanism** | JVM intrinsic monitor (`monitorenter` / `monitorexit`) | API level via AbstractQueuedSynchronizer (AQS) |
| **Lock Acquisition** | Uninterruptible blocking | `lock()`, `lockInterruptibly()`, `tryLock()` |
| **Timed Waiting** | No (blocks indefinitely) | Yes (`tryLock(timeout, unit)`) |
| **Fairness Policy** | Non-fair only (JVM chooses) | Fair or Non-Fair (`new ReentrantLock(true)`) |
| **Wait / Notify Queues** | Single wait-set per monitor (`wait()`, `notify()`) | Multiple condition queues per lock (`newCondition()`) |
| **Lock Release** | Automatic (guaranteed by JVM) | Manual (strictly requires `try { ... } finally { lock.unlock(); }`) |

---

## ⚡ Multi-Condition Queues (`notFull` & `notEmpty`)

With JVM monitors (`synchronized`), calling `notify()` or `notifyAll()` wakes threads indiscriminately from a single wait-set, leading to wasted CPU cycles and context switching.

`ReentrantLock` solves this via explicit condition queues:
* **`notFull` Condition:** Producers await when capacity is reached; consumers signal it when an element is removed.
* **`notEmpty` Condition:** Consumers await when empty; producers signal it when an element is added.

---

## 🧪 Production Implementation Included
- [`BoundedEventQueue.java`](src/main/java/com/arivubuilds/concurrency/locks/BoundedEventQueue.java): High-throughput bounded blocking queue using explicit `ReentrantLock` dual-conditions.
- [`BoundedEventQueueTest.java`](src/test/java/com/arivubuilds/concurrency/locks/BoundedEventQueueTest.java): Multi-threaded producer-consumer test verifying deadlock-free condition signaling.
