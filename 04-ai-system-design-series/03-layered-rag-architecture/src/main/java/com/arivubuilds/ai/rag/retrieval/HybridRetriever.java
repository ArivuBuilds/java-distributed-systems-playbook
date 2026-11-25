package com.arivubuilds.ai.rag.retrieval;

import com.arivubuilds.ai.rag.embedding.DeterministicEmbeddingEngine;
import com.arivubuilds.ai.rag.model.RagModels.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class HybridRetriever {

    private static final double K1 = 1.5;
    private static final double B = 0.75;

    public record RankedHit(DocumentChunk chunk, double score, int rank) {
    }

    public List<RankedHit> denseSearch(List<DocumentChunk> chunks, double[] queryEmbedding, int limit) {
        return chunks.stream()
                .map(c -> new RankedHit(c, DeterministicEmbeddingEngine.cosineSimilarity(queryEmbedding, c.embedding()), 0))
                .sorted(Comparator.comparingDouble(RankedHit::score).reversed())
                .limit(limit)
                .map(h -> assignRank(h, chunks.indexOf(h.chunk())))
                .toList();
    }

    public List<RankedHit> bm25Search(List<DocumentChunk> chunks, String query, int limit) {
        if (chunks.isEmpty()) {
            return List.of();
        }
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty()) {
            return List.of();
        }

        List<List<String>> docTokensList = chunks.stream()
                .map(c -> tokenize(c.text()))
                .toList();

        double avgDocLen = docTokensList.stream()
                .mapToInt(List::size)
                .average()
                .orElse(1.0);

        int totalDocs = chunks.size();
        Map<String, Integer> docFreq = new HashMap<>();
        for (String term : Set.copyOf(queryTerms)) {
            int df = 0;
            for (List<String> tokens : docTokensList) {
                if (tokens.contains(term)) {
                    df++;
                }
            }
            docFreq.put(term, df);
        }

        Map<String, Double> scoresByChunkId = new LinkedHashMap<>();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            List<String> docTokens = docTokensList.get(i);
            int docLen = docTokens.size();

            Map<String, Integer> tfMap = new HashMap<>();
            for (String tok : docTokens) {
                tfMap.merge(tok, 1, Integer::sum);
            }

            double score = 0.0;
            for (String term : queryTerms) {
                int tf = tfMap.getOrDefault(term, 0);
                if (tf == 0) {
                    continue;
                }
                int df = docFreq.getOrDefault(term, 0);
                double idf = Math.log(1.0 + (totalDocs - df + 0.5) / (df + 0.5));
                double denom = tf + K1 * (1.0 - B + B * (docLen / Math.max(1.0, avgDocLen)));
                score += idf * ((tf * (K1 + 1.0)) / denom);
            }
            scoresByChunkId.put(chunk.chunkId(), score);
        }

        List<RankedHit> sorted = chunks.stream()
                .map(c -> new RankedHit(c, scoresByChunkId.getOrDefault(c.chunkId(), 0.0), 0))
                .sorted(Comparator.comparingDouble(RankedHit::score).reversed())
                .limit(limit)
                .toList();

        List<RankedHit> ranked = new java.util.ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            RankedHit hit = sorted.get(i);
            ranked.add(new RankedHit(hit.chunk(), hit.score(), i + 1));
        }
        return ranked;
    }

    private RankedHit assignRank(RankedHit hit, int fallbackIdx) {
        return new RankedHit(hit.chunk(), hit.score(), fallbackIdx + 1);
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9_]+"))
                .filter(t -> !t.isBlank())
                .toList();
    }
}
