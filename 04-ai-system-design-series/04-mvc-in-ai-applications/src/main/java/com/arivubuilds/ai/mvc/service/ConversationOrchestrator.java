package com.arivubuilds.ai.mvc.service;

import com.arivubuilds.ai.mvc.memory.ConversationMemoryStore;
import com.arivubuilds.ai.mvc.model.ChatTurn;
import com.arivubuilds.ai.mvc.model.ChatTurnRequest;
import com.arivubuilds.ai.mvc.model.ChatTurnResponse;
import com.arivubuilds.ai.mvc.model.SseTokenEvent;
import com.arivubuilds.ai.mvc.port.ModelGatewayPort;
import com.arivubuilds.ai.mvc.prompt.VersionedPromptRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ConversationOrchestrator {

    private final ConversationMemoryStore memoryStore;
    private final VersionedPromptRegistry promptRegistry;
    private final ModelGatewayPort modelGatewayPort;

    public ConversationOrchestrator(
            ConversationMemoryStore memoryStore,
            VersionedPromptRegistry promptRegistry,
            ModelGatewayPort modelGatewayPort) {
        this.memoryStore = memoryStore;
        this.promptRegistry = promptRegistry;
        this.modelGatewayPort = modelGatewayPort;
    }

    public ChatTurnResponse handleTurn(ChatTurnRequest request) {
        String resolvedVersion = promptRegistry.resolveVersion(request.promptVersion());

        List<ChatTurn> priorHistory = memoryStore.loadWindow(request.sessionId());
        String renderedPrompt = promptRegistry.render(resolvedVersion, Map.of(
                "tenantId", request.tenantId(),
                "history", memoryStore.formatHistory(priorHistory),
                "userMessage", request.message()
        ));
        memoryStore.appendTurn(request.sessionId(), ChatTurn.of("USER", request.message()));

        String reply = modelGatewayPort.generateCompletion(renderedPrompt);

        memoryStore.appendTurn(request.sessionId(), ChatTurn.of("ASSISTANT", reply));
        List<ChatTurn> updatedWindow = memoryStore.loadWindow(request.sessionId());

        return new ChatTurnResponse(
                request.sessionId(),
                request.tenantId(),
                resolvedVersion,
                updatedWindow.size(),
                memoryStore.currentWindowTokens(request.sessionId()),
                reply,
                Instant.now()
        );
    }

    public SseEmitter streamTurn(ChatTurnRequest request) {
        String resolvedVersion = promptRegistry.resolveVersion(request.promptVersion());
        List<ChatTurn> priorHistory = memoryStore.loadWindow(request.sessionId());
        String renderedPrompt = promptRegistry.render(resolvedVersion, Map.of(
                "tenantId", request.tenantId(),
                "history", memoryStore.formatHistory(priorHistory),
                "userMessage", request.message()
        ));
        memoryStore.appendTurn(request.sessionId(), ChatTurn.of("USER", request.message()));

        SseEmitter emitter = new SseEmitter(30_000L);
        Thread.startVirtualThread(() -> {
            try {
                List<String> deltas = modelGatewayPort.streamCompletion(renderedPrompt);
                StringBuilder fullReply = new StringBuilder();
                int seq = 0;
                for (String delta : deltas) {
                    fullReply.append(delta);
                    emitter.send(SseEmitter.event()
                            .name("delta")
                            .data(new SseTokenEvent(request.sessionId(), seq++, resolvedVersion, delta, false)));
                }
                memoryStore.appendTurn(request.sessionId(), ChatTurn.of("ASSISTANT", fullReply.toString()));
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(new SseTokenEvent(request.sessionId(), seq, resolvedVersion, "[DONE]", true)));
                emitter.complete();
            } catch (IOException | RuntimeException ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }
}
