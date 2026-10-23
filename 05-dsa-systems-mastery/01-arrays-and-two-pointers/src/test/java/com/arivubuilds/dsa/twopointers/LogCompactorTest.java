package com.arivubuilds.dsa.twopointers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Day 01: In-Place Two Pointers Log Compactor Tests")
class LogCompactorTest {

    @Nested
    @DisplayName("compactUnique() Specifications")
    class CompactUniqueTests {

        @Test
        @DisplayName("Should handle empty and single-element buffers")
        void shouldHandleTrivialBuffers() {
            long[] empty = new long[0];
            assertThat(LogCompactor.compactUnique(empty)).isZero();

            long[] single = new long[]{1001L};
            assertThat(LogCompactor.compactUnique(single)).isEqualTo(1);
            assertThat(single[0]).isEqualTo(1001L);
        }

        @Test
        @DisplayName("Should compact buffer with multiple duplicate segments")
        void shouldCompactDuplicatesCorrectly() {
            long[] offsets = {100L, 100L, 101L, 102L, 102L, 102L, 103L, 104L, 104L};
            int uniqueCount = LogCompactor.compactUnique(offsets);

            assertThat(uniqueCount).isEqualTo(5);
            assertThat(Arrays.copyOf(offsets, uniqueCount))
                .containsExactly(100L, 101L, 102L, 103L, 104L);
        }

        @Test
        @DisplayName("Should maintain idempotence when no duplicates exist")
        void shouldBeNoOpWhenAlreadyUnique() {
            long[] offsets = {10L, 20L, 30L, 40L, 50L};
            int count = LogCompactor.compactUnique(offsets);

            assertThat(count).isEqualTo(5);
            assertThat(offsets).containsExactly(10L, 20L, 30L, 40L, 50L);
        }

        @Test
        @DisplayName("Should collapse buffer of identical offsets down to single value")
        void shouldCollapseIdenticalValues() {
            long[] offsets = {42L, 42L, 42L, 42L, 42L};
            int count = LogCompactor.compactUnique(offsets);

            assertThat(count).isEqualTo(1);
            assertThat(offsets[0]).isEqualTo(42L);
        }

        @Test
        @DisplayName("Should throw NullPointerException when input is null")
        void shouldRejectNull() {
            assertThatThrownBy(() -> LogCompactor.compactUnique(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Log offset buffer cannot be null");
        }

        @Test
        @DisplayName("Should fail fast on unsorted buffers")
        void shouldFailOnUnsortedBuffer() {
            long[] unsorted = {10L, 20L, 15L, 30L};
            assertThatThrownBy(() -> LogCompactor.compactUnique(unsorted))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("violates monotonic order at index 2");
        }
    }

    @Nested
    @DisplayName("compactWithQuota() Specifications")
    class CompactWithQuotaTests {

        @ParameterizedTest(name = "Buffer length {0} with quota {1}")
        @MethodSource("quotaTestCases")
        void shouldPreserveUpToQuotaDuplicates(long[] input, int quota, int expectedLength, long[] expectedPrefix) {
            int resultLen = LogCompactor.compactWithQuota(input, quota);

            assertThat(resultLen).isEqualTo(expectedLength);
            assertThat(Arrays.copyOf(input, resultLen)).containsExactly(expectedPrefix);
        }

        static Stream<Arguments> quotaTestCases() {
            return Stream.of(
                // Preserving up to 2 duplicates per log offset
                Arguments.of(
                    new long[]{1L, 1L, 1L, 2L, 2L, 3L},
                    2,
                    5,
                    new long[]{1L, 1L, 2L, 2L, 3L}
                ),
                // Quota 1 is identical to compactUnique()
                Arguments.of(
                    new long[]{1L, 1L, 2L, 2L, 3L, 3L},
                    1,
                    3,
                    new long[]{1L, 2L, 3L}
                ),
                // Quota greater than maximum frequency leaves array unchanged
                Arguments.of(
                    new long[]{5L, 5L, 10L, 10L},
                    3,
                    4,
                    new long[]{5L, 5L, 10L, 10L}
                )
            );
        }

        @Test
        @DisplayName("Should reject illegal quota values")
        void shouldRejectInvalidQuota() {
            long[] buffer = {1L, 2L};
            assertThatThrownBy(() -> LogCompactor.compactWithQuota(buffer, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxAllowedDuplicates must be at least 1");
        }
    }
}
