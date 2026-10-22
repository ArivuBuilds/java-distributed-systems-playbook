package com.arivubuilds.microservices.outbox.repository;

import com.arivubuilds.microservices.outbox.model.OutboxEvent;
import com.arivubuilds.microservices.outbox.model.PolicyRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simulates an ACID relational storage engine.
 * Ensures that business entities and outbox_events commit atomically.
 */
public class TransactionalOutboxRepository {

    private final Map<String, PolicyRecord> policyTable = new ConcurrentHashMap<>();
    private final List<OutboxEvent> outboxTable = new CopyOnWriteArrayList<>();

    public synchronized void savePolicyWithOutboxEvent(PolicyRecord policy, OutboxEvent event, boolean simulateFailure) {
        if (simulateFailure) {
            throw new RuntimeException("Simulated Database I/O Failure: Rolling back transaction");
        }
        policyTable.put(policy.policyId(), policy);
        outboxTable.add(event);
    }

    public Optional<PolicyRecord> findPolicyById(String policyId) {
        return Optional.ofNullable(policyTable.get(policyId));
    }

    public List<OutboxEvent> findAllOutboxEvents() {
        return Collections.unmodifiableList(outboxTable);
    }

    public void clear() {
        policyTable.clear();
        outboxTable.clear();
    }
}
