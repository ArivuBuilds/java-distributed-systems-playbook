package com.arivubuilds.microservices.saga.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared context and audit ledger carried through the distributed execution lifecycle.
 */
public class PolicySagaContext {

    private final String sagaId;
    private final String policyId;
    private final String applicantName;
    private final BigDecimal premiumAmount;
    private SagaState currentState;
    private final List<String> executionHistory = new ArrayList<>();

    public PolicySagaContext(String sagaId, String policyId, String applicantName, BigDecimal premiumAmount) {
        this.sagaId = sagaId;
        this.policyId = policyId;
        this.applicantName = applicantName;
        this.premiumAmount = premiumAmount;
        this.currentState = SagaState.STARTED;
        recordAudit("Saga initialized in state: " + SagaState.STARTED);
    }

    public synchronized void transitionTo(SagaState newState) {
        this.currentState = newState;
        recordAudit("State transitioned to: " + newState);
    }

    public synchronized void recordAudit(String message) {
        this.executionHistory.add(message);
    }

    public String getSagaId() { return sagaId; }
    public String getPolicyId() { return policyId; }
    public String getApplicantName() { return applicantName; }
    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public synchronized SagaState getCurrentState() { return currentState; }
    public synchronized List<String> getExecutionHistory() {
        return Collections.unmodifiableList(executionHistory);
    }
}
