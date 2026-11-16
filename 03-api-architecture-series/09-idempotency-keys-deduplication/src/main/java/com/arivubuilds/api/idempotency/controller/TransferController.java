package com.arivubuilds.api.idempotency.controller;

import com.arivubuilds.api.idempotency.model.TransferRequest;
import com.arivubuilds.api.idempotency.model.TransferResponse;
import com.arivubuilds.api.idempotency.service.LedgerTransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final LedgerTransferService transferService;

    public TransferController(LedgerTransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@RequestBody TransferRequest request) {
        TransferResponse response = transferService.executeTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}