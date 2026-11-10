package com.arivubuilds.api.rest;

import com.arivubuilds.api.rest.controller.OrderController;
import com.arivubuilds.api.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/orders returns 201 with Location header and HATEOAS links")
    void testCreateOrder() throws Exception {
        String payload = """
            { "customerId": "CUST-001", "totalAmount": 99.99 }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pay.href").exists())
                .andExpect(jsonPath("$._links.cancel.href").exists());
    }

    @Test
    @DisplayName("Validation failure returns RFC 9457 Problem Details")
    void testValidationFailure() throws Exception {
        String payload = """
            { "customerId": "", "totalAmount": -10.0 }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.arivubuilds.com/errors/validation-error"))
                .andExpect(jsonPath("$.status").value(400));
    }
}