package com.arecaharvest.controller;

import com.arecaharvest.dto.request.OwnerRequest;
import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.OwnerResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.service.OwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Owners", description = "Areca nut field owner management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Search and list owners")
    public ResponseEntity<ApiResponse<PagedResponse<OwnerResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
        return ResponseEntity.ok(ApiResponse.success(ownerService.searchOwners(search, active, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Get owner details with field and contract summary")
    public ResponseEntity<ApiResponse<OwnerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ownerService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new owner")
    public ResponseEntity<ApiResponse<OwnerResponse>> create(@Valid @RequestBody OwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ownerService.create(request), "Owner created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update owner details")
    public ResponseEntity<ApiResponse<OwnerResponse>> update(
            @PathVariable Long id, @Valid @RequestBody OwnerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ownerService.update(id, request), "Owner updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate owner")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        ownerService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Owner deactivated"));
    }
}
