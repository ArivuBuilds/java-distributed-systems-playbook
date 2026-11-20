package com.arivubuilds.internals.solid.p06_dip.good.adapter;

import com.arivubuilds.internals.solid.p06_dip.good.domain.SettlementLedgerPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JpaLedgerAdapter implements SettlementLedgerPort {
    @Override
    public String recordSettlement(String txId, BigDecimal amount) {
        return "JPA_LEDGER_SAVED:" + txId + "=" + amount;
    }
}
