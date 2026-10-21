package com.arivubuilds.concurrency.locks;

package com.arivubuilds.concurrency.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A production-grade bounded buffer demonstrating explicit Condition queues
 * to avoid thundering-herd context switching between producers and consumers.
 *
 * @param <T> the type of elements held in this queue
 */
public class BoundedEventQueue<T> {

    private final Object[] buffer;
    private int putIndex;
    private int takeIndex;
    private int count;

    private final ReentrantLock lock;
    private final Condition notFull;
    private final Condition notEmpty;

    public BoundedEventQueue(int capacity, boolean fair) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.buffer = new Object[capacity];
        this.lock = new ReentrantLock(fair);
        this.notFull = this.lock.newCondition();
        this.notEmpty = this.lock.newCondition();
    }

    public BoundedEventQueue(int capacity) {
        this(capacity, false);
    }

    /**
     * Inserts an element into the buffer, waiting if full.
     */
    public void put(T item) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            // Guard with a while-loop against spurious wakeups
            while (count == buffer.length) {
                notFull.await();
            }

            buffer[putIndex] = item;
            if (++putIndex == buffer.length) {
                putIndex = 0;
            }
            count++;

            // Signal strictly consumer threads waiting on notEmpty
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of the buffer, waiting if empty.
     */
    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            // Guard with a while-loop against spurious wakeups
            while (count == 0) {
                notEmpty.await();
            }

            T item = (T) buffer[takeIndex];
            buffer[takeIndex] = null; // Prevent memory leak / loitering
            if (++takeIndex == buffer.length) {
                takeIndex = 0;
            }
            count--;

            // Signal strictly producer threads waiting on notFull
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}