package com.arivubuilds.internals.lockfree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lock-Free Queue using VarHandle CAS Tests")
class LockFreeQueueTest {

    @Test
    @DisplayName("Single-threaded FIFO order verification")
    void testSingleThreadedFifo() {
        LockFreeQueue<Integer> queue = new LockFreeQueue<>();
        assertTrue(queue.isEmpty());

        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);

        assertFalse(queue.isEmpty());
        assertEquals(10, queue.dequeue());
        assertEquals(20, queue.dequeue());
        assertEquals(30, queue.dequeue());
        assertNull(queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Concurrent producer-consumer high contention verification")
    void testConcurrentContention() throws InterruptedException {
        LockFreeQueue<Integer> queue = new LockFreeQueue<>();
        int producers = 8;
        int itemsPerProducer = 10_000;
        int totalExpectedItems = producers * itemsPerProducer;

        ExecutorService executor = Executors.newFixedThreadPool(producers + 4);
        CountDownLatch latch = new CountDownLatch(producers);

        for (int p = 0; p < producers; p++) {
            executor.submit(() -> {
                for (int i = 0; i < itemsPerProducer; i++) {
                    queue.enqueue(i);
                }
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);

        AtomicInteger dequeuedCount = new AtomicInteger(0);
        CountDownLatch consumerLatch = new CountDownLatch(4);

        for (int c = 0; c < 4; c++) {
            executor.submit(() -> {
                while (true) {
                    Integer val = queue.dequeue();
                    if (val != null) {
                        dequeuedCount.incrementAndGet();
                    } else if (dequeuedCount.get() >= totalExpectedItems) {
                        break;
                    }
                }
                consumerLatch.countDown();
            });
        }

        boolean completed = consumerLatch.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertTrue(completed, "Consumers did not finish in time");
        assertEquals(totalExpectedItems, dequeuedCount.get());
        assertTrue(queue.isEmpty());
    }
}
