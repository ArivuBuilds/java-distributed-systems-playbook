package com.arivubuilds.ai.mvc.model;

import jakarta.validation.constraints.NotBlank;

public record ChatTurnRequest(
        @NotBlank(message = "sessionId is required")
        String sessionId,
        @NotBlank(message = "tenantId is required")
        String tenantId,
        String promptVersion,
        @NotBlank(message = "message must not be blank")
        String message
) {
}
