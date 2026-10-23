package com.arivubuilds.microservices.saga.orchestrator;

import com.arivubuilds.microservices.saga.model.PolicySagaContext;
import com.arivubuilds.microservices.saga.model.SagaState;
import com.arivubuilds.microservices.saga.step.SagaStep;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Saga Orchestrator: Coordinates forward step invocation and triggers backward 
 * compensating transactions in reverse order upon step failure.
 */
public class PolicySagaOrchestrator {

    private final List<SagaStep> steps;

    public PolicySagaOrchestrator(List<SagaStep> steps) {
        this.steps = steps;
    }

    public boolean executeSaga(PolicySagaContext context) {
        Deque<SagaStep> executedSteps = new ArrayDeque<>();

        for (SagaStep step : steps) {
            context.recordAudit("Executing step: " + step.getName());
            boolean success = false;
            try {
                success = step.execute(context);
            } catch (Exception ex) {
                context.recordAudit("Exception in step " + step.getName() + ": " + ex.getMessage());
                success = false;
            }

            if (success) {
                executedSteps.push(step);
            } else {
                rollback(context, executedSteps);
                return false;
            }
        }

        context.transitionTo(SagaState.COMPLETED);
        context.recordAudit("Saga successfully finalized");
        return true;
    }

    private void rollback(PolicySagaContext context, Deque<SagaStep> executedSteps) {
        context.transitionTo(SagaState.COMPENSATING);
        context.recordAudit("Triggering backward compensation in LIFO order...");

        while (!executedSteps.isEmpty()) {
            SagaStep step = executedSteps.pop();
            try {
                step.compensate(context);
            } catch (Exception ex) {
                // In production, failed compensations are emitted to a Dead Letter Queue (DLQ)
                // for human-in-the-loop remediation to avoid silent leaks.
                context.recordAudit("CRITICAL: Failed to compensate step " + step.getName() + ": " + ex.getMessage());
            }
        }

        context.transitionTo(SagaState.COMPENSATED);
        context.recordAudit("All executed steps compensated. Saga marked as " + SagaState.COMPENSATED);
    }
}
