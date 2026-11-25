package com.arivubuilds.ai.rag.controller;

import com.arivubuilds.ai.rag.model.RagModels.DocumentInput;
import com.arivubuilds.ai.rag.model.RagModels.RagQueryRequest;
import com.arivubuilds.ai.rag.model.RagModels.RagQueryResponse;
import com.arivubuilds.ai.rag.service.LayeredRagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class LayeredRagController {

    private final LayeredRagService ragService;

    public LayeredRagController(LayeredRagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody DocumentInput input) {
        int chunksAdded = ragService.ingest(input);
        return ResponseEntity.ok(Map.of(
                "documentId", input.documentId(),
                "chunksAdded", chunksAdded,
                "totalChunks", ragService.totalChunks()
        ));
    }

    @PostMapping("/query")
    public ResponseEntity<RagQueryResponse> query(@RequestBody RagQueryRequest request) {
        return ResponseEntity.ok(ragService.query(request));
    }
}
