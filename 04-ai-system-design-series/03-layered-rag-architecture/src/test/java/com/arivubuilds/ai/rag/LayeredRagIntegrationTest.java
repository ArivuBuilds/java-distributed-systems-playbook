package com.arivubuilds.ai.rag;

import com.arivubuilds.ai.rag.model.RagModels.DocumentInput;
import com.arivubuilds.ai.rag.model.RagModels.RagQueryRequest;
import com.arivubuilds.ai.rag.model.RagModels.RagQueryResponse;
import com.arivubuilds.ai.rag.service.LayeredRagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LayeredRagIntegrationTest {

    @Autowired
    private LayeredRagService ragService;

    @BeforeEach
    void reset() {
        ragService.clearIndex();
    }

    @Test
    void shouldChunkSemanticallyFuseHybridResultsAndRerankTopPassages() {
        ragService.ingest(new DocumentInput(
                "doc-rrf",
                "retrieval-spec.md",
                "Reciprocal Rank Fusion combines dense vector rankings and BM25 lexical rankings without score calibration. "
                        + "Cross-encoder reranking scores query and passage pairs jointly for higher precision. "
                        + "Semantic chunking preserves paragraph and sentence boundaries across long technical guides.",
                Map.of("domain", "rag")
        ));

        ragService.ingest(new DocumentInput(
                "doc-gateway",
                "gateway-spec.md",
                "API gateways enforce authentication, rate limiting, and circuit breakers at the network edge. "
                        + "Upstream services remain isolated behind ingress routing rules.",
                Map.of("domain", "gateway")
        ));

        assertTrue(ragService.totalChunks() >= 2);

        RagQueryResponse response = ragService.query(
                new RagQueryRequest("Reciprocal Rank Fusion cross-encoder reranking", 2, 6)
        );

        assertFalse(response.citations().isEmpty());
        assertEquals("doc-rrf", response.citations().get(0).documentId());
        assertTrue(response.citations().get(0).rrfScore() > 0.0);
        assertTrue(response.citations().get(0).crossEncoderScore() > 0.0);
        assertTrue(response.groundedAnswer().contains("doc-rrf"));
    }
}
