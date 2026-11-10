package com.arivubuilds.api.websocket.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeEvent(
        String tradeId,
        String symbol,
        BigDecimal price,
        int volume,
        String side,
        Instant timestamp
) {}