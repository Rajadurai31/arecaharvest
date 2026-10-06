package com.arecaharvest.controller;

import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Equipment;
import com.arecaharvest.service.EquipmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Equipment", description = "Harvesting equipment management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<Equipment>>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(ApiResponse.success(equipmentService.search(category, status, pageable)));
    }

    @GetMapping("/maintenance-due")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<Equipment>>> getMaintenanceDue() {
        return ResponseEntity.ok(ApiResponse.success(equipmentService.getMaintenanceDue(),
                "Equipment due for maintenance in next 7 days"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Equipment>> create(@RequestBody EquipmentService.EquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(equipmentService.create(request), "Equipment created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Equipment>> update(
            @PathVariable Long id, @RequestBody EquipmentService.EquipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(equipmentService.update(id, request)));
    }
}
