package com.arivubuilds.api.versioning.controller;

import com.arivubuilds.api.versioning.interceptor.DeprecatedEndpoint;
import com.arivubuilds.api.versioning.model.PolicyContractV1;
import com.arivubuilds.api.versioning.model.PolicyContractV2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class PolicyVersioningController {

    public static final String V1_VENDOR_MEDIA_TYPE = "application/vnd.arivubuilds.policy.v1+json";
    public static final String V2_VENDOR_MEDIA_TYPE = "application/vnd.arivubuilds.policy.v2+json";

    // =========================================================================
    // STRATEGY 1: URI Path Versioning (/api/v1/... vs /api/v2/...)
    // =========================================================================

    @GetMapping("/v1/policies/{policyNumber}")
    @DeprecatedEndpoint(successorUri = "/api/v2/policies/{policyNumber}")
    public ResponseEntity<PolicyContractV1> getPolicyByUriV1(@PathVariable String policyNumber) {
        return ResponseEntity.ok(buildV1(policyNumber));
    }

    @GetMapping("/v2/policies/{policyNumber}")
    public ResponseEntity<PolicyContractV2> getPolicyByUriV2(@PathVariable String policyNumber) {
        return ResponseEntity.ok(buildV2(policyNumber));
    }

    // =========================================================================
    // STRATEGY 2: Custom Header Versioning (X-API-Version: 1 vs 2)
    // =========================================================================

    @GetMapping(value = "/policies/{policyNumber}", headers = "X-API-Version=1")
    @DeprecatedEndpoint(successorUri = "/api/policies/{policyNumber}")
    public ResponseEntity<PolicyContractV1> getPolicyByHeaderV1(@PathVariable String policyNumber) {
        return ResponseEntity.ok(buildV1(policyNumber));
    }

    @GetMapping(value = "/policies/{policyNumber}", headers = "X-API-Version=2")
    public ResponseEntity<PolicyContractV2> getPolicyByHeaderV2(@PathVariable String policyNumber) {
        return ResponseEntity.ok(buildV2(policyNumber));
    }

    // =========================================================================
    // STRATEGY 3: Vendor MIME Media Type Content Negotiation (Accept header)
    // =========================================================================

    @GetMapping(value = "/negotiated/policies/{policyNumber}", produces = V1_VENDOR_MEDIA_TYPE)
    @DeprecatedEndpoint(successorUri = "/api/negotiated/policies/{policyNumber}")
    public ResponseEntity<PolicyContractV1> getPolicyByMediaTypeV1(@PathVariable String policyNumber) {
        return ResponseEntity.ok(buildV1(policyNumber));
    }

    @GetMapping(value = "/negotiated/policies/{policyNumber}", produces = V2_VENDOR_MEDIA_TYPE)
    public ResponseEntity<PolicyContractV2> getPolicyByMediaTypeV2(@PathVariable String policyNumber) {
        return ResponseEntity.ok(buildV2(policyNumber));
    }

    private PolicyContractV1 buildV1(String policyNumber) {
        return new PolicyContractV1(
                policyNumber,
                "Arivumani A",
                new BigDecimal("24500.00"),
                "ACTIVE"
        );
    }

    private PolicyContractV2 buildV2(String policyNumber) {
        return new PolicyContractV2(
                policyNumber,
                new PolicyContractV2.HolderProfile("Arivumani A", "PLATINUM"),
                new PolicyContractV2.MonetaryValue(new BigDecimal("24500.00"), "INR"),
                "IN_FORCE",
                LocalDate.of(2026, 1, 15)
        );
    }
}