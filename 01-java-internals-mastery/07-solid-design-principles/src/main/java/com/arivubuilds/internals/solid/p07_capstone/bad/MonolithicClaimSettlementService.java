package com.arivubuilds.internals.solid.p07_capstone.bad;

import java.math.BigDecimal;

public class MonolithicClaimSettlementService {
    public String settleClaim(String claimId, BigDecimal amount, String claimType, String customerPhone) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Invalid claim amount");
        String railRes;
        if ("HEALTH".equals(claimType)) {railRes = "TPD_HEALTH_PAYOUT:" + amount;}
        else if ("MOTOR".equals(claimType)) {railRes = "GARAGE_DIRECT_PAYOUT:" + amount;}
        else {throw new UnsupportedOperationException("Unsupported claimType: " + claimType);}
        return "JDBC_SAVED:" + claimId + " | " + railRes + " | SMS_TO:" + customerPhone;
    }
}
