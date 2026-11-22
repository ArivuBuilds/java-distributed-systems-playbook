package com.arivubuilds.monolith.controller;

import com.arivubuilds.monolith.domain.LegacyCustomer;
import com.arivubuilds.monolith.repository.LegacyCustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class LegacyCustomerController {

    private final LegacyCustomerRepository customerRepository;

    public LegacyCustomerController(LegacyCustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public ResponseEntity<List<LegacyCustomer>> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LegacyCustomer> getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LegacyCustomer> createCustomer(@RequestBody LegacyCustomer customer) {
        return ResponseEntity.ok(customerRepository.save(customer));
    }
}