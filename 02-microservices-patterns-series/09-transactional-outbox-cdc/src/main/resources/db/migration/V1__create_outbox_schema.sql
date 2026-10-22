-- Core Business Aggregate: Life Insurance Policy Table
CREATE TABLE policies (
    policy_id           VARCHAR(64) PRIMARY KEY,
    holder_name         VARCHAR(255) NOT NULL,
    coverage_amount     NUMERIC(15, 2) NOT NULL,
    premium_amount      NUMERIC(15, 2) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Transactional Outbox Table: Written within the same ACID transaction
CREATE TABLE outbox_events (
    event_id            UUID PRIMARY KEY,
    aggregate_type      VARCHAR(64) NOT NULL,      -- e.g., 'POLICY'
    aggregate_id        VARCHAR(64) NOT NULL,      -- e.g., 'POL-90214'
    event_type          VARCHAR(128) NOT NULL,     -- e.g., 'PolicyBoundEvent'
    payload             JSONB NOT NULL,            -- Canonical event payload
    trace_id            VARCHAR(128),              -- Distributed tracing propagation
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for CDC log mining & aggregate lookups
CREATE INDEX idx_outbox_created_at ON outbox_events (created_at ASC);
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_type, aggregate_id);
