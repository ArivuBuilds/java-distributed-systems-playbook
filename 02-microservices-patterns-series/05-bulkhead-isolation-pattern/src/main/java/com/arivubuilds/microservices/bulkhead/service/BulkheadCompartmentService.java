package com.arivubuilds.microservices.bulkhead.service;

import com.arivubuilds.microservices.bulkhead.client.DownstreamCompartmentClient;
import com.arivubuilds.microservices.bulkhead.dto.WorkloadExecutionResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class BulkheadCompartmentService {

    public static final String SEMAPHORE_BULKHEAD = "inventorySemaphoreBulkhead";
    public static final String THREADPOOL_BULKHEAD = "analyticsPoolBulkhead";

    private final DownstreamCompartmentClient client;

    public BulkheadCompartmentService(DownstreamCompartmentClient client) {
        this.client = client;
    }

    @Bulkhead(name = SEMAPHORE_BULKHEAD, type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "inventoryShedFallback")
    public WorkloadExecutionResponse executeInventoryWithSemaphore(String sku) {
        String upstreamResult = client.reserveInventory(sku);
        return new WorkloadExecutionResponse(
                "INVENTORY_CHECKOUT",
                "SEMAPHORE",
                "ADMITTED",
                Thread.currentThread().getName(),
                upstreamResult
        );
    }

    public WorkloadExecutionResponse inventoryShedFallback(String sku, Throwable cause) {
        return new WorkloadExecutionResponse(
                "INVENTORY_CHECKOUT",
                "SEMAPHORE",
                "SHED_LOAD_SATURATED",
                Thread.currentThread().getName(),
                "Semaphore permits exhausted for SKU " + sku
        );
    }

    @Bulkhead(name = THREADPOOL_BULKHEAD, type = Bulkhead.Type.THREADPOOL, fallbackMethod = "analyticsShedFallback")
    public CompletableFuture<WorkloadExecutionResponse> executeAnalyticsWithThreadPool(String reportId) {
        String upstreamResult = client.generateHeavyAnalytics(reportId);
        return CompletableFuture.completedFuture(new WorkloadExecutionResponse(
                "HEAVY_ANALYTICS",
                "THREADPOOL",
                "ADMITTED",
                Thread.currentThread().getName(),
                upstreamResult
        ));
    }

    public CompletableFuture<WorkloadExecutionResponse> analyticsShedFallback(String reportId, Throwable cause) {
        return CompletableFuture.completedFuture(new WorkloadExecutionResponse(
                "HEAVY_ANALYTICS",
                "THREADPOOL",
                "SHED_LOAD_SATURATED",
                Thread.currentThread().getName(),
                "Dedicated ThreadPool and queue full for report " + reportId
        ));
    }
}