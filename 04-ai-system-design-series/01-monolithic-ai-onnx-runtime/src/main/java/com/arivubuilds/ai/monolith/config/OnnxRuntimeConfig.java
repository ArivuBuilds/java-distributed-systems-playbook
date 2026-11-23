package com.arivubuilds.ai.monolith.config;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OnnxRuntimeConfig {

    @Bean
    public OrtEnvironment ortEnvironment() {
        return OrtEnvironment.getEnvironment();
    }

    @Bean
    public OrtSession.SessionOptions ortSessionOptions(
            @Value("${ai.onnx.intra-op-threads:2}") int intraOpThreads,
            @Value("${ai.onnx.inter-op-threads:1}") int interOpThreads) throws OrtException {
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        // Prevent ONNX native C++ thread pool from spawning 1 thread per core and starving Tomcat/Netty
        options.setIntraOpNumThreads(intraOpThreads);
        options.setInterOpNumThreads(interOpThreads);
        options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
        return options;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "ai.onnx.model-path")
    public OrtSession ortSession(
            OrtEnvironment env,
            OrtSession.SessionOptions options,
            @Value("${ai.onnx.model-path}") String modelPath) throws OrtException {
        if (modelPath == null || modelPath.isBlank()) {
            return null;
        }
        return env.createSession(modelPath, options);
    }
}