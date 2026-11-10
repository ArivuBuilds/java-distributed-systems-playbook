package com.arivubuilds.api.websocket;

import com.arivubuilds.api.websocket.model.OrderRequest;
import com.arivubuilds.api.websocket.model.TradeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(mapper);
        
        stompClient.setMessageConverter(converter);
    }

    @Test
    @DisplayName("STOMP over WebSocket: Send order to /app/order.execute and receive broadcast on /topic/trades")
    void testOrderExecutionAndBroadcast() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws-events";
        BlockingQueue<TradeEvent> receivedEvents = new LinkedBlockingQueue<>();

        StompSession session = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        assertNotNull(session);
        assertTrue(session.isConnected());

        // Subscribe to broadcast topic
        StompSession.Subscription subscription = session.subscribe("/topic/trades", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TradeEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedEvents.offer((TradeEvent) payload);
            }
        });

        assertNotNull(subscription);

        // Allow broker in-memory subscription registry to bind
        Thread.sleep(500);

        // Send execution request to @MessageMapping("/order.execute")
        OrderRequest request = new OrderRequest("INFY", new BigDecimal("1850.50"), 100, "BUY");
        session.send("/app/order.execute", request);

        // Await broadcast frame
        TradeEvent event = receivedEvents.poll(5, TimeUnit.SECONDS);

        assertNotNull(event, "Did not receive TradeEvent within timeout window");
        assertEquals("INFY", event.symbol());
        assertEquals("BUY", event.side());
        assertEquals(100, event.volume());
        assertNotNull(event.tradeId());

        session.disconnect();
    }
}