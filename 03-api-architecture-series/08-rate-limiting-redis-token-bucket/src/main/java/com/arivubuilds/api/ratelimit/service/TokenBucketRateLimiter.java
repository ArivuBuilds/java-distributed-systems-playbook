package com.arivubuilds.api.ratelimit.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenBucketRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> rateLimitScript;

    public TokenBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("token_bucket.lua")));
        this.rateLimitScript.setResultType(List.class);
    }

    /**
     * Executes the atomic Lua script against Redis.
     * @param clientKey Unique client identifier (API Key or IP)
     * @param capacity Burst limit
     * @param refillTokensPerSecond Sustained throughput
     */
    public RateLimitResult tryAcquire(String clientKey, long capacity, double refillTokensPerSecond) {
        String redisKey = "ratelimit:" + clientKey;
        double refillRatePerMs = refillTokensPerSecond / 1000.0;
        long now = Instant.now().toEpochMilli();

        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(
                rateLimitScript,
                List.of(redisKey),
                String.valueOf(capacity),
                String.valueOf(refillRatePerMs),
                "1",
                String.valueOf(now)
        );

        if (result == null || result.isEmpty()) {
            return new RateLimitResult(true, capacity, capacity - 1, 0);
        }

        boolean allowed = result.get(0) == 1L;
        long remaining = result.get(1);
        long waitMs = result.get(2);
        long retryAfterSec = (waitMs > 0) ? (long) Math.ceil(waitMs / 1000.0) : 0;

        return new RateLimitResult(allowed, capacity, remaining, retryAfterSec);
    }
}