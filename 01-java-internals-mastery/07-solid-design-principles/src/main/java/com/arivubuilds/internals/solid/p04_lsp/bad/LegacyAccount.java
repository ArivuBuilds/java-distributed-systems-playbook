package com.arivubuilds.internals.solid.p04_lsp.bad;

import java.math.BigDecimal;

public class LegacyAccount {
    protected BigDecimal balance;
    public LegacyAccount(BigDecimal initialBalance) { this.balance = initialBalance; }
    public void deposit(BigDecimal amount) { this.balance = this.balance.add(amount); }
    public void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient balance");
        this.balance = this.balance.subtract(amount);
    }
    public BigDecimal getBalance() { return balance; }
}
