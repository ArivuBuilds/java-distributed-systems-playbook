package com.arivubuilds.api.idempotency;

import com.arivubuilds.api.idempotency.service.IdempotencyStoreService;
import com.arivubuilds.api.idempotency.service.LedgerTransferService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IdempotencyDeduplicationTest {

    @TestConfiguration
    static class InMemoryRedisTestConfig {

        @Bean
        @Primary
        @SuppressWarnings("unchecked")
        public StringRedisTemplate inMemoryStringRedisTemplate() {
            Map<String, String> store = new ConcurrentHashMap<>();

            StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
            ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

            when(template.opsForValue()).thenReturn(valueOps);

            when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                    .thenAnswer(inv -> store.putIfAbsent(inv.getArgument(0), inv.getArgument(1)) == null);

            when(valueOps.get(anyString()))
                    .thenAnswer(inv -> store.get((String) inv.getArgument(0)));

            doAnswer(inv -> {
                store.put(inv.getArgument(0), inv.getArgument(1));
                return null;
            }).when(valueOps).set(anyString(), anyString(), any(Duration.class));

            when(template.delete(anyString()))
                    .thenAnswer(inv -> store.remove((String) inv.getArgument(0)) != null);

            return template;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LedgerTransferService transferService;

    @Autowired
    private IdempotencyStoreService storeService;

    @BeforeEach
    void resetState() {
        transferService.resetCounter();
    }

    @Test
    @DisplayName("Missing Header: Rejects mutation without Idempotency-Key with HTTP 400")
    void testMissingIdempotencyKeyRejected() throws Exception {
        String payload = """
                {"sourceAccount":"acc_01","targetAccount":"acc_02","amount":1500.00,"currency":"INR"}
                """;

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Missing Idempotency-Key"));

        assertEquals(0, transferService.getExecutionCount());
    }

    @Test
    @DisplayName("Duplicate Retry: Replays cached HTTP 201 response without re-executing transfer logic")
    void testDuplicateRetryReplaysCachedResponse() throws Exception {
        String idempotencyKey = "idem_" + UUID.randomUUID();
        String payload = """
                {"sourceAccount":"acc_01","targetAccount":"acc_02","amount":2500.00,"currency":"INR"}
                """;

        // 1. Initial Request -> Executes business logic
        String firstResponse = mockMvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("X-Idempotency-Replay"))
                .andExpect(jsonPath("$.status").value("SETTLED"))
                .andReturn().getResponse().getContentAsString();

        String originalTxnId = JsonPath.read(firstResponse, "$.transactionId");
        assertEquals(1, transferService.getExecutionCount());

        // 2. Retry with identical key & payload -> Returns cached response
        String secondResponse = mockMvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Idempotency-Replay", "true"))
                .andExpect(jsonPath("$.transactionId").value(originalTxnId))
                .andReturn().getResponse().getContentAsString();

        assertEquals(firstResponse, secondResponse);
        assertEquals(1, transferService.getExecutionCount());
    }

    @Test
    @DisplayName("Payload Mismatch: Rejects reused Idempotency-Key with altered body using HTTP 422")
    void testPayloadMismatchReturns422() throws Exception {
        String idempotencyKey = "idem_" + UUID.randomUUID();
        String originalPayload = """
                {"sourceAccount":"acc_01","targetAccount":"acc_02","amount":1000.00,"currency":"INR"}
                """;
        String alteredPayload = """
                {"sourceAccount":"acc_01","targetAccount":"acc_02","amount":9999.00,"currency":"INR"}
                """;

        mockMvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteredPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Idempotency Payload Mismatch"));

        assertEquals(1, transferService.getExecutionCount());
    }

    @Test
    @DisplayName("Concurrent In-Flight Lock: Rejects duplicate request with HTTP 409 while IN_PROGRESS")
    void testConcurrentInFlightReturns409() throws Exception {
        String idempotencyKey = "idem_" + UUID.randomUUID();
        String payload = """
                {"sourceAccount":"acc_01","targetAccount":"acc_02","amount":500.00,"currency":"INR"}
                """;
        String fingerprint = storeService.computeFingerprint("POST", "/api/v1/transfers", payload.getBytes());

        // Simulate an active in-flight lock held by another thread/node
        storeService.tryLock(idempotencyKey, fingerprint);

        mockMvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Concurrent Request In-Flight"));

        assertEquals(0, transferService.getExecutionCount());
    }
}