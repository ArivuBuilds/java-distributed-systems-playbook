package com.arivubuilds.internals.solid.p07_capstone;

import com.arivubuilds.internals.solid.p07_capstone.bad.MonolithicClaimSettlementService;
import com.arivubuilds.internals.solid.p07_capstone.good.adapter.LedgerClaimAuditAdapter;
import com.arivubuilds.internals.solid.p07_capstone.good.adapter.SmsClaimReceiptAdapter;
import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimEligibilityValidator;
import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimPayoutCoordinator;
import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimRequest;
import com.arivubuilds.internals.solid.p07_capstone.good.strategy.HealthClaimPayoutStrategy;
import com.arivubuilds.internals.solid.p07_capstone.good.strategy.MotorClaimPayoutStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolidCapstoneTest {
    @Test
    @DisplayName("BAD: Monolithic service violates SRP, OCP, LSP, ISP, and DIP at runtime")
    void testMonolithicClaimServiceBrittleness() {
        MonolithicClaimSettlementService service = new MonolithicClaimSettlementService();
        assertThrows(UnsupportedOperationException.class, () -> service.settleClaim("CLM-99", new BigDecimal("50000.00"), "LIFE", "+91-9876543210"));
    }
    @Test
    @DisplayName("GOOD: ClaimPayoutCoordinator unifies SRP, OCP, LSP, ISP, and DIP")
    void testEnterpriseSolidCapstone() {
        ClaimPayoutCoordinator coordinator = new ClaimPayoutCoordinator(new ClaimEligibilityValidator(), Map.of("HEALTH", new HealthClaimPayoutStrategy(), "MOTOR", new MotorClaimPayoutStrategy(), "LIFE", req -> "ANNUITY_LIFE_PAYOUT::" + req.amount()), new LedgerClaimAuditAdapter(), new SmsClaimReceiptAdapter());
        String result = coordinator.processClaim(new ClaimRequest("CLM-101", new BigDecimal("75000.00"), "LIFE", "+91-9876543210"));
        assertEquals("AUDIT_LEDGER:CLM-101 | ANNUITY_LIFE_PAYOUT::75000.00 | SMS_TO:+91-9876543210", result);
    }
}
