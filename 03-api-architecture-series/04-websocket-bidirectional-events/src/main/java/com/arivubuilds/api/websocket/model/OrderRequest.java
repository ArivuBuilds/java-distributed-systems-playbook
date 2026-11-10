package com.arivubuilds.api.websocket.model;

import java.math.BigDecimal;

public record OrderRequest(
        String symbol,
        BigDecimal price,
        int volume,
        String side
) {}