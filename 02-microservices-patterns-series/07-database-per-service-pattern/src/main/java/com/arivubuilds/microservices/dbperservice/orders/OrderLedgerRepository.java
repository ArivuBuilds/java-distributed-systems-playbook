package com.arivubuilds.microservices.dbperservice.orders;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderLedgerRepository extends JpaRepository<OrderLedgerEntity, String> {
    List<OrderLedgerEntity> findByProductId(String productId);
}