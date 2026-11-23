package com.arivubuilds.ai.monolith.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class FencedOnnxInferenceService {

    @FunctionalInterface
    public interface InferenceEngine {
        float[] infer(float[] features) throws Exception;
    }

    private final InferenceEngine engine;
    private final Semaphore inferenceBulkhead;
    private final long acquireTimeoutMs;

    @Autowired
    public FencedOnnxInferenceService(
            OrtEnvironment env,
            @Autowired(required = false) OrtSession session,
            @Value("${ai.onnx.max-concurrent-inferences:4}") int maxConcurrentInferences,
            @Value("${ai.onnx.acquire-timeout-ms:50}") long acquireTimeoutMs) {
        this(
                features -> executeNativeOnnx(env, session, features),
                maxConcurrentInferences,
                acquireTimeoutMs
        );
    }

    public FencedOnnxInferenceService(
            InferenceEngine engine,
            int maxConcurrentInferences,
            long acquireTimeoutMs) {
        this.engine = engine;
        // Fair semaphore bulkhead caps simultaneous native JNI tensor executions
        this.inferenceBulkhead = new Semaphore(maxConcurrentInferences, true);
        this.acquireTimeoutMs = acquireTimeoutMs;
    }

    public float[] predict(float[] features) {
        boolean acquired = false;
        try {
            acquired = inferenceBulkhead.tryAcquire(acquireTimeoutMs, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new InferenceOverloadException(
                        "Inference bulkhead saturated (" + acquireTimeoutMs + "ms timeout). Shedding load.");
            }
            return engine.infer(features);
        } catch (InferenceOverloadException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for inference permit", e);
        } catch (Exception e) {
            throw new IllegalStateException("ONNX Runtime inference execution failed", e);
        } finally {
            if (acquired) {
                inferenceBulkhead.release();
            }
        }
    }

    private static float[] executeNativeOnnx(
            OrtEnvironment env,
            OrtSession session,
            float[] features) throws OrtException {
        if (session == null) {
            throw new IllegalStateException("OrtSession is not initialized. Provide a valid ai.onnx.model-path.");
        }
        long[] shape = new long[]{1, features.length};
        // Always close OnnxTensor deterministically to release off-heap native buffers
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(features), shape);
             OrtSession.Result results = session.run(Map.of("input", inputTensor))) {
            float[][] output = (float[][]) results.get(0).getValue();
            return output[0];
        }
    }

    public int availablePermits() {
        return inferenceBulkhead.availablePermits();
    }

    public static class InferenceOverloadException extends RuntimeException {
        public InferenceOverloadException(String message) {
            super(message);
        }
    }
}