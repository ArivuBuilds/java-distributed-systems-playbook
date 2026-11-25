package com.arivubuilds.ai.rag.model;

import java.util.List;
import java.util.Map;

public final class RagModels {

    private RagModels() {
    }

    public record DocumentInput(
            String documentId,
            String source,
            String content,
            Map<String, String> metadata
    ) {
    }

    public record DocumentChunk(
            String chunkId,
            String documentId,
            String source,
            int chunkIndex,
            String text,
            double[] embedding
    ) {
    }

    public record ScoredCandidate(
            DocumentChunk chunk,
            double denseScore,
            int denseRank,
            double bm25Score,
            int bm25Rank,
            double rrfScore,
            double crossEncoderScore,
            double finalScore
    ) {
    }

    public record RagQueryRequest(
            String query,
            int topK,
            int candidateLimit
    ) {
        public RagQueryRequest {
            if (topK <= 0) {
                topK = 3;
            }
            if (candidateLimit <= 0) {
                candidateLimit = 8;
            }
        }
    }

    public record RagCitation(
            String chunkId,
            String documentId,
            String source,
            double rrfScore,
            double crossEncoderScore,
            String snippet
    ) {
    }

    public record RagQueryResponse(
            String query,
            String groundedAnswer,
            List<RagCitation> citations,
            int indexedChunks
    ) {
    }
}
