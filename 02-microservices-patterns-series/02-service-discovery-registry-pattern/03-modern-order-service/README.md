# 03-modern-order-service

## Overview
Represents the extracted, independently deployable modern **Order Service** carved out from the legacy monolith as part of the incremental microservices migration.

## Role in the Architecture
- **Bounded Context Ownership:** Encapsulates order lifecycle operations with clean service boundaries.
- **Independent Evolution:** Allows independent scaling, release cadence, and maintainability separate from the legacy monolith.
- **Proxy Target:** Receives strangled order traffic forwarded by `02-strangler-routing-proxy`.

## Structure
- `src/main/` — Order service controllers, domain/application services, models, and configuration.
- `pom.xml` — Maven module configuration (Java 21 / Spring Boot 3.3.5).

## Build & Run
```powershell
mvn clean test -pl 02-microservices-patterns-series/02-service-discovery-registry-pattern/03-modern-order-service -am