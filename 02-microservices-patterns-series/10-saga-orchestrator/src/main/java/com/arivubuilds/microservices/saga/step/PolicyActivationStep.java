package com.arivubuilds.microservices.saga.step;

import com.arivubuilds.microservices.saga.model.PolicySagaContext;
import com.arivubuilds.microservices.saga.model.SagaState;

public class PolicyActivationStep implements SagaStep {

    private final boolean simulateFailure;

    public PolicyActivationStep(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    @Override
    public String getName() {
        return "PolicyActivationStep";
    }

    @Override
    public boolean execute(PolicySagaContext context) {
        if (simulateFailure) {
            context.recordAudit("[PolicyLedger] Activation failed: Database unique constraint conflict");
            return false;
        }
        context.transitionTo(SagaState.POLICY_ACTIVATED);
        context.recordAudit("[PolicyLedger] Policy active in immutable ledger: " + context.getPolicyId());
        return true;
    }

    @Override
    public void compensate(PolicySagaContext context) {
        context.recordAudit("[PolicyLedger] Compensation executed: Marked policy status as VOID");
    }
}
