package com.arivubuilds.microservices.bulkhead;

import com.arivubuilds.microservices.bulkhead.dto.WorkloadExecutionResponse;
import com.arivubuilds.microservices.bulkhead.service.BulkheadCompartmentService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BulkheadIsolationPatternTest {

    private static MockWebServer inventoryServer;
    private static MockWebServer analyticsServer;

    @Autowired
    private BulkheadCompartmentService compartmentService;

    @BeforeAll
    static void startServers() throws IOException {
        inventoryServer = new MockWebServer();
        inventoryServer.start();

        analyticsServer = new MockWebServer();
        analyticsServer.start();
    }

    @AfterAll
    static void stopServers() throws IOException {
        inventoryServer.shutdown();
        analyticsServer.shutdown();
    }

    @DynamicPropertySource
    static void registerUrls(DynamicPropertyRegistry registry) {
        registry.add("downstream.inventory-uri", () -> inventoryServer.url("/").toString().replaceAll("/$", ""));
        registry.add("downstream.analytics-uri", () -> analyticsServer.url("/").toString().replaceAll("/$", ""));
    }

    @Test
    @DisplayName("Semaphore Bulkhead: Admits maxConcurrentCalls=2 on caller thread and sheds 3rd concurrent burst immediately")
    void testSemaphoreBulkheadConcurrencyCap() throws Exception {
        for (int i = 0; i < 2; i++) {
            inventoryServer.enqueue(new MockResponse()
                    .setBodyDelay(350, TimeUnit.MILLISECONDS)
                    .setBody("RESERVED"));
        }

        try (ExecutorService callers = Executors.newFixedThreadPool(3)) {
            List<CompletableFuture<WorkloadExecutionResponse>> futures = IntStream.range(0, 3)
                    .mapToObj(i -> CompletableFuture.supplyAsync(
                            () -> compartmentService.executeInventoryWithSemaphore("SKU-500"), callers))
                    .toList();

            List<WorkloadExecutionResponse> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            long admitted = results.stream().filter(r -> "ADMITTED".equals(r.status())).count();
            long shed = results.stream().filter(r -> "SHED_LOAD_SATURATED".equals(r.status())).count();

            assertEquals(2, admitted);
            assertEquals(1, shed);
            assertTrue(results.stream().allMatch(r -> r.workerThreadName().contains("pool-")));
        }
    }

    @Test
    @DisplayName("ThreadPool Bulkhead: Isolates work onto bulkheads threads (2 active + 1 queued) and sheds 4th burst without starving Inventory")
    void testThreadPoolBulkheadIsolationAndLoadShedding() throws Exception {
        for (int i = 0; i < 3; i++) {
            analyticsServer.enqueue(new MockResponse()
                    .setBodyDelay(400, TimeUnit.MILLISECONDS)
                    .setBody("REPORT_READY"));
        }

        // Launch 4 concurrent analytics reports (2 threads + 1 queue = 3 admitted, 1 shed)
        List<CompletableFuture<WorkloadExecutionResponse>> analyticsFutures = IntStream.range(0, 4)
                .mapToObj(i -> compartmentService.executeAnalyticsWithThreadPool("RPT-" + i))
                .toList();

        // While analytics pool is completely saturated, inventory bulkhead stays 100% responsive
        inventoryServer.enqueue(new MockResponse().setBody("FAST_RESERVE_OK"));
        WorkloadExecutionResponse inventoryResult = compartmentService.executeInventoryWithSemaphore("SKU-FAST");
        assertEquals("ADMITTED", inventoryResult.status());
        assertEquals("FAST_RESERVE_OK", inventoryResult.payload());

        List<WorkloadExecutionResponse> analyticsResults = analyticsFutures.stream()
                .map(CompletableFuture::join)
                .toList();

        long admitted = analyticsResults.stream().filter(r -> "ADMITTED".equals(r.status())).count();
        long shed = analyticsResults.stream().filter(r -> "SHED_LOAD_SATURATED".equals(r.status())).count();

        assertEquals(3, admitted);
        assertEquals(1, shed);
        assertTrue(analyticsResults.stream()
                .filter(r -> "ADMITTED".equals(r.status()))
                .allMatch(r -> r.workerThreadName().contains("analyticsPoolBulkhead")));
    }
}