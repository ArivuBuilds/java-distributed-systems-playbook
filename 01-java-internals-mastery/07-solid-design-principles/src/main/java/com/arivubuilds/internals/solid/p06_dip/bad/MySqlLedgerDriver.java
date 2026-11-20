package com.arivubuilds.internals.solid.p06_dip.bad;

import java.math.BigDecimal;

public class MySqlLedgerDriver {
    public String insertRow(String txId, BigDecimal amount) {
        return "MYSQL_ROW_INSERTED:" + txId + "=" + amount;
    }
}
