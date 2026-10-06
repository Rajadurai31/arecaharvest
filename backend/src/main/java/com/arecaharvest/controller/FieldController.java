package com.arecaharvest.controller;

import com.arecaharvest.dto.request.FieldRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.entity.enums.FieldStatus;
import com.arecaharvest.service.FieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Fields", description = "Areca nut field management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/fields")
@RequiredArgsConstructor
public class FieldController {

    private final FieldService fieldService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<FieldResponse>>> search(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) FieldStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fieldName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
        return ResponseEntity.ok(ApiResponse.success(fieldService.searchFields(ownerId, status, search, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Get field details with harvest progress")
    public ResponseEntity<ApiResponse<FieldResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fieldService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FieldResponse>> create(@Valid @RequestBody FieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fieldService.create(request), "Field created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FieldResponse>> update(
            @PathVariable Long id, @Valid @RequestBody FieldRequest request) {
        return ResponseEntity.ok(ApiResponse.success(fieldService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update field status (ACTIVE/HARVESTING/COMPLETED etc.)")
    public ResponseEntity<ApiResponse<FieldResponse>> updateStatus(
            @PathVariable Long id, @RequestParam FieldStatus status) {
        return ResponseEntity.ok(ApiResponse.success(fieldService.updateStatus(id, status)));
    }
}
