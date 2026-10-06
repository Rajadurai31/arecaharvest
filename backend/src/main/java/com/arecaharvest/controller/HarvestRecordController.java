package com.arecaharvest.controller;

import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.HarvestRecord;
import com.arecaharvest.service.HarvestRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Harvest Records", description = "Detailed harvest quantity records with amendment audit trail")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/harvest-records")
@RequiredArgsConstructor
public class HarvestRecordController {

    private final HarvestRecordService recordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ApiResponse<PagedResponse<HarvestRecord>>> search(
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("recordDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                recordService.search(fieldId, contractId, fromDate, toDate, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Create harvest record — auto-creates inventory entry")
    public ResponseEntity<ApiResponse<HarvestRecord>> create(
            @RequestBody HarvestRecordService.HarvestRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(recordService.create(request), "Harvest record created"));
    }

    @PostMapping("/{id}/amend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Amend a harvest record — creates new record with audit trail, original preserved")
    public ResponseEntity<ApiResponse<HarvestRecord>> amend(
            @PathVariable Long id,
            @RequestBody HarvestRecordService.AmendmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(recordService.amend(id, request), "Amendment created"));
    }
}
