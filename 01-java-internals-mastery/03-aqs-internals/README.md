# 03: AbstractQueuedSynchronizer (AQS) Mechanics & CLH Queueing

## 🧠 Architectural Overview

`AbstractQueuedSynchronizer` (AQS) is the foundational engine powering `ReentrantLock`, `Semaphore`, `CountDownLatch`, `ReentrantReadWriteLock`, and `CompletableFuture`.

Instead of relying on OS-level heavy mutexes or continuous busy-spinning across CPU cores, AQS couples:
1. **Volatile State Variable:** A single 32-bit integer (`volatile int state`) accessed and modified exclusively via hardware atomic primitives (CAS - Compare-And-Swap).
2. **Modified CLH (Craig, Landin, and Hagersten) Lock Queue:** A FIFO doubly linked list of thread wait nodes. Contending threads that fail state acquisition are enqueued and parked via `LockSupport.park()`, eliminating cache-line bouncing.

---

## ⚙️ Core Synchronization Mechanics

### 1. State Mutation Contract
* Subclasses override protected template methods:
  * `tryAcquire(int arg)` / `tryRelease(int arg)` (Exclusive Mode)
  * `tryAcquireShared(int arg)` / `tryReleaseShared(int arg)` (Shared Mode)
* State semantics depend on implementation:
  * **`ReentrantLock`:** `state == 0` (unlocked), `state > 0` (reentrant lock hold count).
  * **`Semaphore`:** `state` represents available permit count.
  * **`CountDownLatch`:** `state` represents remaining countdown iterations.

### 2. CLH Node Wait States (`waitStatus`)
Nodes queued in AQS maintain an atomic state governing unparking lifecycles:

| Wait Status | Value | Semantic Meaning |
| :--- | :---: | :--- |
| **`SIGNAL`** | `-1` | The successor thread is parked; this node must unpark its successor when released. |
| **`CANCELLED`** | `1` | Thread timed out or was interrupted while waiting; bypassed during traversal. |
| **`CONDITION`** | `-2` | Node is currently residing on a `ConditionObject` wait queue. |
| **`PROPAGATE`**| `-3` | Shared acquire transitions must propagate transitively across successors. |

---

## 🧪 Production Implementations Included
- [`CustomMutex.java`](src/main/java/com/arivubuilds/concurrency/aqs/CustomMutex.java): A clean, non-reentrant mutual exclusion lock built on AQS demonstrating explicit `tryAcquire` and `tryRelease` CAS state transitions.
- [`AqsMutexTest.java`](src/test/java/com/arivubuilds/concurrency/aqs/AqsMutexTest.java): High-concurrency test harness validating mutual exclusion, FIFO queue serialization, and non-reentrancy barriers under thread contention.
