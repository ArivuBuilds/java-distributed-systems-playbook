# Day 12: API Gateway & Edge Ingress

## Architectural Overview

Edge API gateways sit at the boundary between external clients and internal microservices, centralizing cross-cutting concerns while keeping downstream services focused on domain logic:

- **Direct-to-Service / Duplicated Edge Logic Anti-Pattern:** Exposing internal services directly or repeating auth, routing, tracing, and circuit-breaking logic in every microservice increases surface area and operational drift.
- **API Gateway & Edge Ingress:** Centralizes reverse-proxy routing, W3C `TraceContext` propagation across service hops, and circuit breakers at the edge to protect upstream services.

```mermaid
flowchart LR
    Client["External Client"]
    Gateway["API Gateway / Edge Ingress<br/>(Routing, TraceContext & Circuit Breaker)"]
    SvcA["Downstream Service A"]
    SvcB["Downstream Service B"]

    Client -->|"Ingress Request"| Gateway
    Gateway -->|"Propagated TraceContext"| SvcA
    Gateway -->|"Propagated TraceContext + Breaker"| SvcB

    style Gateway fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style SvcB fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
```

---

## Key Trade-offs

| Dimension | Direct Service Access / Per-Service Edge | Centralized API Gateway Edge Ingress |
| :--- | :--- | :--- |
| **Cross-Cutting Consistency** | Easy to drift across teams/services | Uniform routing, tracing & resilience at the edge |
| **Blast Radius & Bottlenecks** | Distributed entry points | Gateway becomes a critical edge component to size & HA-configure |
| **Payload / Protocol Translation** | Handled inside each service | Offloaded cleanly at the ingress boundary |

---

## Build & Run
```bash
mvn test -f 03-api-architecture-series/12-api-gateway-edge-ingress/pom.xml -Dnet.bytebuddy.experimental=true
```
