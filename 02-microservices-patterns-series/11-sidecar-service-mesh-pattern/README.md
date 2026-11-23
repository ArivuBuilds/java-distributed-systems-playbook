# Day 11: Sidecar and Service Mesh Pattern

## Architectural Overview

As distributed microservice topologies scale to dozens or hundreds of services across polyglot stacks (Java, Go, Python, Node.js), embedding operational concerns like mutual TLS (mTLS), distributed tracing, retries, circuit breaking, and rate limiting into individual application codebases creates friction. Upgrading a cipher suite or rotating TLS certificates requires rebuilding and redeploying every microservice.

The **Sidecar Pattern** decouples network operations from application logic by co-locating an auxiliary L7 proxy (e.g., Envoy) alongside the application container inside the same deployment unit (Kubernetes Pod). A **Service Mesh** (Istio, Linkerd) establishes a centralized control plane to distribute routing policies and rotate X.509 certificates to these distributed sidecar data planes.

```text
[ Client Pod / Caller ]
          |
     (Secure mTLS over Wire on Port 10000)
          v
+---------------------------------------------------------+
| Kubernetes Pod (Shared Network Namespace / localhost)     |
|                                                      |
|   +----------------+          +------------------+   |
|   |   Envoy Sidecar   | -------> | Application Engine |   |
|   |  (Data Plane L7)  |  (HTTP)  |  (Order Service)   |   |
|   |    Port: 10000    |  :8080   |    Port: 8080      |   |
|   +---------------+          +------------------+   |
|             |                                      |
+-------------|--------------------------------------------+
              v
     Prometheus Metrics (:9901)
```

---

## Core Operational Mechanics

1. **Localhost Network Interception**: In Kubernetes, all containers within a Pod share an IOC and network namespace. The application binds strictly to `127.0.0.1:8080`.
2. **Transparent Wire Encryption (mTLS)**: The Envoy sidecar listens on port `10000`, requiring incoming connections to present valid X.509 client certificates signed by the mesh CA (`require_client_certificate: true`).
3. **Automated L7 Retries**: If the local service returns a 5xx error or connection drop, Envoy executes retries with exponential backoff before returning an error upstream.
4. **Decoupled Telemetry**: The sidecar outputs access logs and Prometheus metrics on port `9901` without requiring language-specific agents.

---

## Architecture Trade-offs

| Dimension | Library-Heavy Model (Spring Cloud / SDKs) | Sidecar Service Mesh (Envoy / Istio) |
| :--- | :--- | :--- |
| **Language Agnostic** | No (requires per-language client libraries) | Yes (consistent proxy for Java, Go, Python) |
| **Certificate Rotation** | High complexity (app restart / config reloads) | Zero application downtime (control plane hot reload) |
| **Network Latency** | Baseline (direct socket connection) | Adds ~1-2ms per proxy hop |
| **Resource Overhead** | Low memory footprint | Requires dedicated CPU/Memory per Pod |

---

## Local Verification

### 1. Generate Certificates
```bash
cd certs
./generate-certs.sh
cd ..
```

### 2. Start the Sidecar & Backend
```bash
docker compose up -d
```

### 3. Verify Mutual TLS Enforcement

Attempting to connect without client certificates returns an SSL handshake failure:
```bash
curl -k https://localhost:10000/
# Output: curl: (35) error:0A000412:SSL routines::ssl/tls alert bad certificate
```

Connecting with the valid client key and CA certificate succeeds:
```bash
curl --cacert certs/ca-cert.pem \
     --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     https://localhost:10000/
# Output: Handled by App via Envoy Sidecar mTLS
```

### 4. Inspect Proxy Metric