package com.arivubuilds.api.websocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SessionTrackingChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SessionTrackingChannelInterceptor.class);
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            int current = activeConnections.incrementAndGet();
            log.info("[WebSocket STOMP] Client CONNECT: SessionId={} | Active Connections: {}", accessor.getSessionId(), current);
        } else if (StompCommand.DISCONNECT.equals(command)) {
            int current = activeConnections.decrementAndGet();
            log.info("[WebSocket STOMP] Client DISCONNECT: SessionId={} | Active Connections: {}", accessor.getSessionId(), current);
        }

        return message;
    }

    public int getActiveConnectionCount() {
        return activeConnections.get();
    }
}   