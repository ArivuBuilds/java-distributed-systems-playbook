package com.arivubuilds.api.grpc;

import com.arivubuilds.api.grpc.service.TelemetryServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TelemetryServiceGrpcTest {

    private Server inProcessServer;
    private ManagedChannel inProcessChannel;
    private TelemetryServiceGrpc.TelemetryServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();

        inProcessServer = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(new TelemetryServiceImpl())
                .build()
                .start();

        inProcessChannel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();

        blockingStub = TelemetryServiceGrpc.newBlockingStub(inProcessChannel);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (inProcessChannel != null) {
            inProcessChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
        if (inProcessServer != null) {
            inProcessServer.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Unary RPC: CollectMetric records payload and returns SUCCESS")
    void testCollectMetricUnary() {
        MetricPayload request = MetricPayload.newBuilder()
                .setDeviceId("node-alpha")
                .setCpuLoadPercentage(55.2)
                .setMemoryUsedMb(2048.0)
                .build();

        MetricAck ack = blockingStub.collectMetric(request);

        assertNotNull(ack);
        assertEquals("SUCCESS", ack.getStatus());
    }

    @Test
    @DisplayName("Server Streaming RPC: StreamLiveMetrics streams requested count of metrics over single channel")
    void testStreamLiveMetrics() {
        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .setEnvironment("staging")
                .setMaxEvents(3)
                .build();

        Iterator<MetricPayload> stream = blockingStub.streamLiveMetrics(filter);
        List<MetricPayload> received = new ArrayList<>();
        stream.forEachRemaining(received::add);

        assertEquals(3, received.size());
        assertEquals("staging", received.get(0).getTagsOrDefault("env", ""));
    }
}