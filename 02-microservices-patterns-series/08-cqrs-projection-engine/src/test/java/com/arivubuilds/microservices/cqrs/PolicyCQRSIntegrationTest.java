package com.arivubuilds.microservices.cqrs;

import com.arivubuilds.microservices.cqrs.command.IssuePolicyCommand;
import com.arivubuilds.microservices.cqrs.command.PolicyCommandHandler;
import com.arivubuilds.microservices.cqrs.projection.PolicyProjectionEngine;
import com.arivubuilds.microservices.cqrs.query.PolicyDashboardSummaryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PolicyCQRSIntegrationTest {

    private PolicyProjectionEngine projectionEngine;
    private PolicyCommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        projectionEngine = new PolicyProjectionEngine();
        commandHandler = new PolicyCommandHandler(projectionEngine);
    }

    @Test
    @DisplayName("CQRS: Executing an issuance command generates an asynchronous read projection")
    void testCommandUpdatesReadProjection() {
        String policyId = "POL-CQRS-8801";
        IssuePolicyCommand command = new IssuePolicyCommand(
            policyId,
            "Alexander Vance",
            "TERM_LIFE_ELITE",
            new BigDecimal("1500000.00"),
            new BigDecimal("3200.00")
        );

        commandHandler.handle(command);

        Optional<PolicyDashboardSummaryView> viewOpt = projectionEngine.findSummaryByPolicyId(policyId);
        assertTrue(viewOpt.isPresent(), "Read projection must be generated from domain event");

        PolicyDashboardSummaryView view = viewOpt.get();
        assertEquals(policyId, view.policyId());
        assertEquals("Alexander Vance", view.policyHolder());
        assertEquals("HIGH_NET_WORTH", view.riskTier(), "Projection should calculate pre-aggregated risk tier");
        assertNotNull(view.lastSynchronizedAt());
    }

    @Test
    @DisplayName("CQRS: Command validation rejects invalid commands before projection updates")
    void testCommandValidationRejection() {
        assertThrows(IllegalArgumentException.class, () ->
            new IssuePolicyCommand("POL-ERR-01", "Invalid Holder", "TERM", new BigDecimal("-100"), new BigDecimal("100"))
        );

        assertEquals(0, projectionEngine.totalProjections(), "No projections should exist for failed commands");
    }
}
