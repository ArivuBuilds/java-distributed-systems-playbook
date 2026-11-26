# 07: SOLID Design Principles in Enterprise Java

## Architectural Overview

Enterprise Java codebases accumulate coupling when business orchestration, branching rules, subtyping contracts, and infrastructure adapters are mixed together:

- **God-Service & Tightly Coupled Anti-Pattern (`bad`):** Monolithic services, cascading type branches, broken substitutability, fat interfaces, and direct infrastructure calls make code brittle and hard to test.
- **Modular SOLID Refactoring (`good`):** Separates responsibilities (SRP), routes via extensible strategies (OCP), preserves behavioral contracts (LSP), narrows role interfaces (ISP), and isolates infrastructure behind ports/adapters (DIP) through to a unified capstone workflow.

```mermaid
flowchart LR
    Bad["Anti-Pattern Packages (bad)<br/>God Services, Branching & Coupling"]
    Good["Refactored SOLID Modules (good)<br/>SRP, OCP, LSP, ISP & DIP"]
    Capstone["Capstone Settlement Flow<br/>(Unifies All 5 Principles)"]

    Bad -->|"Refactored Into"| Good --> Capstone

    style Bad fill:#ffebee,stroke:#c62828,stroke-width:2px
    style Good fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style Capstone fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
```

---

## Key Trade-offs

| Dimension | Procedural / Coupled Design (`bad`) | SOLID + Hexagonal Ports (`good`) |
| :--- | :--- | :--- |
| **Change Isolation & Extensibility** | Changes ripple across unrelated logic & conditionals | Focused collaborators & strategy extension points localize change |
| **Testability** | Requires heavy wiring or hits real infrastructure | Easy unit & contract testing via ports and small components |
| **Upfront Structure** | Fast initial script/class | More interfaces, small classes & explicit contracts to navigate |

---

## Build & Run

```bash
mvn test -f 01-java-internals-mastery/07-solid-design-principles/pom.xml -Dnet.bytebuddy.experimental=true
```
