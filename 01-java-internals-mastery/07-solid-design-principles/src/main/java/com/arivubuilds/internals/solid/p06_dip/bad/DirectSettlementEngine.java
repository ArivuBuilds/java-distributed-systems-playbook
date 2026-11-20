package com.arivubuilds.internals.solid.p06_dip.bad;

import java.math.BigDecimal;

public class DirectSettlementEngine {
    private final MySqlLedgerDriver dbDriver = new MySqlLedgerDriver();
    private final SmtpAlertClient smtpClient = new SmtpAlertClient();

    public String settle(String txId, BigDecimal amount, String opsEmail) {
        String dbRes = dbDriver.insertRow(txId, amount);
        String mailRes = smtpClient.sendEmail(opsEmail, "Settled " + txId);
        return dbRes + " | " + mailRes;
    }
}
