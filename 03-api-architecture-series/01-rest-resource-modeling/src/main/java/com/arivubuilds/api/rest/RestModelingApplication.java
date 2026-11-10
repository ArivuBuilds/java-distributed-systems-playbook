package com.arivubuilds.api.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.arivubuilds.api.rest")
public class RestModelingApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestModelingApplication.class, args);
    }
}