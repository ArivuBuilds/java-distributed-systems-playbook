package com.arivubuilds.api.ratelimit.interceptor;

import com.arivubuilds.api.ratelimit.service.RateLimitResult;
import com.arivubuilds.api.ratelimit.service.TokenBucketRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientKey = request.getHeader("X-Client-Id");
        if (clientKey == null || clientKey.isBlank()) {
            clientKey = request.getRemoteAddr();
        }

        // Configuration: Capacity = 5 tokens, Refill = 2 tokens/sec
        long capacity = 5;
        double refillPerSec = 2.0;

        RateLimitResult result = rateLimiter.tryAcquire(clientKey, capacity, refillPerSec);

        // Standard HTTP Rate Limit Headers
        response.addHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));

        if (!result.allowed()) {
            response.addHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  "type": "https://api.arivubuilds.com/errors/too-many-requests",
                  "title": "Too Many Requests",
                  "status": 429,
                  "detail": "Rate limit quota exceeded. Try again in %d seconds."
                }
                """.formatted(result.retryAfterSeconds()));
            return false;
        }

        return true;
    }
}