package com.arivubuilds.internals.lockfree;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * High-performance, lock-free FIFO queue based on the Michael & Scott non-blocking algorithm.
 * Uses Java 9+ VarHandle for precise atomic memory operations (compare-and-set).
 *
 * @param <E> element type stored in the queue
 */
public final class LockFreeQueue<E> {

    private static final class Node<E> {
        final E item;
        volatile Node<E> next;

        private static final VarHandle NEXT_HANDLE;

        static {
            try {
                NEXT_HANDLE = MethodHandles.lookup()
                        .findVarHandle(Node.class, "next", Node.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        Node(E item) {
            this.item = item;
            this.next = null;
        }

        boolean casNext(Node<E> expected, Node<E> update) {
            return NEXT_HANDLE.compareAndSet(this, expected, update);
        }
    }

    private volatile Node<E> head;
    private volatile Node<E> tail;

    private static final VarHandle HEAD_HANDLE;
    private static final VarHandle TAIL_HANDLE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            HEAD_HANDLE = lookup.findVarHandle(LockFreeQueue.class, "head", Node.class);
            TAIL_HANDLE = lookup.findVarHandle(LockFreeQueue.class, "tail", Node.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public LockFreeQueue() {
        Node<E> sentinel = new Node<>(null);
        this.head = sentinel;
        this.tail = sentinel;
    }

    /**
     * Enqueues an item without acquiring mutual exclusion locks.
     *
     * @param item value to enqueue (must not be null)
     */
    public void enqueue(E item) {
        if (item == null) {
            throw new NullPointerException("Null elements not supported");
        }

        Node<E> newNode = new Node<>(item);
        while (true) {
            Node<E> currentTail = this.tail;
            Node<E> tailNext = currentTail.next;

            if (currentTail == this.tail) {
                if (tailNext != null) {
                    // Tail was lagging; advance it forward
                    TAIL_HANDLE.compareAndSet(this, currentTail, tailNext);
                } else {
                    // Try linking new node to current tail
                    if (currentTail.casNext(null, newNode)) {
                        // Successfully linked; swing tail to newNode
                        TAIL_HANDLE.compareAndSet(this, currentTail, newNode);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Dequeues an item using lock-free CAS.
     *
     * @return dequeued item, or null if queue is empty
     */
    public E dequeue() {
        while (true) {
            Node<E> currentHead = this.head;
            Node<E> currentTail = this.tail;
            Node<E> headNext = currentHead.next;

            if (currentHead == this.head) {
                if (currentHead == currentTail) {
                    if (headNext == null) {
                        return null; // Empty queue
                    }
                    // Tail is lagging; advance it
                    TAIL_HANDLE.compareAndSet(this, currentTail, headNext);
                } else {
                    if (headNext != null) {
                        E value = headNext.item;
                        if (HEAD_HANDLE.compareAndSet(this, currentHead, headNext)) {
                            return value;
                        }
                    }
                }
            }
        }
    }

    public boolean isEmpty() {
        return head.next == null;
    }
}
