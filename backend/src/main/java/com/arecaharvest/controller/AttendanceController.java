package com.arecaharvest.controller;

import com.arecaharvest.dto.request.AttendanceRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.service.AttendanceService;
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

@Tag(name = "Attendance", description = "Daily worker attendance tracking")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<AttendanceResponse>>> search(
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.searchAttendance(workerId, fieldId, fromDate, toDate, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Mark attendance — automatically creates wage ledger entry")
    public ResponseEntity<ApiResponse<AttendanceResponse>> mark(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attendanceService.markAttendance(request), "Attendance marked"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> update(
            @PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.updateAttendance(id, request)));
    }
}
