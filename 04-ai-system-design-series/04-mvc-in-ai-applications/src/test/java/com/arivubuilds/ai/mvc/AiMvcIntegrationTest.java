package com.arivubuilds.ai.mvc;

import com.arivubuilds.ai.mvc.memory.ConversationMemoryStore;
import com.arivubuilds.ai.mvc.model.ChatTurnRequest;
import com.arivubuilds.ai.mvc.model.ChatTurnResponse;
import com.arivubuilds.ai.mvc.port.SimulatedModelGatewayAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiMvcIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConversationMemoryStore memoryStore;

    @Autowired
    private SimulatedModelGatewayAdapter modelAdapter;

    @BeforeEach
    void setUp() {
        memoryStore.clearAll();
    }

    @Test
    void shouldRenderVersionedPromptTemplateAndMaintainSlidingWindowTokenBudget() throws Exception {
        for (int i = 1; i <= 6; i++) {
            ChatTurnRequest req = new ChatTurnRequest(
                    "sess-underwriting-01",
                    "tenant-zinnia",
                    "v2",
                    "Turn " + i + ": Verify statutory free-look rider clauses and beneficiary rules for term policy."
            );

            MvcResult res = mockMvc.perform(post("/api/v1/ai-mvc/chat/turn")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andReturn();

            ChatTurnResponse turnResponse = objectMapper.readValue(
                    res.getResponse().getContentAsString(), ChatTurnResponse.class);

            assertThat(turnResponse.resolvedPromptVersion()).isEqualTo("v2");
            assertThat(turnResponse.retainedHistoryTokens()).isLessThanOrEqualTo(240);
            assertThat(turnResponse.assistantReply()).contains("[Advisor-v2]");
        }

        assertThat(modelAdapter.getLastRenderedPrompt()).contains("[SYSTEM v2 - STRICT GROUNDING]");
        assertThat(modelAdapter.getLastRenderedPrompt()).contains("tenant-zinnia");
        assertThat(memoryStore.loadWindow("sess-underwriting-01").size()).isLessThan(12);
    }

    @Test
    void shouldSupportExplicitPromptVersionSelectionAndRejectUnknownVersions() throws Exception {
        ChatTurnRequest v1Req = new ChatTurnRequest(
                "sess-v1",
                "tenant-zinnia",
                "v1",
                "Summarize policy grace period."
        );

        MvcResult okRes = mockMvc.perform(post("/api/v1/ai-mvc/chat/turn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v1Req)))
                .andExpect(status().isOk())
                .andReturn();

        ChatTurnResponse body = objectMapper.readValue(
                okRes.getResponse().getContentAsString(), ChatTurnResponse.class);
        assertThat(body.resolvedPromptVersion()).isEqualTo("v1");
        assertThat(body.assistantReply()).contains("[Advisor-v1]");

        ChatTurnRequest invalidReq = new ChatTurnRequest(
                "sess-v99",
                "tenant-zinnia",
                "v99",
                "Summarize policy grace period."
        );
        mockMvc.perform(post("/api/v1/ai-mvc/chat/turn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldStreamIncrementalTokensOverSseAndPersistCompletedTurn() throws Exception {
        ChatTurnRequest req = new ChatTurnRequest(
                "sess-sse-01",
                "tenant-zinnia",
                null,
                "Explain accelerated death benefit eligibility."
        );

        MvcResult asyncResult = mockMvc.perform(post("/api/v1/ai-mvc/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult completed = mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andReturn();

        String ssePayload = completed.getResponse().getContentAsString();
        assertThat(ssePayload).contains("event:delta");
        assertThat(ssePayload).contains("event:done");
        assertThat(ssePayload).contains("[DONE]");
        assertThat(memoryStore.loadWindow("sess-sse-01")).hasSize(2);
    }
}
