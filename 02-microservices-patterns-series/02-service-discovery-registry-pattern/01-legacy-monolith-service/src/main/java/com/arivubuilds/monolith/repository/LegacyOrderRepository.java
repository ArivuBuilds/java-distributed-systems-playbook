package com.arivubuilds.monolith.repository;

import com.arivubuilds.monolith.domain.LegacyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LegacyOrderRepository extends JpaRepository<LegacyOrder, Long> {
    Optional<LegacyOrder> findByOrderNumber(String orderNumber);
}