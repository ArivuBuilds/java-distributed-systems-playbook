package com.arivubuilds.api.websocket.controller;

import com.arivubuilds.api.websocket.model.OrderRequest;
import com.arivubuilds.api.websocket.model.TradeEvent;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

@Controller
public class TradeStreamController {

    private final SimpMessagingTemplate messagingTemplate;

    public TradeStreamController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/order.execute")
    @SendTo("/topic/trades")
    public TradeEvent executeOrder(OrderRequest orderRequest) {
        return new TradeEvent(
                UUID.randomUUID().toString(),
                orderRequest.symbol(),
                orderRequest.price(),
                orderRequest.volume(),
                orderRequest.side(),
                Instant.now()
        );
    }

    public void broadcastMarketStatus(String statusMessage) {
        messagingTemplate.convertAndSend("/topic/market-status", statusMessage);
    }
}