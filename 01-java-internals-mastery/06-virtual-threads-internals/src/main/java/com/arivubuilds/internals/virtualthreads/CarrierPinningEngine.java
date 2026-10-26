package com.arivubuilds.internals.virtualthreads;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates Project Loom execution mechanics and carrier thread pinning.
 * <p>
 * Virtual threads unmount from ForkJoinPool carrier threads during blocking I/O or parking.
 * However, holding an intrinsic monitor (synchronized block) or executing native code
 * pins the continuation frame to the carrier OS thread, preventing other virtual threads
 * from utilizing that carrier worker.
 */
public final class CarrierPinningEngine {

    private final Object monitorLock = new Object();
    private final ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * Executes a blocking operation inside a synchronized block.
     * Triggers carrier thread pinning.
     */
    public boolean executeWithMonitorPinning(Duration blockDuration) {
        synchronized (monitorLock) {
            try {
                Thread.sleep(blockDuration);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * Executes a blocking operation using ReentrantLock.
     * Virtual thread unmounts cleanly without pinning the underlying carrier thread.
     */
    public boolean executeWithUnmountedLock(Duration blockDuration) {
        reentrantLock.lock();
        try {
            Thread.sleep(blockDuration);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Runs N concurrent tasks on virtual threads and returns total elapsed execution time in milliseconds.
     */
    public static long measureThroughput(int concurrency, Runnable task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(concurrency);
        long start = System.nanoTime();

        for (int i = 0; i < concurrency; i++) {
            Thread.ofVirtual().name("loom-worker-", i).start(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long elapsedNanos = System.nanoTime() - start;

        if (!completed) {
            throw new IllegalStateException("Tasks failed to complete within timeout");
        }

        return TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
    }
}
