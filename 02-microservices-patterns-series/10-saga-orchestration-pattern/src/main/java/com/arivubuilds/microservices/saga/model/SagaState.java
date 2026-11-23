package com.arivubuilds.microservices.saga.model;

/**
 * Finite State Machine representing lifecycle states of a distributed Saga transaction.
 */
public enum SagaState {
    STARTED,
    UNDERWRITING_RESERVED,
    PAYMENT_CAPTURED,
    POLICY_ACTIVATED,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
