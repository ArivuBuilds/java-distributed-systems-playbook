# 04: Lock-Free Primitives and VarHandle Mechanics

## Architectural Overview

Traditional synchronization primitives (`synchronized`, `ReentrantLock`) protect critical sections via thread suspension, operating system context switches, and kernel transitions. Under extreme producer-consumer contention, thread parking and unparking introduce non-deterministic tail latencies.

The **Michael-Scott Lock-Free Queue** algorithm guarantees system-wide forward progress (lock-free) without thread blocking. Using Java 9+ `VarHandle`, memory modifications execute directly as atomic hardware-level Compare-And-Swap (`CAS`) instructions.

```text
Enqueue Transition:
Tail -> [ Sentinel Node ] -> null
              ^
        (newNode arrives)

Step 1 (Swing next pointer):
Tail -> [ Sentinel Node ] ----CAS----> [ New Node ] -> null

Step 2 (Advance tail pointer):
        [ Sentinel Node ]              [ New Node ] -> null
                                             ^
                                           Tail
Core Concurrency MechanicsAtomic Reference Updates with VarHandle: Replaces legacy AtomicReference and AtomicReferenceFieldUpdater with zero-overhead polymorphic signature calls directly inlined by the JIT compiler.Sentinel Node Invariant: The queue maintains a dummy head node. head.next always points to the first true element, preventing empty/non-empty boundary race conditions.Helping Lagging Threads: If a thread detects tail.next != null, another enqueue operation linked a node but did not finish advancing tail. The observing thread proactively advances tail before proceeding, ensuring global progress.Asymptotic & Memory BoundsDimensionMutex-Based Blocking QueueLock-Free Michael-Scott QueueProgress GuaranteeBlocking (Vulnerable to thread descheduling)Lock-Free (At least one thread makes progress)Thread Context SwitchesHigh under contention (OS park/unpark)Zero (Threads spin via bounded CAS attempts)Enq/Deq Complexity$O(1)$ amortized (with lock contention latency)$O(1)$ amortized non-blockingAuxiliary Space$O(1)$$O(1)$ per node insertion
