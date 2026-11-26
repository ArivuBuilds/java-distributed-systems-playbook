package com.arivubuilds.ai.mvc.model;

import java.time.Instant;

public record ChatTurn(
        String role,
        String content,
        int estimatedTokens,
        Instant timestamp
) {
    public static ChatTurn of(String role, String content) {
        int tokens = Math.max(1, (content.length() + 3) / 4);
        return new ChatTurn(role, content, tokens, Instant.now());
    }
}
