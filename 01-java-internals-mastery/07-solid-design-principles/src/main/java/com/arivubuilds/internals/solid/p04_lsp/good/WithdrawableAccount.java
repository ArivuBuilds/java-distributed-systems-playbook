package com.arivubuilds.internals.solid.p04_lsp.good;

import java.math.BigDecimal;

public interface WithdrawableAccount extends AccountBalanceHolder {
    void withdraw(BigDecimal amount);
}
