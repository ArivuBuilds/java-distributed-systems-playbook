package com.arivubuilds.concurrency.aqs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AqsMutexTest {

    @Test
    @DisplayName("AQS Mutex: Guarantees mutual exclusion and atomic increments under heavy contention")
    void testMutualExclusionUnderContention() throws InterruptedException {
        CustomMutex mutex = new CustomMutex();
        int threadCount = 20;
        int incrementsPerThread = 1_000;
        AtomicInteger sharedCounter = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        mutex.lock();
                        try {
                            sharedCounter.set(sharedCounter.get() + 1);
                        } finally {
                            mutex.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "Execution timed out under contention");
        assertEquals(threadCount * incrementsPerThread, sharedCounter.get(),
                "Counter must match exact product of thread count and iterations");
        assertFalse(mutex.isLocked(), "Mutex must be completely unlocked after completion");
    }

    @Test
    @DisplayName("AQS Mutex: tryLock returns false immediately when lock is held by another thread")
    void testTryLockFailure() throws InterruptedException {
        CustomMutex mutex = new CustomMutex();
        CountDownLatch lockAcquiredLatch = new CountDownLatch(1);
        CountDownLatch testCompleteLatch = new CountDownLatch(1);

        Thread ownerThread = new Thread(() -> {
            mutex.lock();
            try {
                lockAcquiredLatch.countDown();
                testCompleteLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                mutex.unlock();
            }
        });

        ownerThread.start();
        lockAcquiredLatch.await();

        // Contender attempts non-blocking tryLock
        boolean acquired = mutex.tryLock();
        assertFalse(acquired, "tryLock must fail when lock is held by another thread");

        testCompleteLatch.countDown();
        ownerThread.join();

        assertTrue(mutex.tryLock(), "tryLock must succeed once owner thread has unlocked");
        mutex.unlock();
    }
}
