package com.arivubuilds.ai.monolith;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.arivubuilds.ai.monolith.service.FencedOnnxInferenceService;
import com.arivubuilds.ai.monolith.service.FencedOnnxInferenceService.InferenceOverloadException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FencedOnnxInferenceServiceTest {

    @Test
    void executesInferenceAndReleasesSemaphorePermits() {
        FencedOnnxInferenceService service = new FencedOnnxInferenceService(
                features -> new float[]{features[0] * 2.0f, features[1] * 2.0f},
                2,
                100
        );

        float[] result = service.predict(new float[]{0.4f, 0.25f});

        assertArrayEquals(new float[]{0.8f, 0.5f}, result, 1e-5f);
        assertEquals(2, service.availablePermits(), "All semaphore permits must be released after inference");
    }

    @Test
    void shedsLoadWhenSemaphoreBulkheadIsSaturated() throws Exception {
        CountDownLatch inferenceStarted = new CountDownLatch(1);
        CountDownLatch releaseInference = new CountDownLatch(1);

        // Allow only 1 concurrent inference with a 30ms acquire timeout
        FencedOnnxInferenceService service = new FencedOnnxInferenceService(
                features -> {
                    inferenceStarted.countDown();
                    releaseInference.await();
                    return features;
                },
                1,
                30
        );

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<float[]> activeCall = pool.submit(() -> service.predict(new float[]{1.0f, 2.0f}));
            inferenceStarted.await();

            // Second concurrent caller must fail fast instead of saturating JVM threads
            assertThrows(InferenceOverloadException.class,
                    () -> service.predict(new float[]{3.0f, 4.0f}));

            releaseInference.countDown();
            assertArrayEquals(new float[]{1.0f, 2.0f}, activeCall.get(), 1e-5f);
            assertEquals(1, service.availablePermits());
        }
    }

    @Test
    void executesNativeOnnxRuntimeWhenOsNativeLibrariesAreAvailable() throws Exception {
        OrtEnvironment env;
        try {
            env = OrtEnvironment.getEnvironment();
        } catch (Throwable nativeMissing) {
            // Skips gracefully if Windows VC++ Redistributable (vcruntime140.dll) is not installed
            Assumptions.assumeTrue(false, "Skipping native JNI test: " + nativeMissing.getMessage());
            return;
        }

        byte[] identityModelBytes = buildMinimalIdentityOnnxModel("input", "output", 3);
        try (OrtSession.SessionOptions opts = new OrtSession.SessionOptions()) {
            opts.setIntraOpNumThreads(2);
            try (OrtSession session = env.createSession(identityModelBytes, opts)) {
                FencedOnnxInferenceService service = new FencedOnnxInferenceService(env, session, 2, 100);
                float[] input = new float[]{0.85f, 0.12f, 0.43f};
                assertArrayEquals(input, service.predict(input), 1e-5f);
                assertEquals(2, service.availablePermits());
            }
        }
    }

    private static byte[] buildMinimalIdentityOnnxModel(String inputName, String outputName, int dim) {
        byte[] nodeProto = concat(
                tagString(1, inputName),
                tagString(2, outputName),
                tagString(4, "Identity")
        );

        byte[] tensorShapeDim1 = tagVarint(1, 1);
        byte[] tensorShapeDimN = tagVarint(1, dim);
        byte[] tensorShape = concat(tagBytes(1, tensorShapeDim1), tagBytes(1, tensorShapeDimN));
        byte[] tensorType = concat(tagVarint(1, 1), tagBytes(2, tensorShape));
        byte[] typeProto = tagBytes(1, tensorType);

        byte[] inputInfo = concat(tagString(1, inputName), tagBytes(2, typeProto));
        byte[] outputInfo = concat(tagString(1, outputName), tagBytes(2, typeProto));

        byte[] graphProto = concat(
                tagBytes(1, nodeProto),
                tagString(2, "identity-graph"),
                tagBytes(11, inputInfo),
                tagBytes(12, outputInfo)
        );

        return concat(
                tagVarint(1, 7),
                tagBytes(8, tagVarint(2, 13)),
                tagBytes(7, graphProto)
        );
    }

    private static byte[] tagVarint(int fieldNumber, long value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeVarint(out, ((long) fieldNumber << 3));
        writeVarint(out, value);
        return out.toByteArray();
    }

    private static byte[] tagString(int fieldNumber, String value) {
        return tagBytes(fieldNumber, value.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] tagBytes(int fieldNumber, byte[] payload) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeVarint(out, ((long) fieldNumber << 3) | 2);
        writeVarint(out, payload.length);
        out.writeBytes(payload);
        return out.toByteArray();
    }

    private static void writeVarint(ByteArrayOutputStream out, long value) {
        while ((value & ~0x7FL) != 0) {
            out.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        out.write((int) value);
    }

    private static byte[] concat(byte[]... chunks) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] chunk : chunks) {
            out.writeBytes(chunk);
        }
        return out.toByteArray();
    }
}