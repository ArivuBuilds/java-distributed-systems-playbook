package com.arivubuilds.microservices.retry.config;

import com.arivubuilds.microservices.retry.backoff.DecorrelatedJitterBackoff;
import com.arivubuilds.microservices.retry.client.DownstreamSettlementClient;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DecorrelatedRetryConfig {

    public static final String RETRY_NAME = "settlementDecorrelatedRetry";

    private static final ThreadLocal<List<Long>> ATTEMPT_DELAYS =
            ThreadLocal.withInitial(ArrayList::new);

    @Bean
    public DecorrelatedJitterBackoff decorrelatedJitterBackoff(
            @Value("${retry.decorrelated.base-delay-ms:40}") long baseDelayMs,
            @Value("${retry.decorrelated.max-cap-ms:600}") long maxCapMs) {
        return new DecorrelatedJitterBackoff(
                baseDelayMs,
                maxCapMs,
                delay -> ATTEMPT_DELAYS.get().add(delay)
        );
    }

    @Bean
    public Retry settlementRetry(
            RetryRegistry retryRegistry,
            DecorrelatedJitterBackoff backoff,
            @Value("${retry.decorrelated.max-attempts:4}") int maxAttempts) {

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .intervalBiFunction(backoff)
                .retryOnException(DownstreamSettlementClient::isTransientFailure)
                .build();

        return retryRegistry.retry(RETRY_NAME, config);
    }

    public static List<Long> drainRecordedDelays() {
        List<Long> snapshot = List.copyOf(ATTEMPT_DELAYS.get());
        ATTEMPT_DELAYS.remove();
        return snapshot;
    }
}