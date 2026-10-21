package com.arivubuilds.concurrency.locks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundedEventQueueTest {

    @Test
    @DisplayName("Dual-Condition Queue: Producers block when full, consumers unblock on consumption")
    void testProducerConsumerDualCondition() throws InterruptedException {
        int capacity = 5;
        int totalItems = 50;
        BoundedEventQueue<Integer> queue = new BoundedEventQueue<>(capacity);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);
        AtomicInteger consumedSum = new AtomicInteger(0);

        Thread producer = new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 1; i <= totalItems; i++) {
                    queue.put(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 1; i <= totalItems; i++) {
                    consumedSum.addAndGet(queue.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        });

        producer.start();
        consumer.start();

        startLatch.countDown();

        boolean completed = finishLatch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "Dual condition queue should not deadlock");
        
        int expectedSum = totalItems * (totalItems + 1) / 2;
        assertEquals(expectedSum, consumedSum.get(), "Sum of all consumed elements must match");
    }
}
