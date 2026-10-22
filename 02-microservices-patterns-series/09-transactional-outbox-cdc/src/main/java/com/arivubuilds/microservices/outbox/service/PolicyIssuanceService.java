package com.arivubuilds.microservices.outbox.service;

import com.arivubuilds.microservices.outbox.model.OutboxEvent;
import com.arivubuilds.microservices.outbox.model.PolicyRecord;
import com.arivubuilds.microservices.outbox.repository.TransactionalOutboxRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Executes business state transitions and stages domain events directly
 * to the outbox table within a single ACID transaction boundary.
 */
public class PolicyIssuanceService {

    private final TransactionalOutboxRepository repository;

    public PolicyIssuanceService(TransactionalOutboxRepository repository) {
        this.repository = repository;
    }

    public void bindAndIssuePolicy(String policyId, String holderName, BigDecimal coverage, BigDecimal premium, String traceId) {
        bindAndIssuePolicy(policyId, holderName, coverage, premium, traceId, false);
    }

    public void bindAndIssuePolicy(String policyId, String holderName, BigDecimal coverage, BigDecimal premium, String traceId, boolean simulateCrash) {
        Instant now = Instant.now();
        PolicyRecord policy = new PolicyRecord(policyId, holderName, coverage, premium, "BOUND", now);

        String payloadJson = String.format(
            "{\"policyId\":\"%s\",\"holderName\":\"%s\",\"coverage\":%s,\"premium\":%s,\"status\":\"BOUND\"}",
            policyId, holderName, coverage.toPlainString(), premium.toPlainString()
        );

        OutboxEvent outboxEvent = new OutboxEvent(
            UUID.randomUUID(),
            "POLICY",
            policyId,
            "PolicyBoundEvent",
            payloadJson,
            traceId,
            now
        );

        repository.savePolicyWithOutboxEvent(policy, outboxEvent, simulateCrash);
    }
}
