package com.arivubuilds.api.pagination.controller;

import com.arivubuilds.api.pagination.entity.LedgerEntry;
import com.arivubuilds.api.pagination.model.CursorPageResponse;
import com.arivubuilds.api.pagination.model.CursorToken;
import com.arivubuilds.api.pagination.model.LedgerDto;
import com.arivubuilds.api.pagination.repository.LedgerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerCursorController {

    private final LedgerRepository ledgerRepository;

    public LedgerCursorController(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<LedgerDto>> getLedgerEntries(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {

        int clampedLimit = Math.clamp(limit, 1, 100);
        // Over-fetch by 1 to detect hasNextPage in a single round-trip without COUNT(*)
        Pageable fetchPage = PageRequest.of(0, clampedLimit + 1);

        CursorToken token = CursorToken.decode(cursor);
        List<LedgerEntry> entries = (token == null)
                ? ledgerRepository.findFirstPage(fetchPage)
                : ledgerRepository.seekNextPage(token.occurredAt(), token.id(), fetchPage);

        boolean hasNext = entries.size() > clampedLimit;
        List<LedgerEntry> pageContent = hasNext ? entries.subList(0, clampedLimit) : entries;

        String nextCursor = null;
        if (hasNext && !pageContent.isEmpty()) {
            LedgerEntry lastEntry = pageContent.getLast();
            nextCursor = CursorToken.encode(lastEntry.getOccurredAt(), lastEntry.getId());
        }

        List<LedgerDto> dtoList = pageContent.stream()
                .map(e -> new LedgerDto(e.getId(), e.getAccountId(), e.getAmount(), e.getOccurredAt()))
                .toList();

        return ResponseEntity.ok(new CursorPageResponse<>(
                dtoList,
                new CursorPageResponse.PageMetadata(dtoList.size(), hasNext, nextCursor)
        ));
    }
}