package com.arivubuilds.api.versioning;

import com.arivubuilds.api.versioning.controller.PolicyVersioningController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiVersioningNegotiationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("URI Versioning: v1 returns legacy schema with Deprecation & Sunset headers; v2 returns evolved schema")
    void testUriPathVersioningAndDeprecationHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/policies/POL-9001"))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().exists("Sunset"))
                .andExpect(header().string("Link", containsString("rel=\"successor-version\"")))
                .andExpect(jsonPath("$.holderName").value("Arivumani A"))
                .andExpect(jsonPath("$.premiumAmount").value(24500.00));

        mockMvc.perform(get("/api/v2/policies/POL-9001"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Deprecation"))
                .andExpect(jsonPath("$.holder.fullName").value("Arivumani A"))
                .andExpect(jsonPath("$.annualPremium.currency").value("INR"))
                .andExpect(jsonPath("$.lifecycleState").value("IN_FORCE"));
    }

    @Test
    @DisplayName("Custom Header Versioning: Routes via X-API-Version header on canonical URI")
    void testCustomHeaderVersioning() throws Exception {
        mockMvc.perform(get("/api/policies/POL-9002")
                        .header("X-API-Version", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/policies/POL-9002")
                        .header("X-API-Version", "2"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Deprecation"))
                .andExpect(jsonPath("$.holder.tier").value("PLATINUM"));
    }

   @Test
    @DisplayName("Content Negotiation: Routes via vendor MIME Accept header and rejects unsupported MIME with 406")
    void testVendorMediaTypeContentNegotiation() throws Exception {
        mockMvc.perform(get("/api/negotiated/policies/POL-9003")
                        .accept(PolicyVersioningController.V2_VENDOR_MEDIA_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(PolicyVersioningController.V2_VENDOR_MEDIA_TYPE))
                .andExpect(jsonPath("$.annualPremium.amount").value(24500.00));

        mockMvc.perform(get("/api/negotiated/policies/POL-9003")
                        .accept("application/vnd.arivubuilds.policy.v99+json"))
                .andExpect(status().isNotAcceptable());
    }
}