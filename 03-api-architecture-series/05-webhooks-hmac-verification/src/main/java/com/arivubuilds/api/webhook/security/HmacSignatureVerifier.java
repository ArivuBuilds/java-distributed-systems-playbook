package com.arivubuilds.api.webhook.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HmacSignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long MAX_ALLOWED_DRIFT_SECONDS = 300; // 5-minute tolerance

    // In-memory processed cache to protect against replay attacks within the tolerance window
    private final Set<String> processedDeliveryIds = ConcurrentHashMap.newKeySet();

    /**
     * Verifies signature and rejects replayed or expired webhooks.
     * Computes HMAC over: timestamp + "." + rawBody
     */
    public boolean verify(String rawBody, String signatureHeader, String timestampHeader, String deliveryId, String secretKey) {
        if (signatureHeader == null || timestampHeader == null || deliveryId == null) {
            return false;
        }

        // 1. Replay attack check: ensure unique delivery ID
        if (!processedDeliveryIds.add(deliveryId)) {
            return false; // Delivery ID already processed
        }

        // 2. Timestamp drift validation
        long timestampSeconds;
        try {
            timestampSeconds = Long.parseLong(timestampHeader);
        } catch (NumberFormatException e) {
            return false;
        }

        long currentSeconds = Instant.now().getEpochSecond();
        if (Math.abs(currentSeconds - timestampSeconds) > MAX_ALLOWED_DRIFT_SECONDS) {
            return false; // Timestamp outside acceptable tolerance window
        }

        // 3. Compute expected HMAC-SHA256 signature
        String signedPayload = timestampHeader + "." + rawBody;
        String expectedSignature = calculateHmac(signedPayload, secretKey);

        // 4. Constant-time comparison to guard against side-channel timing attacks
        byte[] expectedBytes = expectedSignature.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = signatureHeader.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }

    public String calculateHmac(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to calculate HMAC signature", e);
        }
    }
}