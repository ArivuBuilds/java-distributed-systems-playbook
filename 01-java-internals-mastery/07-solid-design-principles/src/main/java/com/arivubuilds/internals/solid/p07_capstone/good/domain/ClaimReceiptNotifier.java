package com.arivubuilds.internals.solid.p07_capstone.good.domain;

public interface ClaimReceiptNotifier {
    String notifyPolicyholder(ClaimRequest request, String payoutRef);
}
