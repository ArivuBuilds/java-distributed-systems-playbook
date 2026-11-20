package com.arivubuilds.internals.solid.p04_lsp.good;

import java.math.BigDecimal;

public class EscrowAccount implements AccountBalanceHolder {
    private final String accountId;
    private BigDecimal balance;
    public EscrowAccount(String accountId, BigDecimal initialBalance) { this.accountId = accountId; this.balance = initialBalance; }
    @Override public String accountId() { return accountId; }
    @Override public BigDecimal balance() { return balance; }
    @Override public void deposit(BigDecimal amount) { this.balance = this.balance.add(amount); }
}
