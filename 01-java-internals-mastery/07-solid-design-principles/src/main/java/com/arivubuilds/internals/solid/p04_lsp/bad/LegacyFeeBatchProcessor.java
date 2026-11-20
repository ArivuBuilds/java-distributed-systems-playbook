package com.arivubuilds.internals.solid.p04_lsp.bad;

import java.math.BigDecimal;
import java.util.List;

public class LegacyFeeBatchProcessor {
    public void deductMaintenanceFee(List<LegacyAccount> accounts, BigDecimal fee) {
        for (LegacyAccount account : accounts) account.withdraw(fee);
    }
}
