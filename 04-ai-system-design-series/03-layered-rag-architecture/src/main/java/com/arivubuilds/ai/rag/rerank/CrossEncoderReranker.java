package com.arivubuilds.ai.rag.rerank;

import com.arivubuilds.ai.rag.model.RagModels.ScoredCandidate;
import com.arivubuilds.ai.rag.retrieval.HybridRetriever;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class CrossEncoderReranker {

    public List<ScoredCandidate> rerank(String query, List<ScoredCandidate> candidates, int topK) {
        Set<String> queryTerms = new HashSet<>(HybridRetriever.tokenize(query));
        String normalizedQuery = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();

        return candidates.stream()
                .map(c -> {
                    double ceScore = scorePair(normalizedQuery, queryTerms, c.chunk().text());
                    double combined = (0.65 * ceScore) + (0.35 * normalizeRrf(c.rrfScore()));
                    return new ScoredCandidate(
                            c.chunk(),
                            c.denseScore(),
                            c.denseRank(),
                            c.bm25Score(),
                            c.bm25Rank(),
                            c.rrfScore(),
                            ceScore,
                            combined
                    );
                })
                .sorted(Comparator.comparingDouble(ScoredCandidate::finalScore).reversed())
                .limit(Math.max(1, topK))
                .toList();
    }

    private double scorePair(String normalizedQuery, Set<String> queryTerms, String passageText) {
        if (passageText == null || passageText.isBlank() || queryTerms.isEmpty()) {
            return 0.0;
        }
        String lowerPassage = passageText.toLowerCase(Locale.ROOT);
        List<String> passageTokens = HybridRetriever.tokenize(lowerPassage);
        Set<String> passageSet = new HashSet<>(passageTokens);

        long overlap = queryTerms.stream().filter(passageSet::contains).count();
        double termCoverage = (double) overlap / queryTerms.size();

        double phraseBonus = (!normalizedQuery.isBlank() && lowerPassage.contains(normalizedQuery)) ? 0.35 : 0.0;
        double adjacencyBonus = consecutiveBigramRatio(queryTerms, passageTokens) * 0.25;

        return Math.min(1.0, (0.65 * termCoverage) + phraseBonus + adjacencyBonus);
    }

    private double consecutiveBigramRatio(Set<String> queryTerms, List<String> passageTokens) {
        if (passageTokens.size() < 2 || queryTerms.size() < 2) {
            return 0.0;
        }
        int matchedBigrams = 0;
        int totalBigrams = 0;
        for (int i = 0; i < passageTokens.size() - 1; i++) {
            String a = passageTokens.get(i);
            String b = passageTokens.get(i + 1);
            if (queryTerms.contains(a) && queryTerms.contains(b)) {
                totalBigrams++;
                matchedBigrams++;
            }
        }
        return totalBigrams == 0 ? 0.0 : Math.min(1.0, (double) matchedBigrams / queryTerms.size());
    }

    private double normalizeRrf(double rrf) {
        return Math.min(1.0, rrf * 30.0);
    }
}
