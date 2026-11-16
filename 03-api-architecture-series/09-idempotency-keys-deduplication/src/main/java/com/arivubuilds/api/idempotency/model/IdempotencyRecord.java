package com.arivubuilds.api.idempotency.model;

public record IdempotencyRecord(
        Status status,
        String requestFingerprint,
        int httpStatus,
        String responseBody
) {
    public enum Status {
        IN_PROGRESS,
        COMPLETED
    }

    public static IdempotencyRecord inProgress(String fingerprint) {
        return new IdempotencyRecord(Status.IN_PROGRESS, fingerprint, 0, null);
    }

    public static IdempotencyRecord completed(String fingerprint, int httpStatus, String responseBody) {
        return new IdempotencyRecord(Status.COMPLETED, fingerprint, httpStatus, responseBody);
    }
}