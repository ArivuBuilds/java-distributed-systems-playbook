# 02-strangler-routing-proxy

## Overview
Implements the Strangler Fig routing and ingress/proxy layer that sits in front of the legacy monolith and modern microservices, directing traffic based on route/path rules and migration toggles.

## Role in the Architecture
- **Traffic Control Point:** Intercepts incoming client requests and routes migrated endpoints to the new service while forwarding remaining traffic to the legacy monolith.
- **Incremental Cutover:** Enables safe, reversible migration without big-bang cutovers or client-side breaking changes.
- **Service Discovery / Routing Integration:** Connects routing rules with service endpoints/registry lookup.

## Structure
- `src/main/` — Proxy/routing configuration, filters/routes, and service definitions.
- `pom.xml` — Maven module configuration (Java 21 / Spring Boot 3.3.5).

## Build & Run
```powershell
mvn clean test -pl 02-microservices-patterns-series/02-service-discovery-registry-pattern/02-strangler-routing-proxy -am