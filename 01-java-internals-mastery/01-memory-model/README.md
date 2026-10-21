# 01: Java Memory Model (JMM), Hardware Barriers & Happens-Before

## 🧠 Architectural Overview

At CPU scale, hardware processors do not execute instructions strictly in source-code sequence. Out-of-order execution, speculative pipelining, store buffers, and multi-level CPU caches (L1/L2/L3) maximize instructions-per-clock (IPC) at the cost of sequential consistency across multiple cores.

The **Java Memory Model (JSR-133)** defines the formal contract governing when writes by one thread become visible to reads by another.

---

## 🛑 Hardware Memory Barriers

HotSpot translates JVM synchronization constructs to low-level hardware memory barriers:

| Barrier Type | Sequence Contract | Hardware Action |
| :--- | :--- | :--- |
| **`LoadLoad`** | `Load1; LoadLoad; Load2` | Guarantees `Load1` data is retrieved before `Load2` and subsequent loads execute. |
| **`StoreStore`** | `Store1; StoreStore; Store2` | Guarantees `Store1` data is flushed to cache before `Store2` becomes visible to other cores. |
| **`LoadStore`** | `Load1; LoadStore; Store2` | Guarantees `Load1` completes before `Store2` data is flushed out of store buffers. |
| **`StoreLoad`** | `Store1; StoreLoad; Load2` | **Heaviest barrier.** Drains write buffers completely; ensures `Store1` is globally visible before `Load2` executes. On x86, HotSpot emits `lock addl ...` or `mfence`. |

### Volatile Barrier Assembly Mapping
* **Before volatile write:** Emits `StoreStore` barrier (prevents previous normal writes from reordering below the volatile write).
* **After volatile write:** Emits `StoreLoad` barrier (flushes write buffers before subsequent operations execute).
* **After volatile read:** Emits `LoadLoad` and `LoadStore` barriers (prevents subsequent reads/writes from floating above the volatile read).

---

## ⚡ The Happens-Before Relationship

If operation $A$ happens-before operation $B$ ($A \xrightarrow{hb} B$), the memory state left by $A$ is guaranteed visible to the thread executing $B$.

Key structural rules:
1. **Program Order Rule:** Each action in a thread happens-before every action that comes later in program order.
2. **Volatile Variable Rule:** A write to a `volatile` variable happens-before every subsequent read of that same variable.
3. **Monitor Lock Rule:** An unlock on a monitor happens-before every subsequent lock on the same monitor.
4. **Transitivity:** If $A \xrightarrow{hb} B$ and $B \xrightarrow{hb} C$, then $A \xrightarrow{hb} C$.

---

## 🧪 Production Implementations Included
- [`InstructionReorderingDemo.java`](src/main/java/com/arivubuilds/concurrency/jmm/InstructionReorderingDemo.java): Dekker/Peterson harness demonstrating CPU store-buffer reordering states ($r1 = 0, r2 = 0$).
- [`VolatilePiggybacking.java`](src/main/java/com/arivubuilds/concurrency/jmm/VolatilePiggybacking.java): Practical pattern showing safe publication of non-volatile configuration objects using volatile transitivity.
