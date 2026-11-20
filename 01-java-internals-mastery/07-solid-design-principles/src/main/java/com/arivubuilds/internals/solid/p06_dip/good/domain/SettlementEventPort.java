package com.arivubuilds.internals.solid.p06_dip.good.domain;

import java.math.BigDecimal;

public interface SettlementEventPort {
    String publishSettledEvent(String txId, BigDecimal amount, String recipient);
}
