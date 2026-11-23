package com.arivubuilds.microservices.retry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RetryBackoffApplication {
    public static void main(String[] args) {
        SpringApplication.run(RetryBackoffApplication.class, args);
    }
}