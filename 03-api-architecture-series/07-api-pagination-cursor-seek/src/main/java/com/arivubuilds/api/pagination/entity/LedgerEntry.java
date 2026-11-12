package com.arivubuilds.api.pagination.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_seek", columnList = "occurred_at DESC, id DESC")
        }
)
public class LedgerEntry {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "account_id", nullable = false, length = 64)
    private String accountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected LedgerEntry() {}

    public LedgerEntry(String id, String accountId, BigDecimal amount, Instant occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public Instant getOccurredAt() { return occurredAt; }
}