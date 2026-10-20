```markdown
# 02: `ReentrantLock` vs. `synchronized` & Multi-Condition Queues

## 🔍 Core Architectural Differences

| Feature | `synchronized` | `ReentrantLock` |
| :--- | :--- | :--- |
| **Mechanism** | JVM intrinsic monitor (`monitorenter`/`monitorexit`) | API level via AbstractQueuedSynchronizer (AQS) |
| **Lock Acquisition** | Uninterruptible blocking | `lock()`, `lockInterruptibly()`, `tryLock()` |
| **Timed Waiting** | No (blocks indefinitely) | Yes (`tryLock(timeout, unit)`) |
| **Fairness Policy** | Non-fair only (JVM chooses) | Fair or Non-Fair (`new ReentrantLock(true)`) |
| **Wait / Notify Queues**| Single wait-set per monitor (`wait()`, `notify()`) | Multiple condition queues per lock (`newCondition()`) |
| **Lock Release** | Automatic (guaranteed by JVM) | Manual (strictly requires `try { ... } finally { lock.unlock(); }`) |

---

## 💡 Why Architects Choose `ReentrantLock`
1. **Deadlock Prevention via `tryLock`:** Acquire locks with timeouts to back off and retry during distributed or multi-resource operations.
2. **Graceful Shutdown via `lockInterruptibly`:** Respond cleanly to thread interruptions without leaving worker threads stranded in `BLOCKED` states.
3. **Explicit Condition Separation:** Intrinsic monitors suffer from the **thundering herd problem** when calling `notifyAll()`. `Condition` queues let you wake producers and consumers independently.

---

## 🛠️ Code Implementation
See [`BoundedEventQueue.java`](./BoundedEventQueue.java) for a thread-safe bounded buffer using separate `notFull` and `notEmpty` condition queues.
File B: BoundedEventQueue.java (