package com.arivubuilds.microservices.outbox;

import com.arivubuilds.microservices.outbox.model.OutboxEvent;
import com.arivubuilds.microservices.outbox.model.PolicyRecord;
import com.arivubuilds.microservices.outbox.repository.TransactionalOutboxRepository;
import com.arivubuilds.microservices.outbox.service.PolicyIssuanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TransactionalOutboxTest {

    private TransactionalOutboxRepository repository;
    private PolicyIssuanceService service;

    @BeforeEach
    void setUp() {
        repository = new TransactionalOutboxRepository();
        service = new PolicyIssuanceService(repository);
    }

    @Test
    @DisplayName("Transactional Outbox: Business state and outbox event commit atomically")
    void testAtomicCommitSuccess() {
        String policyId = "POL-10024";
        service.bindAndIssuePolicy(
            policyId,
            "Sophia Thorne",
            new BigDecimal("750000.00"),
            new BigDecimal("1850.00"),
            "trace-abc-123"
        );

        Optional<PolicyRecord> policy = repository.findPolicyById(policyId);
        assertTrue(policy.isPresent(), "Policy record must be persisted");
        assertEquals("BOUND", policy.get().status());

        List<OutboxEvent> events = repository.findAllOutboxEvents();
        assertEquals(1, events.size(), "Exactly one outbox event must be staged for CDC publication");

        OutboxEvent event = events.get(0);
        assertEquals("POLICY", event.aggregateType());
        assertEquals(policyId, event.aggregateId());
        assertEquals("PolicyBoundEvent", event.eventType());
        assertTrue(event.payloadJson().contains("Sophia Thorne"));
    }

    @Test
    @DisplayName("Dual-Write Hazard Mitigation: If transaction fails, both business record and outbox event roll back")
    void testAtomicRollbackOnFailure() {
        String policyId = "POL-99999";

        assertThrows(RuntimeException.class, () ->
            service.bindAndIssuePolicy(
                policyId,
                "Jonathan Vance",
                new BigDecimal("500000.00"),
                new BigDecimal("1200.00"),
                "trace-err-456",
                true // Trigger simulated failure
            )
        );

        Optional<PolicyRecord> policy = repository.findPolicyById(policyId);
        assertFalse(policy.isPresent(), "Policy record must NOT exist if transaction fails");

        List<OutboxEvent> events = repository.findAllOutboxEvents();
        assertTrue(events.isEmpty(), "Outbox table must NOT contain orphaned events on transaction rollback");
    }
}
