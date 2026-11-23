package com.arivubuilds.microservices.retry.backoff;

import io.github.resilience4j.core.IntervalBiFunction;
import io.github.resilience4j.core.functions.Either;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongConsumer;

public class DecorrelatedJitterBackoff implements IntervalBiFunction<Object> {

    private final long baseDelayMs;
    private final long maxCapMs;
    private final ThreadLocal<Long> previousSleepMs;
    private final LongConsumer delayObserver;

    public DecorrelatedJitterBackoff(long baseDelayMs, long maxCapMs, LongConsumer delayObserver) {
        this.baseDelayMs = baseDelayMs;
        this.maxCapMs = maxCapMs;
        this.previousSleepMs = ThreadLocal.withInitial(() -> baseDelayMs);
        this.delayObserver = delayObserver;
    }

    @Override
    public Long apply(Integer attempt, Either<Throwable, Object> outcome) {
        if (attempt == null || attempt <= 1) {
            previousSleepMs.set(baseDelayMs);
        }
        long prev = previousSleepMs.get();
        long upperBoundExclusive = Math.max(baseDelayMs + 1, (prev * 3) + 1);
        long jittered = ThreadLocalRandom.current().nextLong(baseDelayMs, upperBoundExclusive);
        long boundedSleep = Math.min(maxCapMs, jittered);

        previousSleepMs.set(boundedSleep);
        if (delayObserver != null) {
            delayObserver.accept(boundedSleep);
        }
        return boundedSleep;
    }

    public void resetThreadState() {
        previousSleepMs.remove();
    }
}
