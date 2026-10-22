package com.arivubuilds.microservices.cqrs.command;

import com.arivubuilds.microservices.cqrs.event.PolicyUnderwrittenEvent;
import com.arivubuilds.microservices.cqrs.projection.PolicyProjectionEngine;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command Model: Handles validation, domain invariant enforcement, 
 * and persistence to the normalized write store.
 */
public class PolicyCommandHandler {

    private final Map<String, IssuePolicyCommand> writeDatabase = new ConcurrentHashMap<>();
    private final PolicyProjectionEngine projectionEngine;

    public PolicyCommandHandler(PolicyProjectionEngine projectionEngine) {
        this.projectionEngine = projectionEngine;
    }

    public synchronized void handle(IssuePolicyCommand command) {
        if (writeDatabase.containsKey(command.policyId())) {
            throw new IllegalStateException("Policy ID " + command.policyId() + " already exists in write store");
        }

        // 1. Commit to write model (ACID normalized store)
        writeDatabase.put(command.policyId(), command);

        // 2. Emit domain event to update asynchronous read models / projections
        PolicyUnderwrittenEvent event = new PolicyUnderwrittenEvent(
            command.policyId(),
            command.policyHolder(),
            command.planType(),
            command.coverageAmount(),
            command.annualPremium(),
            Instant.now()
        );

        projectionEngine.onPolicyUnderwritten(event);
    }
}
