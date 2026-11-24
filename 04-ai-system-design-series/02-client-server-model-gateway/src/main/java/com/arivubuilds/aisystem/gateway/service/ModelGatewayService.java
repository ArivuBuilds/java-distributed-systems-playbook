package com.arivubuilds.aisystem.gateway.service;

import com.arivubuilds.aisystem.gateway.model.GatewayContracts.*;
import com.arivubuilds.aisystem.gateway.provider.ModelProviderAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ModelGatewayService {

    private final DynamicModelRouter router;
    private final TenantTokenBudgetService budgetService;

    public ModelGatewayService(DynamicModelRouter router, TenantTokenBudgetService budgetService) {
        this.router = router;
        this.budgetService = budgetService;
    }

    public CompletionResponse complete(CompletionRequest request) {
        String requestId = UUID.randomUUID().toString();
        ModelProviderAdapter provider = router.selectProvider(request);

        int promptTokens = estimateTokenCount(request.prompt());
        ProviderQuote quote = provider.execute(request);
        int completionTokens = Math.min(quote.tokens().size(), request.effectiveMaxTokens());
        int totalTokens = promptTokens + completionTokens;

        int remainingBudget = budgetService.reserveOrThrow(request.tenantId(), totalTokens);
        String content = String.join("", quote.tokens().subList(0, completionTokens));

        return new CompletionResponse(
                requestId,
                provider.providerId(),
                provider.modelName(),
                content,
                quote.estimatedLatencyMs(),
                new TokenUsage(promptTokens, completionTokens, totalTokens, remainingBudget),
                Instant.now()
        );
    }

    public SseEmitter stream(CompletionRequest request) {
        String requestId = UUID.randomUUID().toString();
        ModelProviderAdapter provider = router.selectProvider(request);

        int promptTokens = estimateTokenCount(request.prompt());
        ProviderQuote quote = provider.execute(request);
        int completionTokens = Math.min(quote.tokens().size(), request.effectiveMaxTokens());
        budgetService.reserveOrThrow(request.tenantId(), promptTokens + completionTokens);

        SseEmitter emitter = new SseEmitter(15_000L);
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < completionTokens; i++) {
                    StreamChunk chunk = new StreamChunk(requestId, provider.providerId(), i, quote.tokens().get(i), false);
                    emitter.send(SseEmitter.event().name("token").data(chunk));
                }
                StreamChunk endChunk = new StreamChunk(requestId, provider.providerId(), completionTokens, "", true);
                emitter.send(SseEmitter.event().name("done").data(endChunk));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    private int estimateTokenCount(String text) {
        return Math.max(1, text.trim().split("\\s+").length);
    }
}