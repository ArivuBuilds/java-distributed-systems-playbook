# 09: Event-Driven Architecture & The Transactional Outbox Pattern

## 🧠 Architectural Overview

In a distributed microservice topology, executing a local database update and publishing an event to a message broker (e.g., Apache Kafka or RabbitMQ) represents the classical **dual-write problem**:
            ┌────────────────────────┐
              │   Payment Service      │
              └─────┬────────────┬─────┘
       1. Commit DB │            │ 2. Publish Event (Network Timeout!)
                    ▼            ▼
         ┌────────────┐        ┌──────────────────┐
         │ PostgreSQL │        │  Apache Kafka    │
         └────────────┘        └──────────────────┘     
┌────────────────────────────────────────────────────────┐
  │ Policy Issuance Service                                │
  │                                                        │
  │  BEGIN TRANSACTION;                                    │
  │    INSERT INTO policies (...);                         │
  │    INSERT INTO outbox_events (...);                    │
  │  COMMIT;                                               │
  └─────────────────────────┬──────────────────────────────┘
                            │ Write-Ahead Log (WAL)
                            ▼
              ┌──────────────────────────┐
              │ Debezium CDC Connector   │
              └─────────────┬────────────┘
                            │ Exactly-Once Partitioning
                            ▼
              ┌──────────────────────────┐
              │ Apache Kafka Cluster     │
              └──────────────────────────┘

---

## 🛑 Trade-offs & Production Considerations

| Design Concern | Trade-off / Resolution |
| :--- | :--- |
| **Dual-Write Hazard** | Eliminated entirely; RDBMS atomicity enforces consistency. |
| **CDC vs. Poller** | Log tailing (Debezium) avoids polling CPU overhead and polling query locks. |
| **Delivery Semantics** | Guarantees **at-least-once**. Downstream consumers must be idempotent (using deduplication keys). |
| **Outbox Compaction** | Tabled rows must be vacuumed/truncated asynchronously to prevent unbounded RDBMS table bloat. |

---

## 🧪 Production Implementations Included
- [`V1__create_outbox_schema.sql`](src/main/resources/db/migration/V1__create_outbox_schema.sql): PostgreSQL DDL defining business aggregate and outbox event tables with CDC indexing.
- [`PolicyIssuanceService.java`](src/main/java/com/arivubuilds/microservices/outbox/service/PolicyIssuanceService.java): Production transactional service writing domain aggregates and outbox payloads atomically.
- [`TransactionalOutboxTest.java`](src/test/java/com/arivubuilds/microservices/outbox/TransactionalOutboxTest.java): Concurrency and rollback test verifying zero partial state writes under failure conditions.
