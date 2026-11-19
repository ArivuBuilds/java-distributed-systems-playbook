package com.arivubuilds.microservices.bulkhead;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BulkheadIsolationApplication {
    public static void main(String[] args) {
        SpringApplication.run(BulkheadIsolationApplication.class, args);
    }
}