package com.arivubuilds.microservices.saga.step;

import com.arivubuilds.microservices.saga.model.PolicySagaContext;
import com.arivubuilds.microservices.saga.model.SagaState;

public class PaymentCaptureStep implements SagaStep {

    private final boolean simulateFailure;

    public PaymentCaptureStep(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    @Override
    public String getName() {
        return "PaymentCaptureStep";
    }

    @Override
    public boolean execute(PolicySagaContext context) {
        if (simulateFailure) {
            context.recordAudit("[BillingService] Payment capture failed: Insufficient funds or gateway timeout");
            return false;
        }
        context.transitionTo(SagaState.PAYMENT_CAPTURED);
        context.recordAudit("[BillingService] Successfully charged premium: " + context.getPremiumAmount());
        return true;
    }

    @Override
    public void compensate(PolicySagaContext context) {
        context.recordAudit("[BillingService] Compensation executed: Refunded premium: " + context.getPremiumAmount());
    }
}
