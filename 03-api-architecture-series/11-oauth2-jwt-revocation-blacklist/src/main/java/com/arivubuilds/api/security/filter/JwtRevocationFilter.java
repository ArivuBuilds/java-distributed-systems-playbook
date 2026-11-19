
package com.arivubuilds.api.security.filter;

import com.arivubuilds.api.security.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRevocationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenService;

    public JwtRevocationFilter(JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            if (tokenService.isRevoked(jwt.getId())) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setHeader("WWW-Authenticate",
                        "Bearer error=\"invalid_token\", error_description=\"Token has been revoked\"");
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                response.getWriter().write("""
                        {
                          "type": "https://api.arivubuilds.com/errors/token-revoked",
                          "title": "Token Revoked",
                          "status": 401,
                          "detail": "The presented JWT (jti=%s) has been explicitly revoked."
                        }
                        """.formatted(jwt.getId()));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
