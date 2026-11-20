package com.arivubuilds.internals.solid.p07_capstone.good.adapter;

import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimReceiptNotifier;
import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimRequest;
import org.springframework.stereotype.Component;

@Component
public class SmsClaimReceiptAdapter implements ClaimReceiptNotifier {
    @Override public String notifyPolicyholder(ClaimRequest request, String payoutRef) { return "SMS_TO:" + request.customerPhone(); }
}
