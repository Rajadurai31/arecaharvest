package com.arecaharvest.controller;

import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.BuyerPayment;
import com.arecaharvest.service.BuyerService;
import com.arecaharvest.service.BuyerService.BuyerSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Buyers", description = "Nut buyer / trader management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<BuyerSummary>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(ApiResponse.success(buyerService.searchBuyers(search, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<BuyerSummary>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(buyerService.getById(id)));
    }

    @GetMapping("/{id}/ledger")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Get buyer payment ledger (all sales + payments + adjustments)")
    public ResponseEntity<ApiResponse<PagedResponse<BuyerPayment>>> getLedger(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        return ResponseEntity.ok(ApiResponse.success(buyerService.getLedger(id, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BuyerSummary>> create(
            @Valid @RequestBody BuyerService.BuyerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(buyerService.create(request), "Buyer created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BuyerSummary>> update(
            @PathVariable Long id, @Valid @RequestBody BuyerService.BuyerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(buyerService.update(id, request)));
    }
}
