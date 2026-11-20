package com.arivubuilds.internals.solid.p04_lsp.bad;

import java.math.BigDecimal;

public class LockedEscrowAccount extends LegacyAccount {
    public LockedEscrowAccount(BigDecimal initialBalance) { super(initialBalance); }
    @Override
    public void withdraw(BigDecimal amount) { throw new UnsupportedOperationException("Escrow accounts do not support direct withdrawals"); }
}
