package com.arecaharvest.controller;

import com.arecaharvest.dto.request.ScheduleRequest;
import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Schedule;
import com.arecaharvest.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Schedules", description = "Work scheduling and calendar")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<Schedule>>> search(
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.search(fieldId, status, fromDate, toDate,
                        PageRequest.of(page, size))));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Get today's scheduled work")
    public ResponseEntity<ApiResponse<List<Schedule>>> getToday() {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getToday()));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Get next 7 days planned work")
    public ResponseEntity<ApiResponse<List<Schedule>>> getUpcoming() {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getUpcoming()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<Schedule>> create(@RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(scheduleService.create(request), "Schedule created"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<Schedule>> updateStatus(
            @PathVariable Long id, @RequestParam Schedule.ScheduleStatus status) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.updateStatus(id, status)));
    }
}
