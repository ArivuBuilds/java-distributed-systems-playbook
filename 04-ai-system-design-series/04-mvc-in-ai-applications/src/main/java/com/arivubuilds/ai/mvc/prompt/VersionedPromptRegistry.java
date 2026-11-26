package com.arivubuilds.ai.mvc.prompt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VersionedPromptRegistry {

    private final String defaultVersion;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    public VersionedPromptRegistry(
            @Value("${ai.mvc.default-prompt-version:v2}") String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public String resolveVersion(String requestedVersion) {
        return (requestedVersion == null || requestedVersion.isBlank())
                ? defaultVersion
                : requestedVersion.trim();
    }

    public String render(String requestedVersion, Map<String, String> variables) {
        String version = resolveVersion(requestedVersion);
        String template = templateCache.computeIfAbsent(version, this::loadTemplateFromClasspath);
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return rendered;
    }

    private String loadTemplateFromClasspath(String version) {
        String path = "prompts/underwriting-advisor-" + version + ".st";
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalArgumentException("Unsupported prompt template version: " + version);
        }
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load prompt template: " + path, e);
        }
    }
}
