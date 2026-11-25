package com.arivubuilds.ai.rag.service;

import com.arivubuilds.ai.rag.chunking.SemanticChunker;
import com.arivubuilds.ai.rag.embedding.DeterministicEmbeddingEngine;
import com.arivubuilds.ai.rag.fusion.ReciprocalRankFusion;
import com.arivubuilds.ai.rag.model.RagModels.DocumentChunk;
import com.arivubuilds.ai.rag.model.RagModels.DocumentInput;
import com.arivubuilds.ai.rag.model.RagModels.RagCitation;
import com.arivubuilds.ai.rag.model.RagModels.RagQueryRequest;
import com.arivubuilds.ai.rag.model.RagModels.RagQueryResponse;
import com.arivubuilds.ai.rag.model.RagModels.ScoredCandidate;
import com.arivubuilds.ai.rag.rerank.CrossEncoderReranker;
import com.arivubuilds.ai.rag.retrieval.HybridRetriever;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class LayeredRagService {

    private final SemanticChunker chunker;
    private final DeterministicEmbeddingEngine embeddingEngine;
    private final HybridRetriever hybridRetriever;
    private final ReciprocalRankFusion rrf;
    private final CrossEncoderReranker reranker;
    private final CopyOnWriteArrayList<DocumentChunk> chunkIndex = new CopyOnWriteArrayList<>();

    public LayeredRagService(
            SemanticChunker chunker,
            DeterministicEmbeddingEngine embeddingEngine,
            HybridRetriever hybridRetriever,
            ReciprocalRankFusion rrf,
            CrossEncoderReranker reranker) {
        this.chunker = chunker;
        this.embeddingEngine = embeddingEngine;
        this.hybridRetriever = hybridRetriever;
        this.rrf = rrf;
        this.reranker = reranker;
    }

    public int ingest(DocumentInput input) {
        List<DocumentChunk> produced = chunker.chunkDocument(input, embeddingEngine);
        chunkIndex.addAll(produced);
        return produced.size();
    }

    public void clearIndex() {
        chunkIndex.clear();
    }

    public int totalChunks() {
        return chunkIndex.size();
    }

    public RagQueryResponse query(RagQueryRequest request) {
        List<DocumentChunk> snapshot = List.copyOf(chunkIndex);
        if (snapshot.isEmpty()) {
            return new RagQueryResponse(
                    request.query(),
                    "No indexed documents available to answer the query.",
                    List.of(),
                    0
            );
        }

        double[] qEmb = embeddingEngine.embed(request.query());
        var denseHits = hybridRetriever.denseSearch(snapshot, qEmb, request.candidateLimit());
        var bm25Hits = hybridRetriever.bm25Search(snapshot, request.query(), request.candidateLimit());

        List<ScoredCandidate> fused = rrf.fuse(denseHits, bm25Hits, request.candidateLimit());
        List<ScoredCandidate> reranked = reranker.rerank(request.query(), fused, request.topK());

        List<RagCitation> citations = reranked.stream()
                .map(c -> new RagCitation(
                        c.chunk().chunkId(),
                        c.chunk().documentId(),
                        c.chunk().source(),
                        c.rrfScore(),
                        c.crossEncoderScore(),
                        c.chunk().text()
                ))
                .toList();

        String groundedAnswer = reranked.stream()
                .map(c -> "[" + c.chunk().chunkId() + "] " + c.chunk().text())
                .collect(Collectors.joining(" | "));

        return new RagQueryResponse(
                request.query(),
                groundedAnswer,
                citations,
                snapshot.size()
        );
    }
}
