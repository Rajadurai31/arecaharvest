package com.arecaharvest.controller;

import com.arecaharvest.dto.request.ContractRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.entity.enums.ContractStatus;
import com.arecaharvest.service.ContractService;
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

@Tag(name = "Contracts", description = "Harvesting contract management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<ContractResponse>>> search(
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                contractService.searchContracts(fieldId, ownerId, status, fromDate, toDate, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<ContractResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> create(@Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(contractService.create(request), "Contract created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contractService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractResponse>> updateStatus(
            @PathVariable Long id, @RequestParam ContractStatus status) {
        return ResponseEntity.ok(ApiResponse.success(contractService.updateStatus(id, status)));
    }
}
