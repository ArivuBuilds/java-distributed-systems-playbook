package com.arivubuilds.concurrency.jmm;

/**
 * Dekker-style instruction reordering harness.
 * Without hardware memory barriers, modern processors allow stores to be buffered
 * while subsequent loads execute speculatively, producing the state (r1 == 0 && r2 == 0).
 */
public class InstructionReorderingDemo {

    public int a = 0;
    public int b = 0;
    public int r1 = 0;
    public int r2 = 0;

    public void executeThread1() {
        a = 1;      // Store A
        r1 = b;     // Load B
    }

    public void executeThread2() {
        b = 1;      // Store B
        r2 = a;     // Load A
    }

    public void reset() {
        a = 0;
        b = 0;
        r1 = 0;
        r2 = 0;
    }
}
