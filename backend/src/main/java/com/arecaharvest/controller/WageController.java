package com.arecaharvest.controller;

import com.arecaharvest.dto.request.WorkerPaymentRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.entity.WageLedger;
import com.arecaharvest.entity.Worker;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.WageLedgerRepository;
import com.arecaharvest.repository.WorkerRepository;
import com.arecaharvest.service.WageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Tag(name = "Wages", description = "Worker wage ledger and payments")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/wages")
@RequiredArgsConstructor
public class WageController {

    private final WageService wageService;
    private final WageLedgerRepository wageLedgerRepository;
    private final WorkerRepository workerRepository;

    @GetMapping("/workers/{workerId}/ledger")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','WORKER')")
    @Operation(summary = "Get full wage ledger for a worker with balance summary")
    public ResponseEntity<ApiResponse<WorkerLedgerSummaryResponse>> getLedger(
            @PathVariable Long workerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return ResponseEntity.ok(ApiResponse.success(wageService.getWorkerLedger(workerId, pageable)));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record a payment or advance to a worker")
    public ResponseEntity<ApiResponse<WorkerPaymentResponse>> recordPayment(
            @Valid @RequestBody WorkerPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(wageService.recordPayment(request), "Payment recorded"));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<WorkerPaymentResponse>>> getPayments(
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                wageService.getPaymentsForWorker(workerId, fromDate, toDate, pageable)));
    }

    @PostMapping("/adjustment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a manual BONUS, ADJUSTMENT, or BONUS ledger entry for a worker")
    public ResponseEntity<ApiResponse<WageLedgerResponse>> addAdjustment(
            @Valid @RequestBody AdjustmentRequest request) {
        Worker worker = workerRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", request.getWorkerId()));

        WageLedger.TransactionType type;
        try {
            type = WageLedger.TransactionType.valueOf(request.getTransactionType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid transaction type. Allowed: BONUS, ADJUSTMENT");
        }
        if (type == WageLedger.TransactionType.DAILY_WAGE || type == WageLedger.TransactionType.OVERTIME
                || type == WageLedger.TransactionType.PAYMENT || type == WageLedger.TransactionType.ADVANCE) {
            throw new BusinessException("Use BONUS or ADJUSTMENT for manual entries");
        }

        WageLedger entry = WageLedger.builder()
                .worker(worker).transactionType(type)
                .amount(request.getAmount())
                .adjustmentSign(request.getAdjustmentSign() != null ? request.getAdjustmentSign() : 1)
                .transactionDate(request.getTransactionDate() != null
                        ? request.getTransactionDate() : LocalDate.now())
                .description(request.getDescription())
                .build();
        WageLedger saved = wageLedgerRepository.save(entry);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                WageLedgerResponse.builder()
                        .id(saved.getId()).workerId(worker.getId()).workerName(worker.getName())
                        .transactionType(saved.getTransactionType()).amount(saved.getAmount())
                        .adjustmentSign(saved.getAdjustmentSign())
                        .transactionDate(saved.getTransactionDate())
                        .description(saved.getDescription()).createdAt(saved.getCreatedAt())
                        .build(),
                "Adjustment recorded"));
    }

    @Getter @Setter
    public static class AdjustmentRequest {
        @NotNull private Long workerId;
        @NotBlank private String transactionType; // BONUS or ADJUSTMENT
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        private Integer adjustmentSign; // 1 = adds to balance, -1 = reduces
        private LocalDate transactionDate;
        private String description;
    }
}
