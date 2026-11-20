package com.arivubuilds.internals.solid.p06_dip.good.domain;

import java.math.BigDecimal;

public interface SettlementLedgerPort {
    String recordSettlement(String txId, BigDecimal amount);
}
