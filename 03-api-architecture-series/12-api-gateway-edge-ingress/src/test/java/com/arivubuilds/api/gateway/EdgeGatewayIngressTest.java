package com.arivubuilds.api.gateway;

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
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EdgeGatewayIngressTest {

    private static MockWebServer accountsBackend;
    private static MockWebServer transactionsBackend;

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient webTestClient;

    @BeforeAll
    static void startMockBackends() throws IOException {
        accountsBackend = new MockWebServer();
        accountsBackend.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if ("/accounts/CUST-01".equals(path)) {
                    return jsonResponse("""
                            {"accountId":"ACC-77","customerId":"CUST-01","accountType":"SAVINGS","availableBalance":125000.50,"currency":"INR"}
                            """);
                }
                if ("/accounts/CUST-02".equals(path)) {
                    return jsonResponse("""
                            {"accountId":"ACC-88","customerId":"CUST-02","accountType":"CURRENT","availableBalance":480000.00,"currency":"INR"}
                            """);
                }
                if ("/accounts/CUST-03".equals(path)) {
                    return jsonResponse("""
                            {"accountId":"ACC-99","customerId":"CUST-03","accountType":"SAVINGS","availableBalance":50000.00,"currency":"INR"}
                            """);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        accountsBackend.start();

        transactionsBackend = new MockWebServer();
        transactionsBackend.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if ("/transactions/CUST-02".equals(path)) {
                    return jsonResponse("""
                            [
                              {"txnId":"TXN-1","merchant":"AWS Cloud","amount":12400.00,"direction":"DEBIT"},
                              {"txnId":"TXN-2","merchant":"Client Retainer","amount":95000.00,"direction":"CREDIT"}
                            ]
                            """);
                }
                if ("/transactions/CUST-03".equals(path)) {
                    return new MockResponse().setResponseCode(503);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        transactionsBackend.start();
    }

    @AfterAll
    static void stopMockBackends() throws IOException {
        accountsBackend.shutdown();
        transactionsBackend.shutdown();
    }

    @DynamicPropertySource
    static void registerBackendProperties(DynamicPropertyRegistry registry) {
        registry.add("downstream.accounts-url", () -> accountsBackend.url("/").toString().replaceAll("/$", ""));
        registry.add("downstream.transactions-url", () -> transactionsBackend.url("/").toString().replaceAll("/$", ""));
    }

    @BeforeEach
    void setupWebTestClient() {
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .configureClient()
                .responseTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Test
    @DisplayName("Reverse Proxy & SSL Offloading: Strips /proxy prefix and injects X-Forwarded-Proto & X-Correlation-Id")
    void testReverseProxyRoutingAndSslOffloadingHeaders() throws Exception {
        webTestClient.get()
                .uri("/proxy/accounts/CUST-01")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Correlation-Id")
                .expectHeader().valueEquals("X-Edge-TLS-Offloaded", "true")
                .expectBody()
                .jsonPath("$.accountId").isEqualTo("ACC-77")
                .jsonPath("$.availableBalance").isEqualTo(125000.50);

        RecordedRequest downstreamReq = null;
        RecordedRequest candidate;
        while ((candidate = accountsBackend.takeRequest(200, TimeUnit.MILLISECONDS)) != null) {
            if ("/accounts/CUST-01".equals(candidate.getPath())) {
                downstreamReq = candidate;
            }
        }

        assertNotNull(downstreamReq, "Expected downstream request to /accounts/CUST-01");
        assertEquals("https", downstreamReq.getHeader("X-Forwarded-Proto"));
        assertEquals("accounts-core", downstreamReq.getHeader("X-Routed-Service"));
        assertNotNull(downstreamReq.getHeader("X-Correlation-Id"));
    }

    @Test
    @DisplayName("BFF Fan-Out Aggregation: Merges concurrent Account + Transaction calls into unified payload")
    void testBffDashboardFanOutAggregation() {
        webTestClient.get()
                .uri("/bff/v1/dashboard/CUST-02")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.customerId").isEqualTo("CUST-02")
                .jsonPath("$.account.accountId").isEqualTo("ACC-88")
                .jsonPath("$.recentTransactions.length()").isEqualTo(2)
                .jsonPath("$.recentTransactions[0].merchant").isEqualTo("AWS Cloud")
                .jsonPath("$.partialDegradation").isEqualTo(false);
    }

    @Test
    @DisplayName("BFF Graceful Degradation: Returns core account view when downstream transactions service returns 503")
    void testBffGracefulDegradationOnDownstreamFailure() {
        webTestClient.get()
                .uri("/bff/v1/dashboard/CUST-03")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.customerId").isEqualTo("CUST-03")
                .jsonPath("$.account.accountId").isEqualTo("ACC-99")
                .jsonPath("$.recentTransactions.length()").isEqualTo(0)
                .jsonPath("$.partialDegradation").isEqualTo(true);
    }

    private static MockResponse jsonResponse(String body) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }
}