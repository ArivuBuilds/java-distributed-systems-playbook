package com.arivubuilds.internals.falsesharing;

public class UnpaddedMetricsCounter {
    public volatile long producerCount = 0L;
    public volatile long consumerCount = 0L;
}
