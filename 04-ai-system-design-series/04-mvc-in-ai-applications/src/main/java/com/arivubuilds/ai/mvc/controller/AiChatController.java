package com.arivubuilds.ai.mvc.controller;

import com.arivubuilds.ai.mvc.model.ChatTurnRequest;
import com.arivubuilds.ai.mvc.model.ChatTurnResponse;
import com.arivubuilds.ai.mvc.service.ConversationOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/ai-mvc/chat")
public class AiChatController {

    private final ConversationOrchestrator orchestrator;

    public AiChatController(ConversationOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping(value = "/turn", produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatTurnResponse postTurn(@Valid @RequestBody ChatTurnRequest request) {
        return orchestrator.handleTurn(request);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTurn(@Valid @RequestBody ChatTurnRequest request) {
        return orchestrator.streamTurn(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleInvalidPromptVersion(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Invalid AI MVC Request Parameter");
        return detail;
    }
}
