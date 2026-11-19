package com.arivubuilds.api.security.controller;

import com.arivubuilds.api.security.model.TokenRequest;
import com.arivubuilds.api.security.model.TokenResponse;
import com.arivubuilds.api.security.service.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthAndVaultController {

    private final JwtTokenService tokenService;

    public AuthAndVaultController(JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/auth/token")
    public ResponseEntity<TokenResponse> issueToken(@RequestBody TokenRequest request) {
        return ResponseEntity.ok(tokenService.mintToken(request));
    }

    @PostMapping("/auth/revoke")
    public ResponseEntity<Map<String, String>> revokeCurrentToken(@AuthenticationPrincipal Jwt jwt) {
        tokenService.revokeToken(jwt);
        return ResponseEntity.ok(Map.of(
                "status", "REVOKED",
                "jti", jwt.getId()
        ));
    }

    @GetMapping("/vault/secrets")
    public ResponseEntity<Map<String, Object>> getProtectedSecrets(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
                "principal", jwt.getSubject(),
                "jti", jwt.getId(),
                "vaultStatus", "UNLOCKED",
                "secretPayload", "PROD_DB_ROTATION_KEY_2026"
        ));
    }
}
