package com.arivubuilds.concurrency.jmm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryModelHarnessTest {

    @Test
    @DisplayName("Volatile Piggybacking: Reader observes plain field writes through volatile happens-before transitivity")
    void testVolatilePiggybackingVisibility() throws InterruptedException {
        VolatilePiggybacking component = new VolatilePiggybacking();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicBoolean readerSawConsistentState = new AtomicBoolean(false);

        Thread writer = new Thread(() -> {
            try {
                startLatch.await();
                component.publishConfiguration("PROD_CONFIG_PAYLOAD", 5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        });

        Thread reader = new Thread(() -> {
            try {
                startLatch.await();
                while (!component.isInitialized()) {
                    Thread.onSpinWait();
                }
                String result = component.readConfigurationIfReady();
                if ("Data: PROD_CONFIG_PAYLOAD, Timeout: 5000".equals(result)) {
                    readerSawConsistentState.set(true);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        });

        writer.start();
        reader.start();

        startLatch.countDown();

        boolean finished = doneLatch.await(3, TimeUnit.SECONDS);
        assertTrue(finished, "Execution timed out");
        assertTrue(readerSawConsistentState.get(), "Reader must observe plain writes guarded by volatile write");
    }
}
