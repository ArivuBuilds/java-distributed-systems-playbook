package com.arivubuilds.api.idempotency.service;

import com.arivubuilds.api.idempotency.model.IdempotencyRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class IdempotencyStoreService {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public IdempotencyStoreService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Atomically attempts to acquire an IN_PROGRESS lock for the given key.
     * Returns true if acquired (first execution), false if key already exists.
     */
    public boolean tryLock(String idempotencyKey, String fingerprint) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        String serialized = serialize(IdempotencyRecord.inProgress(fingerprint));
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, serialized, LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public Optional<IdempotencyRecord> getRecord(String idempotencyKey) {
        String raw = redisTemplate.opsForValue().get(KEY_PREFIX + idempotencyKey);
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.of(deserialize(raw));
    }

    public void saveCompleted(String idempotencyKey, String fingerprint, int httpStatus, String responseBody) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        String serialized = serialize(IdempotencyRecord.completed(fingerprint, httpStatus, responseBody));
        redisTemplate.opsForValue().set(redisKey, serialized, COMPLETED_TTL);
    }

    public void releaseLock(String idempotencyKey) {
        redisTemplate.delete(KEY_PREFIX + idempotencyKey);
    }

    public String computeFingerprint(String method, String uri, byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(method.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) ':');
            digest.update(uri.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) ':');
            digest.update(body);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    private String serialize(IdempotencyRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize IdempotencyRecord", e);
        }
    }

    private IdempotencyRecord deserialize(String raw) {
        try {
            return objectMapper.readValue(raw, IdempotencyRecord.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize IdempotencyRecord", e);
        }
    }
}