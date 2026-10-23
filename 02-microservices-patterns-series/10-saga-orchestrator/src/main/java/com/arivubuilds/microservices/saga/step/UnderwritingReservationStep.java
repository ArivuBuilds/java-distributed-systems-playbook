package com.arivubuilds.microservices.saga.step;

import com.arivubuilds.microservices.saga.model.PolicySagaContext;
import com.arivubuilds.microservices.saga.model.SagaState;

public class UnderwritingReservationStep implements SagaStep {

    private final boolean simulateFailure;

    public UnderwritingReservationStep(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    @Override
    public String getName() {
        return "UnderwritingReservationStep";
    }

    @Override
    public boolean execute(PolicySagaContext context) {
        if (simulateFailure) {
            context.recordAudit("[UnderwritingService] Quota reservation failed");
            return false;
        }
        context.transitionTo(SagaState.UNDERWRITING_RESERVED);
        context.recordAudit("[UnderwritingService] Risk profile approved, quota reserved");
        return true;
    }

    @Override
    public void compensate(PolicySagaContext context) {
        context.recordAudit("[UnderwritingService] Compensation executed: Releasing underwriting quota");
    }
}
