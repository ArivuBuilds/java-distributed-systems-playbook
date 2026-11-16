package com.arivubuilds.api.idempotency.service;

import com.arivubuilds.api.idempotency.model.TransferRequest;
import com.arivubuilds.api.idempotency.model.TransferResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LedgerTransferService {

    private final AtomicInteger executionCounter = new AtomicInteger(0);

    public TransferResponse executeTransfer(TransferRequest request) {
        executionCounter.incrementAndGet();
        String txnId = "txn_" + UUID.randomUUID().toString().substring(0, 8);
        return new TransferResponse(
                txnId,
                "SETTLED",
                request.amount(),
                Instant.now()
        );
    }

    public int getExecutionCount() {
        return executionCounter.get();
    }

    public void resetCounter() {
        executionCounter.set(0);
    }
}