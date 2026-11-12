package com.arivubuilds.api.sse;

import com.arivubuilds.api.sse.model.ServerMetricPulse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ServerSentEventsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("SSE Stream: Emits text/event-stream with event metadata and typed payload")
    void testMetricsStreamWithEventMetadata() {
        ParameterizedTypeReference<ServerSentEvent<ServerMetricPulse>> typeRef =
                new ParameterizedTypeReference<>() {};

        Flux<ServerSentEvent<ServerMetricPulse>> eventStream = webTestClient.get()
                .uri("/api/v1/metrics/stream?limit=3")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(typeRef)
                .getResponseBody();

        StepVerifier.create(eventStream)
                .assertNext(event -> {
                    assertEquals("1", event.id());
                    assertEquals("metric-pulse", event.event());
                    assertNotNull(event.data());
                    assertEquals(1L, event.data().sequenceId());
                })
                .assertNext(event -> {
                    assertEquals("2", event.id());
                    assertEquals("metric-pulse", event.event());
                    assertEquals(2L, event.data().sequenceId());
                })
                .assertNext(event -> {
                    assertEquals("3", event.id());
                    assertEquals("metric-pulse", event.event());
                    assertEquals(3L, event.data().sequenceId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("SSE Stream Reconnection: Honors Last-Event-ID header and resumes stream sequence")
    void testReconnectionWithLastEventId() {
        ParameterizedTypeReference<ServerSentEvent<ServerMetricPulse>> typeRef =
                new ParameterizedTypeReference<>() {};

        Flux<ServerSentEvent<ServerMetricPulse>> resumedStream = webTestClient.get()
                .uri("/api/v1/metrics/stream?limit=2")
                .header("Last-Event-ID", "100")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(typeRef)
                .getResponseBody();

        StepVerifier.create(resumedStream)
                .assertNext(event -> {
                    assertEquals("101", event.id());
                    assertEquals(101L, event.data().sequenceId());
                })
                .assertNext(event -> {
                    assertEquals("102", event.id());
                    assertEquals(102L, event.data().sequenceId());
                })
                .verifyComplete();
    }
}