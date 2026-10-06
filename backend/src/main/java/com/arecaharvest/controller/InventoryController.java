package com.arecaharvest.controller;

import com.arecaharvest.dto.response.*;
import com.arecaharvest.entity.Inventory;
import com.arecaharvest.repository.InventoryRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Inventory", description = "Post-harvest stock management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<Inventory>>> search(
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("receivedDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(inventoryRepository.searchInventory(fieldId, status, pageable))));
    }

    @GetMapping("/available-total")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<BigDecimal>> getAvailableTotal() {
        return ResponseEntity.ok(ApiResponse.success(inventoryRepository.totalAvailableQuantity(),
                "Total available inventory in KG"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<Inventory>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryRepository.findById(id)
                        .orElseThrow(() -> new com.arecaharvest.exception.ResourceNotFoundException("Inventory", "id", id))));
    }
}
