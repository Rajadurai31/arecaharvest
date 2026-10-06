package com.arecaharvest.controller;

import com.arecaharvest.dto.request.BuyerPaymentRequest;
import com.arecaharvest.dto.request.SaleRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Sales", description = "Areca nut sales to buyers")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<SaleResponse>>> search(
            @RequestParam(required = false) Long buyerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("saleDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                saleService.searchSales(buyerId, status, fromDate, toDate, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<SaleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create sale — validates inventory, updates stock, creates buyer ledger entry")
    public ResponseEntity<ApiResponse<SaleResponse>> create(@Valid @RequestBody SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saleService.createSale(request), "Sale recorded"));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record payment from buyer")
    public ResponseEntity<ApiResponse<SaleResponse>> recordPayment(
            @Valid @RequestBody BuyerPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saleService.recordBuyerPayment(request), "Payment recorded"));
    }
}
