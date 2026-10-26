package com.arivubuilds.internals.virtualthreads;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("06: Virtual Threads Internals and Carrier Pinning")
class CarrierPinningEngineTest {

    @Test
    @DisplayName("Verify virtual thread identity and state")
    void testVirtualThreadIdentity() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);

        Thread vThread = Thread.ofVirtual().name("test-vthread").start(() -> {
            assertTrue(Thread.currentThread().isVirtual());
            counter.incrementAndGet();
        });

        vThread.join();
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Verify unmounted ReentrantLock execution under concurrency")
    void testUnmountedReentrantLock() throws InterruptedException {
        CarrierPinningEngine engine = new CarrierPinningEngine();
        int tasks = 50;

        long elapsedMs = CarrierPinningEngine.measureThroughput(tasks, () -> {
            engine.executeWithUnmountedLock(Duration.ofMillis(2));
        });

        assertTrue(elapsedMs > 0, "Execution duration should be non-zero");
    }

    @Test
    @DisplayName("Verify monitor execution completes under virtual thread execution")
    void testSynchronizedMonitorExecution() throws InterruptedException {
        CarrierPinningEngine engine = new CarrierPinningEngine();
        int tasks = 20;

        long elapsedMs = CarrierPinningEngine.measureThroughput(tasks, () -> {
            engine.executeWithMonitorPinning(Duration.ofMillis(2));
        });

        assertTrue(elapsedMs > 0, "Execution duration should be non-zero");
    }
}
