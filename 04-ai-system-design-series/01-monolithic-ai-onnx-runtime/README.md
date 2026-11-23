# Day 01 — Monolithic AI Architecture (In-Process Inference with ONNX Runtime)

## 1. Architectural Problem
Embedding ML models directly inside a transactional Spring Boot JVM process eliminates network hops on Day 1, but introduces severe production hazards under concurrent traffic:
1. **Off-Heap Native Memory Contention:** ONNX Runtime allocates weights and execution tensors via native C++ JNI memory outside `-Xmx`, colliding with JVM heap and OS page cache and causing Linux `OOMKill` events.
2. **CPU Thread Starvation:** Compute-heavy matrix multiplications saturate CPU cores and starve Tomcat/Netty worker threads and HikariCP housekeeping.
3. **Asymmetric Scaling:** Bundling 600MB–2GB model weights inside service containers slows autoscaling and couples model updates to full application redeployments.

## 2. Production Guardrails Implemented
* **Semaphore Bulkhead (`FencedOnnxInferenceService`):** Caps concurrent native `OrtSession.run()` calls and sheds excess load immediately (`HTTP 429` + `Retry-After`) before HTTP worker threads pile up.
* **Native Thread Pool Capping (`OnnxRuntimeConfig`):** Sets `setIntraOpNumThreads(2)` and `setInterOpNumThreads(1)` so ONNX's C++ thread pool does not thrash the JVM scheduler.
* **Deterministic Tensor Lifecycle:** Uses `try-with-resources` on `OnnxTensor` and `OrtSession.Result` to release native off-heap buffers immediately after inference.

## 3. Running Locally
```bash
mvn clean test