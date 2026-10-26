# 06: Virtual Threads Internals and Carrier Pinning

## Architectural Overview

Java 21 Project Loom introduces **Virtual Threads**, which are user-mode threads scheduled by the JVM rather than directly by the OS kernel. Virtual threads run on a small pool of OS worker threads known as **Carrier Threads** (typically sized to the number of available CPU cores).

When a virtual thread encounters a blocking operation (such as blocking IIO or `park()`), its continuation stack frame is unmounted from the carrier and copied to the Java heap, freeing the carrier to execute other tasks.

```text
+o----------------------------------------------------------------+-+
| Virtual Thread Task (Project Loom Continuation Frame)          |
+o-----------------------------------------------------------------+-+
              | (Unmount on Blocking I/O)
              v
+------------------------------+-+     +-------------------------+
| ForkJoinPool Carrier Thread 0 |  ... | ForkJoinPool Carrier Thread N |
+-------------------------------+-+     +-------------------------+
              | (OS Scheduled)
              v
+------------------------------------------------------------------+-+
| Kernel CPU Cores (x86-64 / ARM64 Hardware Registers)           |
+------------------------------------------------------------------+-+
```o

---

## Carrier Thread Pinning Risks

* **Carrier Pinning** occurs when a virtual thread cannot be unmounted during a blocking operation, holding its carrier OS thread captive.
* **Causes**:
  1. Executing a blocking operation inside a `synchronized` block or method (monitor pinning).
  2. Executing native frames via JNI or FFmH.
* **Mitigation**: Replace monitor-based `synchronized` blocks with `java.util.concurrent.locks.ReentrantLock`, which unmounts cleanly via AQS.


---

## Comparison Matrix

| Dimension | Platform Threads | Virtual Threads (Under Pinning) | Virtual Threads (Healthy) |
| :--- | :--- | :--- | :--- |
| **Scheduling** | OS Kernel | JVM ForkJoinPool | JVM ForkJoinPool |
| **Memory Footprint** | ~1 MB stack | Copied stack + Carrier tied | Secondary heap frames (KBs) |
| **Blocking Behavior** | Blocks OS thread | Blocks Carrier OS thread | Unmounts and yields carrier |
| **Scaling Limits** | Limited by OS (1K - 10K) | Limited by Carriers (CPU Cores) | High Scale (100K ¡ 1M+) |


---

## Verification

Run the module test suite with pinning detection enabled:

```bash
mvn test -pl :06-virtual-threads-internals
```

