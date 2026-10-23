package com.arivubuilds.microservices.saga.step;

import com.arivubuilds.microservices.saga.model.PolicySagaContext;

/**
 * Contract for a discrete distributed action requiring forward execution and backward compensation.
 */
public interface SagaStep {
    String getName();
    boolean execute(PolicySagaContext context);
    void compensate(PolicySagaContext context);
}
