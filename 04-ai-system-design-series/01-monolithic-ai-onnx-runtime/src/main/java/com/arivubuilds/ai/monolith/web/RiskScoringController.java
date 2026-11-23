package com.arivubuilds.ai.monolith.web;

import com.arivubuilds.ai.monolith.service.FencedOnnxInferenceService;
import com.arivubuilds.ai.monolith.service.FencedOnnxInferenceService.InferenceOverloadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inference")
public class RiskScoringController {

    private final FencedOnnxInferenceService inferenceService;

    public RiskScoringController(FencedOnnxInferenceService inferenceService) {
        this.inferenceService = inferenceService;
    }

    @PostMapping("score")
    public ResponseEntity score(@RequestBody ScoreRequest request) {
        long startNanos = System.nanoTime();
        float[] scores = inferenceService.predict(request.features());
        double latencyMs = (System.nanoTime() - startNanos) / 1_000_000.0;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scores", scores);
        body.put("latencyMs", latencyMs);
        body.put("remainingPermits", inferenceService.availablePermits());
        return ResponseEntity.ok(body);
    }

    @ExceptionHandler(InferenceOverloadException.class)
    public ResponseEntity handleOverload(InferenceOverloadException ex) {
        Map<String, String> errorBody = new LinkedHashMap<>();
        errorBody.put("error", "INFERENCE_BULKHEAD_FULL");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "1")
                .body(errorBody);
    }

    public record ScoreRequest(float[] features) {
    }
}