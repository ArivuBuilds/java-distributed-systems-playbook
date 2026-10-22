package com.arivubuilds.concurrency.aqs;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Production-grade custom non-reentrant mutual exclusion lock.
 * Demonstrates direct extension of AbstractQueuedSynchronizer (AQS)
 * using state 0 (unlocked) and 1 (locked) via atomic CAS.
 */
public class CustomMutex implements Lock, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static class Sync extends AbstractQueuedSynchronizer {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean tryAcquire(int acquires) {
            assert acquires == 1; // Strict non-reentrant mutex protocol
            // CAS: 0 (free) -> 1 (held)
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int releases) {
            assert releases == 1;
            if (getState() == 0) {
                throw new IllegalMonitorStateException("Attempted to release unacquired lock");
            }
            if (Thread.currentThread() != getExclusiveOwnerThread()) {
                throw new IllegalMonitorStateException("Current thread does not own this lock");
            }
            setExclusiveOwnerThread(null);
            setState(0); // Volatile write; unparks successor in AQS
            return true;
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1 && getExclusiveOwnerThread() == Thread.currentThread();
        }

        Condition newCondition() {
            return new ConditionObject();
        }
    }

    private final Sync sync = new Sync();

    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    public boolean isLocked() {
        return sync.isHeldExclusively();
    }
}
