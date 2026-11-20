package com.arivubuilds.internals.solid.p04_lsp.good;

import java.math.BigDecimal;

public interface AccountBalanceHolder {
    String accountId();
    BigDecimal balance();
    void deposit(BigDecimal amount);
}
