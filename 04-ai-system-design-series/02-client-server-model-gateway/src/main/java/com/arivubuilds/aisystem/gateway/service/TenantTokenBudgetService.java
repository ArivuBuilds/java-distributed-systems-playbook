package com.arivubuilds.aisystem.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TenantTokenBudgetService {

    private final int defaultTenantLimit;
    private final ConcurrentHashMap<String, AtomicInteger> remainingTokens = new ConcurrentHashMap<>();

    public TenantTokenBudgetService(@Value("${gateway.budget.default-tenant-token-limit:1000}") int defaultTenantLimit) {
        this.defaultTenantLimit = defaultTenantLimit;
    }

    public int reserveOrThrow(String tenantId, int estimatedTokens) {
        AtomicInteger bucket = remainingTokens.computeIfAbsent(tenantId, id -> new AtomicInteger(defaultTenantLimit));
        while (true) {
            int current = bucket.get();
            if (current < estimatedTokens) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Token budget exceeded for tenant '" + tenantId + "'. Remaining: " + current + ", requested: " + estimatedTokens
                );
            }
            int updated = current - estimatedTokens;
            if (bucket.compareAndSet(current, updated)) {
                return updated;
            }
        }
    }

    public int getRemaining(String tenantId) {
        return remainingTokens.computeIfAbsent(tenantId, id -> new AtomicInteger(defaultTenantLimit)).get();
    }

    public void resetTenant(String tenantId, int newQuota) {
        remainingTokens.put(tenantId, new AtomicInteger(newQuota));
    }
}