package com.arivubuilds.ai.rag.chunking;

import com.arivubuilds.ai.rag.model.RagModels.DocumentChunk;
import com.arivubuilds.ai.rag.model.RagModels.DocumentInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class SemanticChunker {

    private final int maxChars;
    private final int overlapSentences;

    public SemanticChunker(
            @Value("${rag.chunking.max-chars:320}") int maxChars,
            @Value("${rag.chunking.overlap-sentences:1}") int overlapSentences) {
        this.maxChars = Math.max(80, maxChars);
        this.overlapSentences = Math.max(0, overlapSentences);
    }

    public List<String> splitIntoSemanticUnits(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        String[] paragraphs = content.split("\\R{2,}");
        List<String> units = new ArrayList<>();
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] sentences = trimmed.split("(?<=[.!?])\\s+");
            for (String sentence : sentences) {
                String s = sentence.trim();
                if (!s.isEmpty()) {
                    units.add(s);
                }
            }
        }
        return units;
    }

    public List<DocumentChunk> chunkDocument(DocumentInput input, EmbeddingEncoder encoder) {
        List<String> units = splitIntoSemanticUnits(input.content());
        List<DocumentChunk> chunks = new ArrayList<>();
        if (units.isEmpty()) {
            return chunks;
        }

        List<String> currentWindow = new ArrayList<>();
        int currentLen = 0;
        int index = 0;

        for (String unit : units) {
            int projectedLen = currentLen + (currentWindow.isEmpty() ? 0 : 1) + unit.length();
            if (!currentWindow.isEmpty() && projectedLen > maxChars) {
                chunks.add(buildChunk(input, index++, currentWindow, encoder));
                List<String> overlap = tailOverlap(currentWindow);
                currentWindow = new ArrayList<>(overlap);
                currentLen = currentWindow.stream().mapToInt(String::length).sum()
                        + Math.max(0, currentWindow.size() - 1);
            }
            currentWindow.add(unit);
            currentLen += (currentWindow.size() > 1 ? 1 : 0) + unit.length();
        }

        if (!currentWindow.isEmpty()) {
            chunks.add(buildChunk(input, index, currentWindow, encoder));
        }
        return chunks;
    }

    private List<String> tailOverlap(List<String> window) {
        if (overlapSentences <= 0 || window.isEmpty()) {
            return List.of();
        }
        int fromIndex = Math.max(0, window.size() - overlapSentences);
        return window.subList(fromIndex, window.size());
    }

    private DocumentChunk buildChunk(
            DocumentInput input,
            int index,
            List<String> window,
            EmbeddingEncoder encoder) {
        String text = String.join(" ", window);
        String chunkId = input.documentId() + "-chunk-" + index;
        String source = input.source() != null ? input.source() : input.documentId();
        return new DocumentChunk(
                chunkId,
                input.documentId(),
                source,
                index,
                text,
                encoder.embed(text.toLowerCase(Locale.ROOT))
        );
    }

    @FunctionalInterface
    public interface EmbeddingEncoder {
        double[] embed(String normalizedText);
    }
}
