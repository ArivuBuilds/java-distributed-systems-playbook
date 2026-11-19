package com.arivubuilds.api.security.model;

import java.time.Instant;

public record TokenResponse(
        String accessToken,
        String tokenType,
        String jti,
        Instant expiresAt
) {}