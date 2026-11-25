# 01-legacy-monolith-service

## Overview
Represents the legacy monolithic service baseline prior to incremental decomposition. It hosts existing domain logic and endpoints that are gradually intercepted and migrated behind the routing proxy.

## Role in the Architecture
- **Source of Legacy Capability:** Houses baseline monolithic order/domain handlers and data access logic.
- **Migration Target:** Gradually carved out as specific bounded contexts (such as orders) move into dedicated microservices.
- **Fallback / Coexistence:** Continues serving non-migrated traffic while strangler routing shifts targeted routes to modern services.

## Structure
- `src/` — Application source code, configuration, and tests.
- `pom.xml` — Maven module configuration (Java 21 / Spring Boot 3.3.5).

## Build & Run
```powershell
mvn clean test -pl 02-microservices-patterns-series/02-service-discovery-registry-pattern/01-legacy-monolith-service -am