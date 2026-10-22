# 🚀 Java & Distributed Systems Architecture Playbook

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Distributed-black.svg)](https://kafka.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A production-focused reference repository documenting core Java internals, 12 microservices resilience patterns, and 50 distributed system design trade-offs—curated from 18+ years of enterprise engineering in Life Insurance & FinTech platforms.

---

## 📌 Repository Roadmap

### 🧵 Module 1: Java Internals & High-Concurrency Mechanics
- [x] **01-memory-model:** Java Memory Model (`volatile`, StoreLoad barriers, Happens-Before)
- [x] **02-locks-and-aqs:** `ReentrantLock` vs. `synchronized` & Multi-Condition Queues
- [ ] **03-aqs-internals:** AbstractQueuedSynchronizer (AQS) state mechanics & CLH queue
- [ ] **04-virtual-threads:** Virtual Threads (Project Loom) vs. Platform Thread pools: throughput & pinning
- [ ] **05-jvm-tuning-gc:** Low-latency garbage collectors (G1GC vs. Generational ZGC)

---

### 🔄 Module 2: The 12-Day Microservices Architecture Series
Each pattern includes architectural trade-offs, sequence diagrams, and runnable code:
- [ ] **Day 01:** API Gateway Pattern (Edge routing, auth offload & ingress rate limiting)
- [ ] **Day 02:** Service Discovery & Registry (Consul/Eureka vs. Kubernetes CoreDNS)
- [ ] **Day 03:** Backend for Frontend (BFF) Pattern (Client-tailored contracts)
- [ ] **Day 04:** Circuit Breaker & Fallback (Resilience4j state machines)
- [ ] **Day 05:** Bulkhead Isolation Pattern (Thread pool vs. Semaphore isolation)
- [ ] **Day 06:** Retry with Exponential Backoff & Jitter (Eliminating thundering herds)
- [ ] **Day 07:** Database-per-Service Pattern (Polyglot persistence & decoupling)
- [x] **Day 08:** CQRS Pattern (Segregating write models from projection stores)
- [x] **Day 09:** Event-Driven Architecture & Transactional Outbox Pattern (Debezium CDC)
- [ ] **Day 10:** Saga Pattern (Orchestration vs. Choreography without 2PC)
- [ ] **Day 11:** Sidecar & Service Mesh Pattern (Envoy infrastructure & mTLS)
- [ ] **Day 12:** Strangler Fig Pattern (Zero-downtime monolith decomposition)

---

### 📐 Module 3: Distributed System Design (50 Core Concepts)
Structured trade-offs across:
- **Storage & Caching:** Redis topologies, Cache-Aside vs. Write-Through, cache stampede prevention
- **Consistency Models:** Linearizable vs. Causal vs. Eventual Consistency
- **Distributed Locking:** Redlock vs. single-instance Redis CAS vs. ZooKeeper ephemeral nodes
- **Data Partitioning:** Virtual nodes in Consistent Hashing & partition rebalancing

---

### ⚙️ How to Build & Run
```bash
# Clone the repository
git clone [https://github.com/ArivuBuilds/java-distributed-systems-playbook.git](https://github.com/ArivuBuilds/java-distributed-systems-playbook.git)
cd java-distributed-systems-playbook

# Run all test suites
./mvnw clean test
