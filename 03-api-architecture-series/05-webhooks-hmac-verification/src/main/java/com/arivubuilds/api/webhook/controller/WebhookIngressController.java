package com.arivubuilds.api.webhook.controller;

import com.arivubuilds.api.webhook.model.WebhookAck;
import com.arivubuilds.api.webhook.model.WebhookPayload;
import com.arivubuilds.api.webhook.security.HmacSignatureVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookIngressController {

    private static final Logger log = LoggerFactory.getLogger(WebhookIngressController.class);

    private final HmacSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public WebhookIngressController(
            HmacSignatureVerifier signatureVerifier,
            ObjectMapper objectMapper,
            @Value("${webhook.secret:default-secret-key-arivubuilds-2026}") String webhookSecret) {
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/payments")
    public ResponseEntity<WebhookAck> handlePaymentWebhook(
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
            @RequestHeader(value = "X-Webhook-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Webhook-Id", required = false) String deliveryId,
            @RequestBody String rawPayload) {

        boolean isValid = signatureVerifier.verify(rawPayload, signature, timestamp, deliveryId, webhookSecret);

        if (!isValid) {
            log.warn("[Webhook Security] Rejected unauthorized or replayed webhook delivery: id={}", deliveryId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new WebhookAck("REJECTED", "Invalid HMAC signature or expired timestamp", Instant.now()));
        }

        try {
            WebhookPayload payload = objectMapper.readValue(rawPayload, WebhookPayload.class);
            log.info("[Webhook Ingress] Successfully verified and processed event: type={}, id={}",
                    payload.eventType(), payload.eventId());

            return ResponseEntity.ok(new WebhookAck("ACCEPTED", "Webhook processed successfully", Instant.now()));
        } catch (Exception e) {
            log.error("[Webhook Processing] Failed to parse payload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new WebhookAck("MALFORMED", "Invalid JSON payload structure", Instant.now()));
        }
    }
}