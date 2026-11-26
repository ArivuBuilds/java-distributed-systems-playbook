package com.arivubuilds.ai.mvc.model;

public record SseTokenEvent(
        String sessionId,
        int sequence,
        String promptVersion,
        String delta,
        boolean done
) {
}
