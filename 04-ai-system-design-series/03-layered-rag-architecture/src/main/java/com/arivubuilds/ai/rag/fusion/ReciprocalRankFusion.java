package com.arivubuilds.ai.rag.fusion;

import com.arivubuilds.ai.rag.model.RagModels.DocumentChunk;
import com.arivubuilds.ai.rag.model.RagModels.ScoredCandidate;
import com.arivubuilds.ai.rag.retrieval.HybridRetriever.RankedHit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReciprocalRankFusion {

    private final int rrfK;

    public ReciprocalRankFusion(@Value("${rag.retrieval.rrf-k:60}") int rrfK) {
        this.rrfK = Math.max(1, rrfK);
    }

    public List<ScoredCandidate> fuse(
            List<RankedHit> denseHits,
            List<RankedHit> bm25Hits,
            int maxCandidates) {

        Map<String, DocumentChunk> chunkById = new LinkedHashMap<>();
        Map<String, Double> denseScoreMap = new HashMap<>();
        Map<String, Integer> denseRankMap = new HashMap<>();
        Map<String, Double> bm25ScoreMap = new HashMap<>();
        Map<String, Integer> bm25RankMap = new HashMap<>();

        for (int i = 0; i < denseHits.size(); i++) {
            RankedHit hit = denseHits.get(i);
            String id = hit.chunk().chunkId();
            chunkById.put(id, hit.chunk());
            denseScoreMap.put(id, hit.score());
            denseRankMap.put(id, i + 1);
        }

        for (int i = 0; i < bm25Hits.size(); i++) {
            RankedHit hit = bm25Hits.get(i);
            String id = hit.chunk().chunkId();
            chunkById.put(id, hit.chunk());
            bm25ScoreMap.put(id, hit.score());
            bm25RankMap.put(id, i + 1);
        }

        List<ScoredCandidate> fused = new java.util.ArrayList<>(chunkById.size());
        for (Map.Entry<String, DocumentChunk> entry : chunkById.entrySet()) {
            String id = entry.getKey();
            DocumentChunk chunk = entry.getValue();

            double dScore = denseScoreMap.getOrDefault(id, 0.0);
            int dRank = denseRankMap.getOrDefault(id, Integer.MAX_VALUE);
            double bScore = bm25ScoreMap.getOrDefault(id, 0.0);
            int bRank = bm25RankMap.getOrDefault(id, Integer.MAX_VALUE);

            double rrf = 0.0;
            if (dRank != Integer.MAX_VALUE) {
                rrf += 1.0 / (rrfK + dRank);
            }
            if (bRank != Integer.MAX_VALUE) {
                rrf += 1.0 / (rrfK + bRank);
            }

            fused.add(new ScoredCandidate(
                    chunk,
                    dScore,
                    dRank == Integer.MAX_VALUE ? -1 : dRank,
                    bScore,
                    bRank == Integer.MAX_VALUE ? -1 : bRank,
                    rrf,
                    0.0,
                    rrf
            ));
        }

        return fused.stream()
                .sorted(Comparator.comparingDouble(ScoredCandidate::rrfScore).reversed())
                .limit(Math.max(1, maxCandidates))
                .toList();
    }
}
