package com.arivubuilds.microservices.dbperservice;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DatabasePerServicePatternTest {

    private static MockWebServer catalogApiServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void startServer() throws IOException {
        catalogApiServer = new MockWebServer();
        catalogApiServer.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        catalogApiServer.shutdown();
    }

    @DynamicPropertySource
    static void configureCatalogUri(DynamicPropertyRegistry registry) {
        registry.add("downstream.catalog-service-uri", () -> catalogApiServer.url("/").toString().replaceAll("/$", ""));
    }

    @Test
    @DisplayName("API Composition & Bounded Context Isolation: Joins relational Order ledger with remote Catalog NoSQL doc without cross-DB SQL JOINs")
    void testApiCompositionAndEventDrivenSnapshotIsolation() throws Exception {
        // 1. Persist order in local relational H2 Orders DB with point-in-time price snapshot (18,500)
        mockMvc.perform(post("/api/v1/bounded/orders")
                        .param("orderId", "ORD-5001")
                        .param("customerId", "CUST-88")
                        .param("productId", "PROD-900")
                        .param("quantity", "1")
                        .param("unitPrice", "18500.00")
                        .param("title", "Keychron Q1 Pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-5001"))
                .andExpect(jsonPath("$.unitPriceSnapshot").value(18500.00));

        // 2. Remote Catalog service raises live list price to 19,900 -> API Composition joins both contexts cleanly
        catalogApiServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "productId":"PROD-900",
                          "title":"Keychron Q1 Pro (V2 Wireless)",
                          "category":"PERIPHERALS",
                          "currentListPrice":19900.00,
                          "flexibleSpecs":{"switches":"Banana","layout":"75%"}
                        }
                        """));

        mockMvc.perform(get("/api/v1/bounded/orders/ORD-5001/composed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compositionSource").value("API_COMPOSED_ACROSS_BOUNDED_CONTEXTS"))
                .andExpect(jsonPath("$.purchasedUnitPrice").value(18500.00))
                .andExpect(jsonPath("$.liveCatalogPrice").value(19900.00))
                .andExpect(jsonPath("$.liveCatalogSpecs.switches").value("Banana"));

        // 3. Domain event updates local Order projection title while preserving immutable purchasedUnitPrice
        mockMvc.perform(post("/api/v1/bounded/events/product-renamed")
                        .param("productId", "PROD-900")
                        .param("newTitle", "Keychron Q1 Pro V2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedOrders").value(1));

        // 4. Even when remote Catalog API is down (503), Orders DB serves its bounded snapshot independently
        catalogApiServer.enqueue(new MockResponse().setResponseCode(503));

        mockMvc.perform(get("/api/v1/bounded/orders/ORD-5001/composed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compositionSource").value("LOCAL_ORDERS_SNAPSHOT_FALLBACK"))
                .andExpect(jsonPath("$.purchasedUnitPrice").value(18500.00))
                .andExpect(jsonPath("$.liveCatalogTitle").value("Keychron Q1 Pro V2"));
    }
}