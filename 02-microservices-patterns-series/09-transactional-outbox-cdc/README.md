# 09: Event-Driven Architecture & The Transactional Outbox Pattern

## Architectural Overview

In a distributed microservice topology, executing a local database update and publishing an event to a message broker (e.g., Apache Kafka or RabbitMQ) represents the classical **dual-write problem**:

```text
                  +------------------------+
                  |   Payment Service      |
                  +-----+------------+-----+
           1. Commit DB |            | 2. Publish Event (Network Timeout!)
                        v            v
             +--------------+        +------------------+
             |  PostgreSQL  |        |   Apache Kafka   |
             +--------------+        +------------------+
If the database commit succeeds but the network connection to Kafka drops, the message is lost forever (data inconsistency). If the event is published first and the database transaction rolls back, downstream services process phantom records.

Two-Phase Commit (2PC / XA) is unfeasible across modern distributed infrastructure due to blocking locks and poor horizontal scalability.

The Solution: Transactional Outbox with Change Data Capture (CDC)
Instead of reaching out across the network to Kafka during the HTTP transaction:

The service inserts the business record (policies) and the domain event (outbox_events) inside the exact same ACID local transaction.

A Transaction Log Miner (e.g., Debezium CDC via PostgreSQL Write-Ahead Log / pgoutput) streams new rows out of outbox_events asynchronously and emits them into Kafka with at-least-once delivery guarantees.
+--------------------------------------------------------+
 | Policy Issuance Service                                |
 |                                                        |
 |  BEGIN TRANSACTION;                                    |
 |    INSERT INTO policies (...);                         |
 |    INSERT INTO outbox_events (...);                    |
 |  COMMIT;                                               |
 +-------------------------+------------------------------+
                           | Write-Ahead Log (WAL)
                           v
             +--------------------------+
             |  Debezium CDC Connector  |
             +-------------+------------+
                           | Exactly-Once Partitioning
                           v
             +--------------------------+
             |   Apache Kafka Cluster   |
             +--------------------------+
Trade-offs & Production ConsiderationsDesign ConcernTrade-off / ResolutionDual-Write HazardEliminated entirely; RDBMS atomicity enforces consistency.CDC vs. PollerLog tailing (Debezium) avoids polling CPU overhead and polling query locks.Delivery SemanticsGuarantees at-least-once. Downstream consumers must be idempotent (using deduplication keys).Outbox CompactionTabled rows must be vacuumed/truncated asynchronously to prevent unbounded RDBMS table bloat.
Production Implementations Included
V1__create_outbox_schema.sql: PostgreSQL DDL defining business aggregate and outbox event tables with CDC indexing.

PolicyIssuanceService.java: Production transactional service writing domain aggregates and outbox payloads atomically.

TransactionalOutboxTest.java: Concurrency and rollback test verifying zero partial state writes under failure conditions.
