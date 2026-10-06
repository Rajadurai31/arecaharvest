package com.arecaharvest.controller;

import com.arecaharvest.dto.request.WorkerRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.entity.enums.SkillType;
import com.arecaharvest.entity.enums.WorkerStatus;
import com.arecaharvest.service.WorkerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Workers", description = "Harvesting worker management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<WorkerResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) WorkerStatus status,
            @RequestParam(required = false) SkillType skillType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
        return ResponseEntity.ok(ApiResponse.success(workerService.searchWorkers(search, status, skillType, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','WORKER')")
    public ResponseEntity<ApiResponse<WorkerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(workerService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkerResponse>> create(@Valid @RequestBody WorkerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(workerService.create(request), "Worker created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkerResponse>> update(
            @PathVariable Long id, @Valid @RequestBody WorkerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(workerService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkerResponse>> updateStatus(
            @PathVariable Long id, @RequestParam WorkerStatus status) {
        return ResponseEntity.ok(ApiResponse.success(workerService.updateStatus(id, status)));
    }
}
