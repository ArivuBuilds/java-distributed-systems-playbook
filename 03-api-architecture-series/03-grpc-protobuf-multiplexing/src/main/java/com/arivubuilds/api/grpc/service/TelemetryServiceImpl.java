package com.arivubuilds.api.grpc.service;

import com.arivubuilds.api.grpc.MetricAck;
import com.arivubuilds.api.grpc.MetricPayload;
import com.arivubuilds.api.grpc.SubscriptionFilter;
import com.arivubuilds.api.grpc.TelemetryServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class TelemetryServiceImpl extends TelemetryServiceGrpc.TelemetryServiceImplBase {

    @Override
    public void collectMetric(MetricPayload request, StreamObserver<MetricAck> responseObserver) {
        if (request.getDeviceId() == null || request.getDeviceId().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("device_id must not be blank")
                    .asRuntimeException());
            return;
        }

        MetricAck ack = MetricAck.newBuilder()
                .setStatus("SUCCESS")
                .setRecordedAt(System.currentTimeMillis())
                .build();

        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }

    @Override
    public void streamLiveMetrics(SubscriptionFilter request, StreamObserver<MetricPayload> responseObserver) {
        int maxEvents = request.getMaxEvents() > 0 ? request.getMaxEvents() : 3;
        String env = (request.getEnvironment() == null || request.getEnvironment().isBlank())
                ? "prod"
                : request.getEnvironment();

        for (int i = 1; i <= maxEvents; i++) {
            MetricPayload payload = MetricPayload.newBuilder()
                    .setDeviceId("edge-node-" + i)
                    .setTimestampEpochMs(System.currentTimeMillis())
                    .setCpuLoadPercentage(42.5 + i)
                    .setMemoryUsedMb(1024.0 * i)
                    .putTags("env", env)
                    .build();

            responseObserver.onNext(payload);
        }
        responseObserver.onCompleted();
    }
}