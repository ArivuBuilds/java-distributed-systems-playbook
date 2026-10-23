# Day 01: In-Place Two Pointers Log Compaction

## Problem Statement
Given a monotonically non-decreasing array of 64-bit log offsets (long[]), remove duplicate entries in-place such that each unique offset appears only once. The relative order of the elements must be preserved. Return the number of unique elements K, where the first K slots of the original array contain the compacted results.

An extension variant (compactWithQuota) allows preserving up to M duplicate occurrences per unique offset for quorum verification scenarios.

---

## Systems Architecture Context

In write-heavy distributed systems (e.g., Apache Kafka, Apache BookKeeper, LSM-Tree WALs):
* **At-Least-Once Delivery**: Network retries lead to duplicate event sequences landing in contiguous memory batches.
* **Write-Ahead Log (WAL) Compaction**: Before flushing memory buffers to disk or committing SSTables, redundant sequence numbers must be eliminated to conserve I/O bandwidth and disk space.
* **Mechanical Sympathy**: Using primitive arrays (long[]) instead of object collections (List<Long>) avoids the 24-byte object header tax per entry, completely eliminates pointer chasing, and aligns with 64-byte CPU cache lines (fetching 8 packed long values per cycle).

---

## Two Pointers Mechanics and Invariant Proof

Two pointers traverse the array:
* **readIndex (Fast Explorer)**: Scans every element of the array sequentially.
* **writeIndex (Slow Committer)**: Tracks the boundary of the verified unique sub-array.

```text
Initial State:
[ 100,  100,  101,  102,  102,  103 ]
   ^     ^
 write  read

Step 1: offsets[read] == offsets[write] -> Duplicate detected. Advance readIndex.
[ 100,  100,  101,  102,  102,  103 ]
   ^           ^
 write        read

Step 2: offsets[read] != offsets[write] -> New value. Advance writeIndex, copy value.
[ 100,  101,  101,  102,  102,  103 ]
         ^     ^
       write  read

Loop InvariantAt the start of each iteration:
For all i in [0, writeIndex - 1], offsets[i] < offsets[i + 1].
The subarray offsets[0 ... writeIndex] contains all unique values processed so far in strictly increasing order.Complexity AnalysisMetricBoundJustificationTime ComplexityO(N)The readIndex traverses the buffer of size N exactly once. Each step executes O(1) operations.Auxiliary SpaceO(1)Memory is reused in-place. Zero heap allocations or garbage collection events occur.Cache BehaviorOptimalSequential memory access allows CPU hardware prefetchers to load sequential cache lines ahead of instructions.
Verification
Run the test suite using Maven:

