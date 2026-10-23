package com.arivubuilds.microservices.saga;

import com.arivubuilds.microservices.saga.model.PolicySagaContext;
import com.arivubuilds.microservices.saga.model.SagaState;
import com.arivubuilds.microservices.saga.orchestrator.PolicySagaOrchestrator;
import com.arivubuilds.microservices.saga.step.PaymentCaptureStep;
import com.arivubuilds.microservices.saga.step.PolicyActivationStep;
import com.arivubuilds.microservices.saga.step.SagaStep;
import com.arivubuilds.microservices.saga.step.UnderwritingReservationStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicySagaOrchestratorTest {

    @Test
    @DisplayName("Saga: Happy path completes all steps and transitions to COMPLETED")
    void testHappyPathSagaExecution() {
        List<SagaStep> steps = List.of(
            new UnderwritingReservationStep(false),
            new PaymentCaptureStep(false),
            new PolicyActivationStep(false)
        );

        PolicySagaOrchestrator orchestrator = new PolicySagaOrchestrator(steps);
        PolicySagaContext context = new PolicySagaContext(
            "SAGA-9001",
            "POL-IND-402",
            "Marcus Sterling",
            new BigDecimal("1250.00")
        );

        boolean result = orchestrator.executeSaga(context);

        assertTrue(result, "Saga should succeed when all forward steps complete");
        assertEquals(SagaState.COMPLETED, context.getCurrentState());
        assertTrue(context.getExecutionHistory().contains("[PolicyLedger] Policy active in immutable ledger: POL-IND-402"));
    }

    @Test
    @DisplayName("Saga: Failure at payment step compensates earlier underwriting quota")
    void testCompensationTriggeredOnStepFailure() {
        List<SagaStep> steps = List.of(
            new UnderwritingReservationStep(false),
            new PaymentCaptureStep(true), // Simulated failure
            new PolicyActivationStep(false)
        );

        PolicySagaOrchestrator orchestrator = new PolicySagaOrchestrator(steps);
        PolicySagaContext context = new PolicySagaContext(
            "SAGA-9002",
            "POL-IND-403",
            "Elena Rostova",
            new BigDecimal("2400.00")
        );

        boolean result = orchestrator.executeSaga(context);

        assertFalse(result, "Saga should fail when payment step fails");
        assertEquals(SagaState.COMPENSATED, context.getCurrentState(), "Context state must mark compensation complete");
        
        // Assert that Step 1 was compensated
        assertTrue(context.getExecutionHistory().contains("[UnderwritingService] Compensation executed: Releasing underwriting quota"));
        
        // Assert that Step 3 was never executed
        assertFalse(context.getExecutionHistory().contains("[PolicyLedger] Policy active in immutable ledger: POL-IND-403"));
    }
}
