package com.arivubuilds.internals.solid.p04_lsp;

import com.arivubuilds.internals.solid.p04_lsp.bad.LegacyAccount;
import com.arivubuilds.internals.solid.p04_lsp.bad.LegacyFeeBatchProcessor;
import com.arivubuilds.internals.solid.p04_lsp.bad.LockedEscrowAccount;
import com.arivubuilds.internals.solid.p04_lsp.good.CheckingAccount;
import com.arivubuilds.internals.solid.p04_lsp.good.EscrowAccount;
import com.arivubuilds.internals.solid.p04_lsp.good.MaintenanceFeeService;
import com.arivubuilds.internals.solid.p04_lsp.good.SavingsAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LiskovSubstitutionPrincipleTest {
    @Test
    @DisplayName("BAD: LockedEscrowAccount breaks LegacyAccount behavioral contract with UnsupportedOperationException")
    void testLspViolationThrowsAtRuntime() {
        LegacyFeeBatchProcessor processor = new LegacyFeeBatchProcessor();
        List<LegacyAccount> mixedAccounts = List.of(new LegacyAccount(new BigDecimal("1000.00")), new LockedEscrowAccount(new BigDecimal("5000.00")));
        assertThrows(UnsupportedOperationException.class, () -> processor.deductMaintenanceFee(mixedAccounts, new BigDecimal("25.00")));
    }
    @Test
    @DisplayName("GOOD: All WithdrawableAccount subtypes satisfy identical behavioral postconditions safely")
    void testLspCompliantSubtypesHonorPostconditions() {
        MaintenanceFeeService feeService = new MaintenanceFeeService();
        CheckingAccount checking = new CheckingAccount("CHK-01", new BigDecimal("1000.00"));
        SavingsAccount savings = new SavingsAccount("SAVm01", new BigDecimal("2500.00"));
        EscrowAccount escrow = new EscrowAccount("ESC-01", new BigDecimal("9000.00"));
        feeService.deductMaintenanceFee(List.of(checking, savings), new BigDecimal("50.00"));
        assertEquals(new BigDecimal("950.00"), checking.balance());
        assertEquals(new BigDecimal("2450.00"), savings.balance());
        assertEquals(new BigDecimal("9000.00"), escrow.balance());
    }
}
