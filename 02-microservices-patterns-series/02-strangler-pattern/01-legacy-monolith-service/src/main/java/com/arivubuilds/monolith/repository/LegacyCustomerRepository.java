package com.arivubuilds.monolith.repository;

import com.arivubuilds.monolith.domain.LegacyCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LegacyCustomerRepository extends JpaRepository<LegacyCustomer, Long> {
    Optional<LegacyCustomer> findByEmail(String email);
}