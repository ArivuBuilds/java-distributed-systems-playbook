# Day 04: MVC in AI Applications

## Architectural Overview

AI-enabled web applications still benefit from clear Model-View-Controller boundaries so prompt assembly, tool/orchestrator calls, and streaming responses do not leak into controllers:

- **Fat Controller / Mixed Prompt & Stream Logic Anti-Pattern:** Embedding raw prompt strings, tool invocation rules, and stream formatting inside web controllers makes workflows hard to test and maintain.
- **Layered MVC in AI Applications:** Separates request/stream endpoints (Controller), orchestration, prompt templates, memory, and tool schemas (Service/Model), and SSE/structured response delivery.

```mermaid
flowchart LR
    Client["Client (REST / SSE)"]
    Controller["Web / SSE Controller"]
    Orchestrator["AI Orchestrator & Tool Schemas"]
    PromptMem["Prompt Templates & Bounded Memory"]
    Model["LLM / Inference Backend"]

    Client <-->|"Request / SSE Stream"| Controller <--> Orchestrator
    Orchestrator <--> PromptMem
    Orchestrator <--> Model

    style Controller fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style Orchestrator fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
```

---

## Key Trade-offs

| Dimension | Inline / Ad-Hoc AI Endpoints | Layered MVC AI Orchestration |
| :--- | :--- | :--- |
| **Testability & Separation** | Hard to unit-test prompts, tools & stream states | Clear boundaries for prompts, memory, tools & controllers |
| **Streaming Complexity** | Ad-hoc chunk writes can leak partial states | Explicit SSE lifecycle & error/completion handling |
| **Boilerplate** | Fast initial prototype | More structured interfaces & orchestration layers |

---

## Build & Run
```bash
mvn test -f 04-ai-system-design-series/04-mvc-in-ai-applications/pom.xml -Dnet.bytebuddy.experimental=true
```
