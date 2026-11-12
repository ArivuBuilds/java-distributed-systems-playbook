package com.arivubuilds.api.sse.controller;

import com.arivubuilds.api.sse.model.ServerMetricPulse;
import com.arivubuilds.api.sse.service.MetricStreamService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ServerSentEventsController {

    private final MetricStreamService metricStreamService;

    public ServerSentEventsController(MetricStreamService metricStreamService) {
        this.metricStreamService = metricStreamService;
    }

    @GetMapping(value = "/api/v1/metrics/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ServerMetricPulse>> streamServerMetrics(
            @RequestHeader(value = "Last-Event-ID", required = false) Long lastEventId,
            @RequestParam(value = "limit", defaultValue = "0") int limit) {
        return metricStreamService.streamMetrics(lastEventId, limit);
    }
}