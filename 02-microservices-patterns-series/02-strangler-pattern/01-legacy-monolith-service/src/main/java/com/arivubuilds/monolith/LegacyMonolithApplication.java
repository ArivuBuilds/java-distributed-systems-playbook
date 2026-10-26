package com.arivubuilds.monolith;

import com.arivubuilds.monolith.domain.LegacyCustomer;
import com.arivubuilds.monolith.domain.LegacyInventory;
import com.arivubuilds.monolith.repository.LegacyCustomerRepository;
import com.arivubuilds.monolith.repository.LegacyInventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class LegacyMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyMonolithApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(LegacyCustomerRepository customerRepo, LegacyInventoryRepository inventoryRepo) {
        return args -> {
            customerRepo.save(LegacyCustomer.builder()
                    .fullName("Enterprise Client A")
                    .email("client.a@enterprise.internal")
                    .tier("PLATINUM")
                    .build());

            inventoryRepo.save(LegacyInventory.builder()
                    .sku("SKU-CORP-SERVER")
                    .productName("Rackmount Server Node 1U")
                    .availableStock(50)
                    .unitPrice(new BigDecimal("2499.00"))
                    .build());

            inventoryRepo.save(LegacyInventory.builder()
                    .sku("SKU-CORP-SWITCH")
                    .productName("48-Port Managed Switch")
                    .availableStock(100)
                    .unitPrice(new BigDecimal("799.00"))
                    .build());
        };
    }
}