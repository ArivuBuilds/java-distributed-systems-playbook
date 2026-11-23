package com.arivubuilds.microservices.bff;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BffPatternTest {

    private static MockWebServer profileServer;
    private static MockWebServer ordersServer;
    private static MockWebServer loyaltyServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void startDownstreams() throws IOException {
        profileServer = new MockWebServer();
        profileServer.start();

        ordersServer = new MockWebServer();
        ordersServer.start();

        loyaltyServer = new MockWebServer();
        loyaltyServer.start();
    }

    @AfterAll
    static void stopDownstreams() throws IOException {
        profileServer.shutdown();
        ordersServer.shutdown();
        loyaltyServer.shutdown();
    }

    @DynamicPropertySource
    static void registerDownstreamUrls(DynamicPropertyRegistry registry) {
        registry.add("downstream.profile-uri", () -> profileServer.url("/").toString().replaceAll("/$", ""));
        registry.add("downstream.orders-uri", () -> ordersServer.url("/").toString().replaceAll("/$", ""));
        registry.add("downstream.loyalty-uri", () -> loyaltyServer.url("/").toString().replaceAll("/$", ""));
    }

    private void enqueueHealthyDownstreams() {
        profileServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"customerId":"CUST-77","fullName":"Arivumani A","email":"arivu@example.com","phone":"+91-9000000000","city":"Coimbatore","kycStatus":"VERIFIED"}
                        """));

        ordersServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"customerId":"CUST-77","orders":[
                          {"orderId":"ORD-1","itemDescription":"MacBook Stand","totalAmount":2500.00,"status":"DELIVERED","warehouseCode":"MAA-1"},
                          {"orderId":"ORD-2","itemDescription":"Mechanical Keyboard","totalAmount":6500.00,"status":"SHIPPED","warehouseCode":"BLR-2"}
                        ]}
                        """));

        loyaltyServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"customerId":"CUST-77","tier":"PLATINUM","pointsBalance":14200,"pointsExpiringSoon":500}
                        """));
    }

    @Test
    @DisplayName("Mobile BFF Contract: Returns compact payload tailored for cellular bandwidth")
    void testMobileBffSlimPayloadContract() throws Exception {
        enqueueHealthyDownstreams();

        mockMvc.perform(get("/bff/mobile/home/CUST-77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("CUST-77"))
                .andExpect(jsonPath("$.displayName").value("Arivumani"))
                .andExpect(jsonPath("$.activeOrderCount").value(2))
                .andExpect(jsonPath("$.latestOrderAmount").value(2500.00))
                .andExpect(jsonPath("$.loyaltyTier").value("PLATINUM"))
                .andExpect(jsonPath("$.degradedLoyaltyData").value(false))
                .andExpect(jsonPath("$.profile").doesNotExist())
                .andExpect(jsonPath("$.recentOrders").doesNotExist());
    }

    @Test
    @DisplayName("Web BFF Contract & Graceful Degradation: Aggregates rich dashboard even when Loyalty fails")
    void testWebBffRichDashboardWithPartialDegradation() throws Exception {
        profileServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"customerId":"CUST-77","fullName":"Arivumani A","email":"arivu@example.com","phone":"+91-9000000000","city":"Coimbatore","kycStatus":"VERIFIED"}
                        """));

        ordersServer.enqueue(new MockResponse()
                .setHeadersDelay(150, TimeUnit.MILLISECONDS)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"customerId":"CUST-77","orders":[
                          {"orderId":"ORD-1","itemDescription":"MacBook Stand","totalAmount":2500.00,"status":"DELIVERED","warehouseCode":"MAA-1"},
                          {"orderId":"ORD-2","itemDescription":"Mechanical Keyboard","totalAmount":6500.00,"status":"SHIPPED","warehouseCode":"BLR-2"}
                        ]}
                        """));

        loyaltyServer.enqueue(new MockResponse().setResponseCode(503));

        mockMvc.perform(get("/bff/web/dashboard/CUST-77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.city").value("Coimbatore"))
                .andExpect(jsonPath("$.recentOrders.length()").value(2))
                .andExpect(jsonPath("$.lifetimeSpend").value(9000.00))
                .andExpect(jsonPath("$.loyalty.tier").value("STANDARD"))
                .andExpect(jsonPath("$.degradedLoyaltyData").value(true));
    }
}