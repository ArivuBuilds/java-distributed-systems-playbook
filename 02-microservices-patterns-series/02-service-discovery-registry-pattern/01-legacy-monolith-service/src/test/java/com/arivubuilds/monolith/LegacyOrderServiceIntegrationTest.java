package com.arivubuilds.monolith;

import com.arivubuilds.monolith.domain.LegacyCustomer;
import com.arivubuilds.monolith.domain.LegacyInventory;
import com.arivubuilds.monolith.domain.LegacyOrder;
import com.arivubuilds.monolith.repository.LegacyCustomerRepository;
import com.arivubuilds.monolith.repository.LegacyInventoryRepository;
import com.arivubuilds.monolith.service.LegacyOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LegacyOrderServiceIntegrationTest {

    @Autowired
    private LegacyOrderService orderService;

    @Autowired
    private LegacyCustomerRepository customerRepository;

    @Autowired
    private LegacyInventoryRepository inventoryRepository;

    @Test
    void testPlaceOrderSuccessfullyDeductsStock() {
        LegacyCustomer customer = customerRepository.save(
                LegacyCustomer.builder()
                        .name("Arivu Engineering")
                        .email("arivu@distributed.io")
                        .tier("ENTERPRISE")
                        .build()
        );

        LegacyInventory inventory = inventoryRepository.save(
                LegacyInventory.builder()
                        .productCode("SKU-1001")
                        .productName("Distributed Systems Engine")
                        .availableStock(50)
                        .unitPrice(new BigDecimal("199.99"))
                        .build()
        );

        LegacyOrder order = orderService.placeOrder(customer.getId(), "SKU-1001", 5);

        assertNotNull(order.getId());
        assertEquals("COMPLETED", order.getStatus());
        assertEquals(5, order.getQuantity());

        LegacyInventory updated = inventoryRepository.findByProductCode("SKU-1001").orElseThrow();
        assertEquals(45, updated.getAvailableStock());
    }
}