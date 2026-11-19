package com.arivubuilds.api.security;

import com.jayway.jsonpath.JsonPath;
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
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtRevocationBlacklistTest {

    @TestConfiguration
    static class InMemoryRedisBlacklistConfig {

        @Bean
        @Primary
        @SuppressWarnings("unchecked")
        public StringRedisTemplate inMemoryStringRedisTemplate() {
            Map<String, String> blacklist = new ConcurrentHashMap<>();

            StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
            ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

            when(template.opsForValue()).thenReturn(valueOps);

            doAnswer(inv -> {
                blacklist.put(inv.getArgument(0), inv.getArgument(1));
                return null;
            }).when(valueOps).set(anyString(), anyString(), any(Duration.class));

            when(template.hasKey(anyString()))
                    .thenAnswer(inv -> blacklist.containsKey((String) inv.getArgument(0)));

            return template;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Full Lifecycle: Active JWT accesses vault, then is immediately rejected with 401 after revocation")
    void testJwtIssuanceAccessAndInstantRevocation() throws Exception {
        String tokenRequest = """
                {"subject":"arivumani","scopes":["vault:read","vault:write"],"ttlSeconds":600}
                """;

        String tokenJson = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.jti").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String jwt = JsonPath.read(tokenJson, "$.accessToken");
        String jti = JsonPath.read(tokenJson, "$.jti");

        mockMvc.perform(get("/api/v1/vault/secrets")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value("arivumani"))
                .andExpect(jsonPath("$.jti").value(jti))
                .andExpect(jsonPath("$.vaultStatus").value("UNLOCKED"));

        mockMvc.perform(post("/api/v1/auth/revoke")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"))
                .andExpect(jsonPath("$.jti").value(jti));

        mockMvc.perform(get("/api/v1/vault/secrets")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("invalid_token")))
                .andExpect(jsonPath("$.title").value("Token Revoked"));
    }

    @Test
    @DisplayName("Unauthenticated Request: Rejects access without Bearer token with HTTP 401")
    void testMissingBearerTokenRejected() throws Exception {
        mockMvc.perform(get("/api/v1/vault/secrets"))
                .andExpect(status().isUnauthorized());
    }
}