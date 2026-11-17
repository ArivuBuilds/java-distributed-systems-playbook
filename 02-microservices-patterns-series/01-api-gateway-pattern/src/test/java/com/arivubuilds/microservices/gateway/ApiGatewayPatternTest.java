package com.arivubuilds.microservices.gateway;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayPatternTest {

    private static MockWebServer ordersServer;
    private static MockWebServer inventoryServer;

    @Autowired
    private ApplicationContext context;

    private WebTestClient webTestClient;

    @BeforeAll
    static void initBackends() throws IOException {
        ordersServer = new MockWebServer();
        ordersServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if ("/orders/ORD-501".equals(request.getPath())) {
                    return new MockResponse()
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody("{\"orderId\":\"ORD-501\",\"status\":\"SHIPPED\"}");
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        ordersServer.start();

        inventoryServer = new MockWebServer();
        inventoryServer.start();
    }

    @AfterAll
    static void shutdownBackends() throws IOException {
        ordersServer.shutdown();
        inventoryServer.shutdown();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("services.orders-uri", () -> ordersServer.url("/").toString().replaceAll("/$", ""));
        registry.add("services.inventory-uri", () -> inventoryServer.url("/").toString().replaceAll("/$", ""));
    }

    @BeforeEach
    void setupClient() {
        this.webTestClient = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .responseTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Test
    @DisplayName("Fail Fast at Gateway: Rejects unauthenticated ingress request with HTTP 401 before hitting downstream")
    void testUnauthenticatedRequestRejectedAtEdge() {
        webTestClient.get()
                .uri("/api/orders/ORD-501")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("UNAUTHORIZED");

        assertEquals(0, ordersServer.getRequestCount());
    }

    @Test
    @DisplayName("SSL & Token Offload: Strips /api prefix, terminates SSL metadata, strips Authorization, injects identity headers")
    void testIngressRoutingSslAndTokenOffload() throws Exception {
        String token = Base64.getEncoder().encodeToString("arivumani:ARCHITECT,ADMIN".getBytes(StandardCharsets.UTF_8));

        webTestClient.get()
                .uri("/api/orders/ORD-501")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Request-Id")
                .expectBody()
                .jsonPath("$.orderId").isEqualTo("ORD-501")
                .jsonPath("$.status").isEqualTo("SHIPPED");

        RecordedRequest downstream = null;
        RecordedRequest candidate;
        while ((candidate = ordersServer.takeRequest(200, TimeUnit.MILLISECONDS)) != null) {
            if ("/orders/ORD-501".equals(candidate.getPath())) {
                downstream = candidate;
            }
        }

        assertNotNull(downstream);
        assertEquals("https", downstream.getHeader("X-Forwarded-Proto"));
        assertEquals("443", downstream.getHeader("X-Forwarded-Port"));
        assertEquals("true", downstream.getHeader("X-SSL-Terminated-At-Edge"));
        assertEquals("arivumani", downstream.getHeader("X-Authenticated-User"));
        assertEquals("ARCHITECT,ADMIN", downstream.getHeader("X-User-Roles"));
        assertNull(downstream.getHeader(HttpHeaders.AUTHORIZATION), "Raw Bearer token must be stripped at edge");
    }
}