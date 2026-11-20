package com.arivubuilds.internals.falsesharing;

public class ManualPaddedMetricsCounter {
    public volatile long producerCount = 0L;
    public long p1, p2, p3, p4, p5, p6, p7;
    public volatile long consumerCount = 0L;
    public long preventDeadCodeElimination() { return p1 + p2 + p3 + p4 + p5 + p6 + p7; }
}
