package com.arivubuilds.api.pagination.repository;

import com.arivubuilds.api.pagination.entity.LedgerEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntry, String> {

    // Initial page query when no cursor is provided
    @Query("SELECT e FROM LedgerEntry e ORDER BY e.occurredAt DESC, e.id DESC")
    List<LedgerEntry> findFirstPage(Pageable pageable);

    // Keyset Seek with deterministic primary key tie-breaker
    @Query("""
        SELECT e FROM LedgerEntry e 
        WHERE (e.occurredAt < :occurredAt) 
           OR (e.occurredAt = :occurredAt AND e.id < :id)
        ORDER BY e.occurredAt DESC, e.id DESC
    """)
    List<LedgerEntry> seekNextPage(
            @Param("occurredAt") Instant occurredAt,
            @Param("id") String id,
            Pageable pageable
    );
}