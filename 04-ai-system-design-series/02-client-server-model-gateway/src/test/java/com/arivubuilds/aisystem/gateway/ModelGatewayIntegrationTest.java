package com.arivubuilds.aisystem.gateway;

import com.arivubuilds.aisystem.gateway.model.GatewayContracts.CompletionRequest;
import com.arivubuilds.aisystem.gateway.model.GatewayContracts.CompletionResponse;
import com.arivubuilds.aisystem.gateway.model.GatewayContracts.RoutingStrategy;
import com.arivubuilds.aisystem.gateway.service.TenantTokenBudgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ModelGatewayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantTokenBudgetService budgetService;

    @BeforeEach
    void resetBudgets() {
        budgetService.resetTenant("tenant-enterprise", 500);
        budgetService.resetTenant("tenant-capped", 5);
    }

    @Test
    void shouldRouteDynamicallyByStrategyAndDeductTokens() throws Exception {
        CompletionRequest reasoningReq = new CompletionRequest(
                "tenant-enterprise",
                "Design multi-region active-active failover for policy claims",
                RoutingStrategy.HIGH_REASONING,
                null,
                128
        );

        MvcResult res = mockMvc.perform(post("/api/v1/gateway/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reasoningReq)))
                .andExpect(status().isOk())
                .andReturn();

        CompletionResponse body = objectMapper.readValue(res.getResponse().getContentAsString(), CompletionResponse.class);
        assertEquals("frontier-reasoning", body.providerId());
        assertEquals("claude-sonnet-4-6", body.modelName());
        assertTrue(body.usage().totalTokens() > 0);
        assertEquals(500 - body.usage().totalTokens(), body.usage().remainingTenantBudget());
    }

    @Test
    void shouldRejectRequestWith429WhenTenantTokenBudgetExhausted() throws Exception {
        CompletionRequest oversizedReq = new CompletionRequest(
                "tenant-capped",
                "Generate comprehensive underwriting audit across fifty policy schedules",
                RoutingStrategy.COST_OPTIMIZED,
                null,
                128
        );

        mockMvc.perform(post("/api/v1/gateway/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oversizedReq)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldStreamUnifiedSseTokenEvents() throws Exception {
        CompletionRequest streamReq = new CompletionRequest(
                "tenant-enterprise",
                "Summarize SLA latency targets",
                RoutingStrategy.LOW_LATENCY,
                null,
                64
        );

        MvcResult asyncResult = mockMvc.perform(post("/api/v1/gateway/completions/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(streamReq)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("event:token")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("event:done")));
    }
}