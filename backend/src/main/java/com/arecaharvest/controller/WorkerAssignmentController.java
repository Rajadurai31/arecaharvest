package com.arecaharvest.controller;

import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.WorkerAssignment;
import com.arecaharvest.service.WorkerAssignmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Worker Assignments", description = "Assign workers to fields and contracts")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/worker-assignments")
@RequiredArgsConstructor
public class WorkerAssignmentController {

    private final WorkerAssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<WorkerAssignment>>> search(
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("assignedDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.search(workerId, fieldId, fromDate, toDate, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<WorkerAssignment>> create(
            @Valid @RequestBody WorkerAssignmentService.AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(assignmentService.create(request), "Assignment created"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<WorkerAssignment>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.cancel(id), "Assignment cancelled"));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<WorkerAssignment>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.complete(id), "Assignment completed"));
    }
}
