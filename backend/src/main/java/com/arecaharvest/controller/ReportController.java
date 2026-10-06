package com.arecaharvest.controller;

import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.service.ReportService;
import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reports", description = "Exportable business reports — CSV, Excel, PDF")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public class ReportController {

    private final ReportService reportService;

    // ── JSON data endpoints ──────────────────────────────────────────────────

    @GetMapping("/worker-wages")
    @Operation(summary = "Worker wage report data")
    public ResponseEntity<ApiResponse<List<ReportService.WorkerWageRow>>> workerWages(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.buildWorkerWageReport(fromDate, toDate)));
    }

    @GetMapping("/sales")
    @Operation(summary = "Sales report data")
    public ResponseEntity<ApiResponse<List<ReportService.SaleRow>>> sales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.buildSalesReport(fromDate, toDate)));
    }

    @GetMapping("/expenses")
    @Operation(summary = "Expense report data")
    public ResponseEntity<ApiResponse<List<ReportService.ExpenseRow>>> expenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.buildExpenseReport(fromDate, toDate)));
    }

    @GetMapping("/profit")
    @Operation(summary = "P&L report data (monthly breakdown)")
    public ResponseEntity<ApiResponse<List<ReportService.ProfitRow>>> profit(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.buildProfitReport(fromDate, toDate)));
    }

    // ── CSV exports ──────────────────────────────────────────────────────────

    @GetMapping("/worker-wages/export/csv")
    @Operation(summary = "Download worker wages as CSV")
    public ResponseEntity<byte[]> workerWagesCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return csvResponse("worker-wages.csv", reportService.exportWorkerWagesCSV(fromDate, toDate));
    }

    @GetMapping("/sales/export/csv")
    public ResponseEntity<byte[]> salesCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return csvResponse("sales.csv", reportService.exportSalesCSV(fromDate, toDate));
    }

    @GetMapping("/expenses/export/csv")
    public ResponseEntity<byte[]> expensesCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return csvResponse("expenses.csv", reportService.exportExpensesCSV(fromDate, toDate));
    }

    @GetMapping("/profit/export/csv")
    public ResponseEntity<byte[]> profitCSV(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return csvResponse("profit.csv", reportService.exportProfitCSV(fromDate, toDate));
    }

    // ── Excel exports ────────────────────────────────────────────────────────

    @GetMapping("/worker-wages/export/excel")
    public ResponseEntity<byte[]> workerWagesExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws IOException {
        return excelResponse("worker-wages.xlsx", reportService.exportWorkerWagesExcel(fromDate, toDate));
    }

    @GetMapping("/sales/export/excel")
    public ResponseEntity<byte[]> salesExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws IOException {
        return excelResponse("sales.xlsx", reportService.exportSalesExcel(fromDate, toDate));
    }

    // ── PDF exports ──────────────────────────────────────────────────────────

    @GetMapping("/profit/export/pdf")
    public ResponseEntity<byte[]> profitPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate)
            throws DocumentException {
        return pdfResponse("profit-report.pdf", reportService.exportProfitPDF(fromDate, toDate));
    }

    @GetMapping("/worker-wages/export/pdf")
    public ResponseEntity<byte[]> workerWagesPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate)
            throws DocumentException {
        return pdfResponse("worker-wages.pdf", reportService.exportWorkerWagesPDF(fromDate, toDate));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> csvResponse(String filename, byte[] data) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }

    private ResponseEntity<byte[]> excelResponse(String filename, byte[] data) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    private ResponseEntity<byte[]> pdfResponse(String filename, byte[] data) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
