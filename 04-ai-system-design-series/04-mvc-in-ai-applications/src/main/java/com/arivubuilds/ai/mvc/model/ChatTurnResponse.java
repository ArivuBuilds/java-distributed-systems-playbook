package com.arivubuilds.ai.mvc.model;

import java.time.Instant;

public record ChatTurnResponse(
        String sessionId,
        String tenantId,
        String resolvedPromptVersion,
        int retainedHistoryTurns,
        int retainedHistoryTokens,
        String assistantReply,
        Instant timestamp
) {
}
