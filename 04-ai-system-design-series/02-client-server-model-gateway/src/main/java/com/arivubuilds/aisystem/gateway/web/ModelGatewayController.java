package com.arivubuilds.aisystem.gateway.web;

import com.arivubuilds.aisystem.gateway.model.GatewayContracts.CompletionRequest;
import com.arivubuilds.aisystem.gateway.model.GatewayContracts.CompletionResponse;
import com.arivubuilds.aisystem.gateway.service.ModelGatewayService;
import com.arivubuilds.aisystem.gateway.service.TenantTokenBudgetService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gateway")
public class ModelGatewayController {

    private final ModelGatewayService gatewayService;
    private final TenantTokenBudgetService budgetService;

    public ModelGatewayController(ModelGatewayService gatewayService, TenantTokenBudgetService budgetService) {
        this.gatewayService = gatewayService;
        this.budgetService = budgetService;
    }

    @PostMapping("/completions")
    public CompletionResponse createCompletion(@Valid @RequestBody CompletionRequest request) {
        return gatewayService.complete(request);
    }

    @PostMapping(value = "/completions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCompletion(@Valid @RequestBody CompletionRequest request) {
        return gatewayService.stream(request);
    }

    @GetMapping("/tenants/{tenantId}/budget")
    public Map<String, Object> getTenantBudget(@PathVariable String tenantId) {
        return Map.of(
                "tenantId", tenantId,
                "remainingTokens", budgetService.getRemaining(tenantId)
        );
    }
}