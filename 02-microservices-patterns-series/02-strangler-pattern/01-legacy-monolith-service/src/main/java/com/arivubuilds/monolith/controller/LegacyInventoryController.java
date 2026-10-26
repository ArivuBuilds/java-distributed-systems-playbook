package com.arivubuilds.monolith.controller;

import com.arivubuilds.monolith.domain.LegacyInventory;
import com.arivubuilds.monolith.repository.LegacyInventoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class LegacyInventoryController {

    private final LegacyInventoryRepository inventoryRepository;

    public LegacyInventoryController(LegacyInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<LegacyInventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryRepository.findAll());
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<LegacyInventory> getByProductCode(@PathVariable String productCode) {
        return inventoryRepository.findByProductCode(productCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}