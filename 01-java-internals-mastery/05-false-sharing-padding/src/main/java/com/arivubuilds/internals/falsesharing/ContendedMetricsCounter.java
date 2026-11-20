package com.arivubuilds.internals.falsesharing;

import jdk.internal.vm.annotation.Contended;

public class ContendedMetricsCounter {
    @Contended
    public volatile long producerCount = 0L;
    @Contended
    public volatile long consumerCount = 0L;
}
