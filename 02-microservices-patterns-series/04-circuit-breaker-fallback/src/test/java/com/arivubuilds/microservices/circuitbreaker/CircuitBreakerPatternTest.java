package com.arivubuilds.microservices.circuitbreaker;

import com.arivubuilds.microservices.circuitbreaker.service.CreditEvaluationService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CircuitBreakerPatternTest {

    private static MockWebServer creditServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CreditEvaluationService creditService;

    @Autowired
    private CircuitBreakerRegistry breakerRegistry;

    @BeforeAll
    static void startServer() throws IOException {
        creditServer = new MockWebServer();
        creditServer.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        creditServer.shutdown();
    }

    @DynamicPropertySource
    static void configureUpstream(DynamicPropertyRegistry registry) {
        registry.add("downstream.credit-scoring-uri", () -> creditServer.url("/").toString().replaceAll("/$", ""));
    }

    @BeforeEach
    void resetState() {
        creditService.resetBreaker();
    }

    @Test
    @DisplayName("State Lifecycle: CLOSED -> OPEN on 50% sliding window failures -> fail-fast fallback -> HALF_OPEN -> CLOSED recovery")
    void testCircuitBreakerFullLifecycleAndFallback() throws Exception {
        CircuitBreaker breaker = breakerRegistry.circuitBreaker(CreditEvaluationService.BREAKER_NAME);
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());

        int initialRequests = creditServer.getRequestCount();

        // 1. Two healthy calls populate the cache and keep breaker CLOSED
        for (int i = 0; i < 2; i++) {
            creditServer.enqueue(new MockResponse()
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody("""
                            {"customerId":"CUST-101","creditScore":790,"riskTier":"PRIME","source":"RAW","breakerState":"NA"}
                            """));

            mockMvc.perform(get("/api/v1/credit/CUST-101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.source").value("LIVE_UPSTREAM"))
                    .andExpect(jsonPath("$.creditScore").value(790));
        }
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());

        // 2. Next two calls fail with HTTP 500 -> 2/4 = 50% failure threshold reached -> trips to OPEN
        creditServer.enqueue(new MockResponse().setResponseCode(500));
        creditServer.enqueue(new MockResponse().setResponseCode(500));

        mockMvc.perform(get("/api/v1/credit/CUST-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("STALE_CACHE_FALLBACK"))
                .andExpect(jsonPath("$.creditScore").value(790));

        mockMvc.perform(get("/api/v1/credit/CUST-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("STALE_CACHE_FALLBACK"));

        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());
        int requestsWhenOpened = creditServer.getRequestCount();
        assertEquals(initialRequests + 4, requestsWhenOpened);

        // 3. While OPEN, requests fail-fast locally WITHOUT hitting MockWebServer
        mockMvc.perform(get("/api/v1/credit/CUST-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("DEFAULT_DEGRADED_FALLBACK"))
                .andExpect(jsonPath("$.creditScore").value(650))
                .andExpect(jsonPath("$.breakerState").value("OPEN"));

        assertEquals(requestsWhenOpened, creditServer.getRequestCount());

        // 4. Transition to HALF_OPEN and send 2 successful probe calls -> recovers back to CLOSED
        breaker.transitionToHalfOpenState();
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.getState());

        for (int i = 0; i < 2; i++) {
            creditServer.enqueue(new MockResponse()
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody("""
                            {"customerId":"CUST-101","creditScore":805,"riskTier":"PRIME","source":"RAW","breakerState":"NA"}
                            """));

            mockMvc.perform(get("/api/v1/credit/CUST-101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.source").value("LIVE_UPSTREAM"))
                    .andExpect(jsonPath("$.creditScore").value(805));
        }

        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());
    }
}