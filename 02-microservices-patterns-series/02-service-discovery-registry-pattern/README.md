# 02: Service Discovery & Registry Pattern (Dynamic Routing)

## Architectural Overview

In cloud-native and containerized environments, microservice instances scale elastically and receive ephemeral IP addresses and ports. Hardcoding hostnames or static load-balancer IP lists breaks immediately during autoscaling or rolling deployments:

- **Dynamic Registration:** Service instances (`01-legacy-monolith-service`, `03-modern-order-service`) register their network locations and heartbeat health endpoints with a central registry upon startup.
- **Dynamic Lookup & Load-Balanced Routing:** Routing proxies (`02-strangler-routing-proxy`) resolve logical service IDs (`lb://order-service`) dynamically, automatically evicting unhealthy instances without manual config reloads.

```mermaid
flowchart TB
    Registry[("Service Registry<br/>(Eureka / Consul / K8s DNS)")]

    subgraph Providers ["Registered Service Instances"]
        LegacySvc["01 - Legacy Monolith Service<br/>(Heartbeat + Metadata)"]
        ModernSvc["03 - Modern Order Microservice<br/>(Heartbeat + Metadata)"]
    end

    Proxy["02 - Strangler Routing Proxy<br/>(Client-Side Load Balancer)"]
    Client["API Consumer"]

    LegacySvc -->|"1. Register & Heartbeat"| Registry
    ModernSvc -->|"1. Register & Heartbeat"| Registry
    Client -->|"2. Request /orders"| Proxy
    Proxy <-->|"3. Fetch Healthy Instance Registry"| Registry
    Proxy -->|"4. Load-Balanced Route"| ModernSvc

    style Registry fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    style Providers fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
```

---

## Key Trade-offs

| Dimension | Client-Side Discovery (Spring Cloud LoadBalancer) | Server-Side Discovery (AWS ALB / K8s Service) |
| :--- | :--- | :--- |
| **Network Hops** | Direct call from caller to target instance (1 hop) | Extra hop through dedicated load balancer |
| **Routing Intelligence** | Custom sticky, zone-aware, or weighted rules in JVM | Constrained to infrastructure LB capabilities |
| **Polyglot Support** | Requires discovery SDK per language | Language-agnostic (standard DNS / VIP) |
| **Registry Outage Behavior** | Continues routing using local cached registry | Depends on control-plane health checks |

---

## Production Verification Tests Included
- Multi-module reactor verification across `01-legacy-monolith-service`, `02-strangler-routing-proxy`, and `03-modern-order-service` validating service registration, health eviction, and dynamic route resolution.

## Build & Run
```bash
mvn clean test -f 02-microservices-patterns-series/02-service-discovery-registry-pattern/pom.xml -Dnet.bytebuddy.experimental=true
```
