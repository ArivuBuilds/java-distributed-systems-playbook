# 08: CQRS Pattern (Command Query Responsibility Segregation)

## Architectural Overview

In complex enterprise architectures, using a single unified data model for both reads and writes leads to severe scalability bottlenecks:

- **Write Requirements:** High normalization (3NF), ACID transactions, row-level locking, complex business validation, and strict invariant checks.
- **Read Requirements:** Low latency, denormalized flattened structures, multi-table aggregations, and high read throughput without lock contention.

**CQRS solves this by explicitly decoupling the write path from the read path:**

```mermaid
flowchart LR
    Client["Client / Application"]
    
    subgraph CommandPath ["Write Path (Command)"]
        CmdHandler["Command Handler"]
        WriteDB[("Normalized Write DB\n(ACID Store)")]
    end

    subgraph QueryPath ["Read Path (Query)"]
        ProjEngine["Projection Engine"]
        ReadDB[("Denormalized Read DB\n(Elastic / Redis / Read Replica)")]
        QueryService["Query Service"]
    end

    Client -->|"1. Issue Command (POST/PUT)"| CmdHandler
    CmdHandler -->|"2. Validate & Commit"| WriteDB
    CmdHandler -.->|"3. Emit Domain Event"| ProjEngine
    ProjEngine -->|"4. Update Projection"| ReadDB
    Client -->|"5. Query View (GET)"| QueryService
    QueryService -->|"6. Fast Read"| ReadDB

    style WriteDB fill:#ffebee,stroke:#c62828,stroke-width:2px
    style ReadDB fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
Key Trade-offsDimensionCommand Model (Write)Query Model (Read)Primary GoalInvariant enforcement & state integrityMicrosecond query latency & high concurrencyData StructureHighly normalized (PostgreSQL / Oracle)Denormalized / document / key-value viewsConsistencyStrong Consistency (ACID)Eventual Consistency (BASE)Scaling VectorScaled vertically or sharded by partition keyScaled horizontally via read replicas / cachesProduction Verification Tests IncludedPolicyCQRSIntegrationTest.java: End-to-end integration test validating command validation, event emission, read projection generation, and eventual consistency boundaries.
