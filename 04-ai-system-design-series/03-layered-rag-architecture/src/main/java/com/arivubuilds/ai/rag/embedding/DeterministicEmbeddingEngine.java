package com.arivubuilds.ai.rag.embedding;

import com.arivubuilds.ai.rag.chunking.SemanticChunker.EmbeddingEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

@Component
public class DeterministicEmbeddingEngine implements EmbeddingEncoder {

    public static final int DIMENSIONS = 64;

    @Override
    public double[] embed(String text) {
        double[] vector = new double[DIMENSIONS];
        if (text == null || text.isBlank()) {
            return vector;
        }
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z0-9_]+");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            byte[] digest = sha256(token);
            for (int i = 0; i < 4; i++) {
                int idx = Math.abs(digest[i]) % DIMENSIONS;
                double sign = (digest[i + 4] & 1) == 0 ? 1.0 : -1.0;
                vector[idx] += sign;
            }
        }
        return normalize(vector);
    }

    public static double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static double[] normalize(double[] vector) {
        double sumSq = 0.0;
        for (double v : vector) {
            sumSq += v * v;
        }
        if (sumSq == 0.0) {
            return vector;
        }
        double invNorm = 1.0 / Math.sqrt(sumSq);
        double[] out = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            out[i] = vector[i] * invNorm;
        }
        return out;
    }

    private static byte[] sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
