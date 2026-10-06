package com.arecaharvest.controller;

import com.arecaharvest.dto.request.HarvestSessionRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.service.HarvestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Harvest Sessions", description = "Daily harvest session recording")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/harvest-sessions")
@RequiredArgsConstructor
public class HarvestController {

    private final HarvestService harvestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<HarvestSessionResponse>>> search(
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                harvestService.searchSessions(fieldId, contractId, status, fromDate, toDate, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<HarvestSessionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(harvestService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<HarvestSessionResponse>> create(
            @Valid @RequestBody HarvestSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(harvestService.createSession(request), "Session created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<HarvestSessionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody HarvestSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(harvestService.updateSession(id, request)));
    }
}
