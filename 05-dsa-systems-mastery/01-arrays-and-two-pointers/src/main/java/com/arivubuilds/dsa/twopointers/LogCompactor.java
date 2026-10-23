package com.arivubuilds.dsa.twopointers;

import java.util.Objects;

/**
 * Production-grade in-place log stream compactor utilizing the Two-Pointers pattern.
 *
 * <p><strong>Systems Context:</strong>
 * In high-throughput distributed commit logs (such as Kafka partition segments or LSM-tree write-ahead logs),
 * consumers frequently ingest batches of monotonic event offsets or telemetry sequence numbers containing duplicates
 * caused by at-least-once retry delivery semantics.
 *
 * <p><strong>Mechanical Sympathy:</strong>
 * Operating directly on contiguous primitive {@code long[]} buffers maximizes CPU L1/L2 cache line utilization
 * (64 bytes per cache line = 8 packed long values per fetch) and produces zero JVM heap allocations, completely
 * bypassing garbage collection churn.
 *
 * <p><strong>Complexity:</strong>
 * <ul>
 *   <li>Time Complexity: {@code O(N)} — Single linear pass with read and write cursors.</li>
 *   <li>Space Complexity: {@code O(1)} — Strict in-place mutation without auxiliary memory.</li>
 * </ul>
 */
public final class LogCompactor {

    private LogCompactor() {
        // Static utility class
    }

    /**
     * Compacts a monotonically non-decreasing array of log offsets in-place by removing all duplicates.
     *
     * <p><strong>Invariant:</strong>
     * For all indices {@code i} such that {@code 0 <= i < writeIndex}, {@code offsets[i] < offsets[i + 1]}.
     *
     * @param offsets a sorted array of log sequence numbers or offsets
     * @return the number of unique elements remaining in the compacted prefix of the array
     * @throws NullPointerException if {@code offsets} is null
     * @throws IllegalArgumentException if the array violates the non-decreasing sort order invariant
     */
    public static int compactUnique(long[] offsets) {
        Objects.requireNonNull(offsets, "Log offset buffer cannot be null");

        if (offsets.length <= 1) {
            return offsets.length;
        }

        int writeIndex = 0;

        for (int readIndex = 1; readIndex < offsets.length; readIndex++) {
            // Verify invariant: input must be sorted
            if (offsets[readIndex] < offsets[readIndex - 1]) {
                throw new IllegalArgumentException(
                    "Input buffer violates monotonic order at index %d: %d < %d"
                        .formatted(readIndex, offsets[readIndex], offsets[readIndex - 1])
                );
            }

            // Only advance and write when encountering a strictly greater offset
            if (offsets[readIndex] != offsets[writeIndex]) {
                writeIndex++;
                if (writeIndex != readIndex) {
                    offsets[writeIndex] = offsets[readIndex];
                }
            }
        }

        return writeIndex + 1;
    }

    /**
     * Compacts a monotonically non-decreasing array of log offsets in-place, allowing at most
     * {@code maxAllowedDuplicates} consecutive occurrences of any offset.
     *
     * <p><strong>Systems Context:</strong>
     * Used in windowed telemetry sampling where a small constant factor of duplicate heartbeats
     * is retained for consensus quorum verification before purging.
     *
     * @param offsets               a sorted array of log offsets
     * @param maxAllowedDuplicates  the maximum frequency of duplicates to preserve per unique offset (must be >= 1)
     * @return the number of elements retained in the compacted prefix
     * @throws NullPointerException     if {@code offsets} is null
     * @throws IllegalArgumentException if {@code maxAllowedDuplicates < 1} or array is unsorted
     */
    public static int compactWithQuota(long[] offsets, int maxAllowedDuplicates) {
        Objects.requireNonNull(offsets, "Log offset buffer cannot be null");
        if (maxAllowedDuplicates < 1) {
            throw new IllegalArgumentException("maxAllowedDuplicates must be at least 1, received: " + maxAllowedDuplicates);
        }

        if (offsets.length <= maxAllowedDuplicates) {
            return offsets.length;
        }

        int writeIndex = 0;

        for (int readIndex = 0; readIndex < offsets.length; readIndex++) {
            if (readIndex > 0 && offsets[readIndex] < offsets[readIndex - 1]) {
                throw new IllegalArgumentException(
                    "Input buffer violates monotonic order at index %d: %d < %d"
                        .formatted(readIndex, offsets[readIndex], offsets[readIndex - 1])
                );
            }

            // An element is admitted if we have not yet written maxAllowedDuplicates elements,
            // or if the current candidate is strictly greater than the element written k positions ago.
            if (writeIndex < maxAllowedDuplicates || offsets[readIndex] != offsets[writeIndex - maxAllowedDuplicates]) {
                if (writeIndex != readIndex) {
                    offsets[writeIndex] = offsets[readIndex];
                }
                writeIndex++;
            }
        }

        return writeIndex;
    }
}
