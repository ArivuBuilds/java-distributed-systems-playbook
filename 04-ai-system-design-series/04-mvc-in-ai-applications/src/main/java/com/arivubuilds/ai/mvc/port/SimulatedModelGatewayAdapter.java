package com.arivubuilds.ai.mvc.port;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class SimulatedModelGatewayAdapter implements ModelGatewayPort {

    private final AtomicReference<String> lastRenderedPrompt = new AtomicReference<>("");

    @Override
    public String generateCompletion(String renderedPrompt) {
        lastRenderedPrompt.set(renderedPrompt);
        String versionTag = renderedPrompt.contains("[SYSTEM v1]") ? "v1" : "v2";
        return "[Advisor-" + versionTag + "] Statutory free-look review completed with grounded policy citations.";
    }

    public String getLastRenderedPrompt() {
        return lastRenderedPrompt.get();
    }
}
