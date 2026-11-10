package com.arivubuilds.api.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcMultiplexApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrpcMultiplexApplication.class, args);
    }
}