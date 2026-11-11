package com.arivubuilds.api.webhook;

import com.arivubuilds.api.webhook.security.HmacSignatureVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookIngressSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HmacSignatureVerifier signatureVerifier;

    @Value("${webhook.secret:default-secret-key-arivubuilds-2026}")
    private String secretKey;

    private static final String SAMPLE_PAYLOAD = """
            {
              "eventId": "evt_99182",
              "eventType": "payment.succeeded",
              "accountId": "acc_india_101",
              "amount": 4999.00,
              "currency": "INR",
              "occurredAt": "2026-11-10T18:00:00Z"
            }
            """;

    @Test
    @DisplayName("Valid HMAC Signature: Successfully verifies and returns HTTP 200 ACCEPTED")
    void testValidWebhookSignature() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String deliveryId = UUID.randomUUID().toString();
        String signature = signatureVerifier.calculateHmac(timestamp + "." + SAMPLEPayloadTrimmed(), secretKey);

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", timestamp)
                        .header("X-Webhook-Id", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAMPLEPayloadTrimmed()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("Tampered Payload: Rejects modified payload with HTTP 401 UNAUTHORIZED")
    void testTamperedPayloadRejected() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String deliveryId = UUID.randomUUID().toString();
        String signature = signatureVerifier.calculateHmac(timestamp + "." + SAMPLEPayloadTrimmed(), secretKey);

        String tamperedPayload = SAMPLEPayloadTrimmed().replace("4999.00", "9999.00");

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", timestamp)
                        .header("X-Webhook-Id", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tamperedPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Replay Attack: Rejects identical delivery ID on second submission")
    void testReplayAttackRejected() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String deliveryId = UUID.randomUUID().toString();
        String signature = signatureVerifier.calculateHmac(timestamp + "." + SAMPLEPayloadTrimmed(), secretKey);

        // First attempt succeeds
        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", timestamp)
                        .header("X-Webhook-Id", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAMPLEPayloadTrimmed()))
                .andExpect(status().isOk());

        // Replayed request with the same deliveryId is rejected
        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", timestamp)
                        .header("X-Webhook-Id", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAMPLEPayloadTrimmed()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Expired Timestamp: Rejects request with timestamp outside 300-second window")
    void testExpiredTimestampRejected() throws Exception {
        // Timestamp 10 minutes in the past
        String expiredTimestamp = String.valueOf(Instant.now().minusSeconds(600).getEpochSecond());
        String deliveryId = UUID.randomUUID().toString();
        String signature = signatureVerifier.calculateHmac(expiredTimestamp + "." + SAMPLEPayloadTrimmed(), secretKey);

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header("X-Webhook-Signature", signature)
                        .header("X-Webhook-Timestamp", expiredTimestamp)
                        .header("X-Webhook-Id", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAMPLEPayloadTrimmed()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    private String SAMPLEPayloadTrimmed() {
        return SAMPLE_PAYLOAD.trim();
    }
}