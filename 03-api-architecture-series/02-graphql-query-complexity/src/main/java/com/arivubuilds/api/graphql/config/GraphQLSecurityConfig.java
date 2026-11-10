package com.arivubuilds.api.graphql.config;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GraphQLSecurityConfig {

    /**
     * Rejects nested malicious queries (e.g. book -> author -> books -> author -> books)
     * beyond depth 4, and limits query complexity score to 50.
     */
    @Bean
    public Instrumentation graphQLInstrumentation() {
        return new ChainedInstrumentation(List.of(
                new MaxQueryDepthInstrumentation(4),
                new MaxQueryComplexityInstrumentation(50)
        ));
    }
}