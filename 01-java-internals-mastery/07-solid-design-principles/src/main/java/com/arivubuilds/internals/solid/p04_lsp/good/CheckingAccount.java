package com.arivubuilds.internals.solid.p04_lsp.good;

import java.math.BigDecimal;

public class CheckingAccount implements WithdrawableAccount {
    private final String accountId;
    private BigDecimal balance;
    public CheckingAccount(String accountId, BigDecimal initialBalance) { this.accountId = accountId; this.balance = initialBalance; }
    @Override public String accountId() { return accountId; }
    @Override public BigDecimal balance() { return balance; }
    @Override public void deposit(BigDecimal amount) { this.balance = this.balance.add(amount); }
    @Override public void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds");
        this.balance = this.balance.subtract(amount);
    }
}
