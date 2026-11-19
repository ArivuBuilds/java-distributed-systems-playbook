package com.arivubuilds.microservices.circuitbreaker.service;

import com.arivubuilds.microservices.circuitbreaker.client.DownstreamCreditClient;
import com.arivubuilds.microservices.circuitbreaker.dto.CreditAssessmentResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CreditEvaluationService {

    public static final String BREAKER_NAME = "creditScoringBreaker";

    private final DownstreamCreditClient creditClient;
    private final CircuitBreakerRegistry breakerRegistry;
    private final Map<String, CreditAssessmentResponse> lastKnownGoodCache = new ConcurrentHashMap<>();

    public CreditEvaluationService(DownstreamCreditClient creditClient,
                                   CircuitBreakerRegistry breakerRegistry) {
        this.creditClient = creditClient;
        this.breakerRegistry = breakerRegistry;
    }

    @CircuitBreaker(name = BREAKER_NAME, fallbackMethod = "evaluateCreditFallback")
    public CreditAssessmentResponse evaluateCredit(String customerId) {
        CreditAssessmentResponse live = creditClient.fetchLiveCreditScore(customerId);
        String currentState = getCurrentBreakerState();
        CreditAssessmentResponse enriched = new CreditAssessmentResponse(
                live.customerId(),
                live.creditScore(),
                live.riskTier(),
                "LIVE_UPSTREAM",
                currentState
        );
        lastKnownGoodCache.put(customerId, enriched);
        return enriched;
    }

    @SuppressWarnings("unused")
    public CreditAssessmentResponse evaluateCreditFallback(String customerId, Throwable cause) {
        String currentState = getCurrentBreakerState();
        CreditAssessmentResponse cached = lastKnownGoodCache.get(customerId);
        if (cached != null) {
            return new CreditAssessmentResponse(
                    customerId,
                    cached.creditScore(),
                    cached.riskTier(),
                    "STALE_CACHE_FALLBACK",
                    currentState
            );
        }
        return new CreditAssessmentResponse(
                customerId,
                650,
                "PROVISIONAL_STANDARD",
                "DEFAULT_DEGRADED_FALLBACK",
                currentState
        );
    }

    public String getCurrentBreakerState() {
        io.github.resilience4j.circuitbreaker.CircuitBreaker cb = breakerRegistry.circuitBreaker(BREAKER_NAME);
        return cb.getState().name();
    }

    public void resetBreaker() {
        breakerRegistry.circuitBreaker(BREAKER_NAME).reset();
        lastKnownGoodCache.clear();
    }
}