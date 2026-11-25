# 04 — AI System Design & Production LLM Architecture Series

Production-grade Java 21 + Spring Boot 3.3.x reference implementations covering the architectural evolution from embedded, in-process ML inference to resilient model gateways, layered RAG pipelines, MVC orchestration, GPU pools, event-driven pipelines, serverless inference, tool calling, and deterministic self-reflection agent loops.

## 9-Day Architectural Roadmap

| Day | Module / Pattern | Core Architectural Focus | Status |
| :--- | :--- | :--- | :--- |
| **Day 01** | `01-monolithic-ai-onnx-runtime` | In-process ONNX Runtime & semaphore bulkhead isolation inside Spring Boot | ✅ Published |
| **Day 02** | `02-client-server-model-gateway` | Client-server decoupling and model gateways (vLLM, SGLang, TensorRT-LLM) | ✅ Published |
| **Day 03** | `03-layered-rag-architecture` | Layered RAG architecture: semantic chunking, hybrid RRF, and cross-encoder ranking | ✅ Published |
| **Day 04** | `04-mvc-in-ai-applications` | MVC in AI applications: tool schemas, SSE streams, and orchestrators | | ✅ Built & Tested |
| **Day 05** | `05-microservices-gpu-pools` | Microservices and GPU pools: heterogeneous node pools and KV-cache rate limits | 📅 Scheduled (Daily Series) |
| **Day 06** | `06-event-driven-ai-pipelines` | Event-driven AI pipelines: async Kafka/CDC embeddings and backpressure | 📅 Scheduled (Daily Series) |
| **Day 07** | `07-serverless-ai-cold-starts` | Serverless AI & cold starts: warm GPU pools and weight streaming via Firecracker | 📅 Scheduled (Daily Series) |
| **Day 08** | `08-tool-use-function-calling` | Tool use and function calling: structured JSON schemas and timeout budgets | 📅 Scheduled (Daily Series) |
| **Day 09** | `09-self-reflection-agent-loops` | Self-reflection and agent loops: infinite-loop guardrails, FSM, and deterministic halting | 📅 Scheduled (Daily Series) |

---

## Build & Run All Modules

```bash
mvn clean test -f 04-ai-system-design-series/pom.xml -Dnet.bytebuddy.experimental=true
```
