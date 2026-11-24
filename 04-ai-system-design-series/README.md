# 04 — AI System Design & Production LLM Architecture Series

Production-grade **Java 21 + Spring Boot 3.3.x** reference implementations covering the architectural evolution from embedded, in-process ML inference to resilient, multi-model AI gateways, Retrieval-Augmented Generation (RAG) pipelines, and deterministic agentic orchestration.

---

## 9-Day Architectural Roadmap

| Day | Module / Pattern | Core Architectural Focus | Status |
| :--- | :--- | :--- | :--- |
| **Day 01** | [`01-monolithic-ai-onnx-runtime`](./01-monolithic-ai-onnx-runtime) | In-process ONNX Runtime inference inside Spring Boot, zero-network-hop latency vs. JVM heap/native memory isolation | ✅ Complete |
| **Day 02** | `02-client-server-model-gateway` | Decoupled AI Model Gateway, dynamic provider routing, token budgeting, and unified REST/SSE streaming contracts | 🚧 Planned |
| **Day 03** | `03-semantic-caching-vector-store` | Embedding-based semantic similarity cache (cosine threshold gating) to cut LLM latency and token spend | 🚧 Planned |
| **Day 04** | `04-rag-hybrid-retrieval-pipeline` | Chunking strategies, dense vector + BM25 lexical hybrid search, Reciprocal Rank Fusion (RRF), and citation grounding | 🚧 Planned |
| **Day 05** | `05-llm-resilience-token-rate-limiting` | Token-bucket TPM/RPM rate limiting, fallback model cascades, and timeout guardrails for slow upstream inference | 🚧 Planned |
| **Day 06** | `06-structured-outputs-guardrails` | Deterministic JSON Schema validation, PII redaction filters, and prompt-injection boundary enforcement | 🚧 Planned |
| **Day 07** | `07-async-batch-inference-outbox` | High-throughput asynchronous document ingestion and batch inference backed by transactional outbox queues | 🚧 Planned |
| **Day 08** | `08-mcp-tool-orchestration-pattern` | Model Context Protocol (MCP) unified tool gateway, persistent session state, and bounded multi-step agent loops | 🚧 Planned |
| **Day 09** | `09-llm-observability-eval-harness` | OpenTelemetry token/latency tracing, cost attribution per tenant, and automated regression evaluation gates | 🚧 Planned |

---

## Build & Verification

Run the Maven reactor for all active modules in this series:

```bash
mvn test -f 04-ai-system-design-series/pom.xml
