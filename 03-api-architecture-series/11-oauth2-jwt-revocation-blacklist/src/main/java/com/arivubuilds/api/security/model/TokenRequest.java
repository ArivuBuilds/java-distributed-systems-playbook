package com.arivubuilds.api.security.model;

import java.util.List;

public record TokenRequest(
        String subject,
        List<String> scopes,
        long ttlSeconds
) {}