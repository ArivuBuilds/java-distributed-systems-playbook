package com.arivubuilds.ai.mvc.port;

import java.util.List;

public interface ModelGatewayPort {

    String generateCompletion(String renderedPrompt);

    default List<String> streamCompletion(String renderedPrompt) {
        String full = generateCompletion(renderedPrompt);
        return List.of(full.split("(?<=\\s)"));
    }
}
