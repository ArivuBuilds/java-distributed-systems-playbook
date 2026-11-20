package com.arivubuilds.internals.solid.p06_dip.good.domain;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SettlementProcessor {

    private final SettlementLedgerPort ledgerPort;
    private final SettlementEventPort eventPort;

    public SettlementProcessor(SettlementLedgerPort ledgerPort, SettlementEventPort eventPort) {
        this.ledgerPort = ledgerPort;
        this.eventPort = eventPort;
    }

    public String executeSettlement(String txId, BigDecimal amount, String recipient) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be positive");
        }

        return ledgerPort.recordSettlement(txId, amount) + " | " + eventPort.publishSettledEvent(txId, amount, recipient);
    }
}
