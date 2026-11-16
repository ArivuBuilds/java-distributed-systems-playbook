package com.arivubuilds.api.idempotency.filter;

import com.arivubuilds.api.idempotency.model.IdempotencyRecord;
import com.arivubuilds.api.idempotency.service.IdempotencyStoreService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private final IdempotencyStoreService storeService;

    public IdempotencyFilter(IdempotencyStoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!HttpMethod.POST.matches(request.getMethod()) || !request.getRequestURI().startsWith("/api/v1/transfers")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            writeProblemResponse(response, HttpStatus.BAD_REQUEST,
                    "Missing Idempotency-Key",
                    "Header 'Idempotency-Key' is mandatory for financial mutation endpoints.");
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String fingerprint = storeService.computeFingerprint(
                cachedRequest.getMethod(),
                cachedRequest.getRequestURI(),
                cachedRequest.getCachedBody()
        );

        if (!storeService.tryLock(idempotencyKey, fingerprint)) {
            Optional<IdempotencyRecord> existingOpt = storeService.getRecord(idempotencyKey);
            if (existingOpt.isEmpty()) {
                writeProblemResponse(response, HttpStatus.CONFLICT,
                        "Concurrent Request Conflict",
                        "Request with this Idempotency-Key is currently being processed. Retry shortly.");
                return;
            }

            IdempotencyRecord existing = existingOpt.get();

            if (!existing.requestFingerprint().equals(fingerprint)) {
                writeProblemResponse(response, HttpStatus.UNPROCESSABLE_ENTITY,
                        "Idempotency Payload Mismatch",
                        "Idempotency-Key was previously used with a different request payload.");
                return;
            }

            if (existing.status() == IdempotencyRecord.Status.IN_PROGRESS) {
                writeProblemResponse(response, HttpStatus.CONFLICT,
                        "Concurrent Request In-Flight",
                        "A request with this Idempotency-Key is already in progress.");
                return;
            }

            response.setStatus(existing.httpStatus());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("X-Idempotency-Replay", "true");
            response.getWriter().write(existing.responseBody());
            return;
        }

        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(cachedRequest, cachedResponse);

            int status = cachedResponse.getStatus();
            String responseBody = new String(cachedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

            if (status >= 200 && status < 500) {
                storeService.saveCompleted(idempotencyKey, fingerprint, status, responseBody);
            } else {
                storeService.releaseLock(idempotencyKey);
            }

            cachedResponse.copyBodyToResponse();
        } catch (Exception ex) {
            storeService.releaseLock(idempotencyKey);
            throw ex;
        }
    }

    private void writeProblemResponse(HttpServletResponse response, HttpStatus status, String title, String detail)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {
                  "type": "https://api.arivubuilds.com/errors/idempotency",
                  "title": "%s",
                  "status": %d,
                  "detail": "%s"
                }
                """.formatted(title, status.value(), detail));
    }
}