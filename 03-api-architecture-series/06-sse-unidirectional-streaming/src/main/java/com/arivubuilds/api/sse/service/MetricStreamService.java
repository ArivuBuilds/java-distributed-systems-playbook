package com.arivubuilds.api.sse.service;

import com.arivubuilds.api.sse.model.ServerMetricPulse;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MetricStreamService {

    /**
     * Produces a continuous reactive stream of ServerSentEvents with explicit backpressure buffering.
     * Emits sequence ID for Last-Event-ID catch-up and 3000ms client reconnect retry.
     */
    public Flux<ServerSentEvent<ServerMetricPulse>> streamMetrics(Long lastEventId, int limit) {
        long startSequence = (lastEventId != null) ? lastEventId + 1 : 1;

        Flux<ServerSentEvent<ServerMetricPulse>> stream = Flux.interval(Duration.ofMillis(150))
                .onBackpressureBuffer(128)
                .map(i -> {
                    long currentSeq = startSequence + i;
                    ServerMetricPulse pulse = new ServerMetricPulse(
                            currentSeq,
                            "node-cluster-a",
                            Math.round(ThreadLocalRandom.current().nextDouble(15.0, 85.0) * 100.0) / 100.0,
                            ThreadLocalRandom.current().nextLong(4096, 16384),
                            Instant.now()
                    );

                    return ServerSentEvent.<ServerMetricPulse>builder()
                            .id(String.valueOf(currentSeq))
                            .event("metric-pulse")
                            .retry(Duration.ofMillis(3000))
                            .data(pulse)
                            .build();
                });

        return (limit > 0) ? stream.take(limit) : stream;
    }
}