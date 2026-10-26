package com.arivubuilds.monolith.repository;

import com.arivubuilds.monolith.domain.LegacyInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LegacyInventoryRepository extends JpaRepository<LegacyInventory, Long> {
    Optional<LegacyInventory> findByProductCode(String productCode);
}