package com.arivubuilds.internals.falsesharing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FalseSharingBenchmarkTest {
    private static final long ITRATIONS = 20_000_000L;


    @Test
    @DisplayName("Verify cache-line padding and @Contended complete accurately under multi-core contention")
    void testCacheLinePaddingAndContendedIsolation() throws InterruptedException {
        UnpaddedMetricsCounter unpadded = new UnpaddedMetricsCounter();
        ManualPaddedMetricsCounter padded = new ManualPaddedMetricsCounter();
        ContendedMetricsCounter contended = new ContendedMetricsCounter();

        long unpaddedNs = runConcurrent(() -> { for (long i = 0; i < ITRATIONS; i++) unpadded.producerCount = i; }, () -> { for (long i = 0; i < ITRATIONS; i++) unpadded.consumerCount = i; });
        long paddedNs = runConcurrent(() -> { for (long i = 0; i < ITRATIONS; i++) padded.producerCount = i; }, () -> { for (long i = 0; i < ITRATIONS; i++) padded.consumerCount = i; });
        long contendedNs = runConcurrent(() -> { for (long i = 0; i < ITRATIONS; i++) contended.producerCount = i; }, () -> { for (long i = 0; i < ITRATIONS; i++) contended.consumerCount = i; });

        System.out.printf("Unpadded: %d ms | Manual Padded: %d ms | @Contended: %d ms%n", unpaddedNs / 1_000_000, paddedNs / 1_000_000, contendedNs / 1_000_000);
        assertEquals(ITRATIONS - 1, padded.producerCount);
        assertEquals(ITRATIONS - 1, contended.consumerCount);
        assertTrue(paddedNs > 0 && contendedNs > 0);
    }

    private static long runConcurrent(Runnable t1, Runnable t2) throws InterruptedException {
        Thread a = new Thread(t1); Thread b = new Thread(t2);
        long start = System.nanoTime();
        a.start(); b.start(); a.join(); b.join();
        return System.nanoTime() - start;
    }
}
