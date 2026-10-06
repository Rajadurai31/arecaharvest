package com.arecaharvest.controller;

import com.arecaharvest.dto.request.ExpenseRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.entity.ExpenseCategory;
import com.arecaharvest.repository.ExpenseCategoryRepository;
import com.arecaharvest.service.ExpenseService;
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
import java.util.List;

@Tag(name = "Expenses", description = "Business expense tracking")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseCategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expenseDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.searchExpenses(categoryId, fieldId, fromDate, toDate, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(expenseService.create(request), "Expense recorded"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Expense deleted"));
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ExpenseCategory>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findByActiveTrueOrderByName()));
    }
}
