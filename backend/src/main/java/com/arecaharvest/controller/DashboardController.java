package com.arecaharvest.controller;

import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.repository.BuyerPaymentRepository;
import com.arecaharvest.repository.BuyerRepository;
import com.arecaharvest.repository.ExpenseRepository;
import com.arecaharvest.repository.HarvestSessionRepository;
import com.arecaharvest.repository.SaleRepository;
import com.arecaharvest.repository.WageLedgerRepository;
import com.arecaharvest.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Dashboard", description = "KPIs and analytics for the Head Harvester dashboard")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final HarvestSessionRepository sessionRepository;
    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final WageLedgerRepository wageLedgerRepository;
    private final BuyerRepository buyerRepository;
    private final BuyerPaymentRepository buyerPaymentRepository;

    @GetMapping("/kpis")
    @Operation(summary = "Get today's KPIs")
    public ResponseEntity<ApiResponse<DashboardService.DashboardKpis>> getKpis() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTodayKpis()));
    }

    @GetMapping("/harvest-trend")
    @Operation(summary = "Daily harvest quantities for a date range (chart data)")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getHarvestTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Map<String, BigDecimal> trend = new LinkedHashMap<>();
        LocalDate d = fromDate;
        while (!d.isAfter(toDate)) {
            trend.put(d.toString(), sessionRepository.sumQuantityForDate(d));
            d = d.plusDays(1);
        }
        return ResponseEntity.ok(ApiResponse.success(trend, "Daily harvest trend"));
    }

    @GetMapping("/revenue-vs-expenses")
    @Operation(summary = "Monthly revenue vs expenses for a date range (chart data)")
    public ResponseEntity<ApiResponse<Map<String, Map<String, BigDecimal>>>> getRevenueVsExpenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        LocalDate monthCursor = fromDate.withDayOfMonth(1);

        while (!monthCursor.isAfter(toDate)) {
            LocalDate monthEnd = monthCursor.withDayOfMonth(monthCursor.lengthOfMonth());
            LocalDate effectiveTo = monthEnd.isAfter(toDate) ? toDate : monthEnd;
            BigDecimal revenue = saleRepository.sumRevenueForPeriod(monthCursor, effectiveTo);
            BigDecimal expenses = expenseRepository.sumExpensesForPeriod(monthCursor, effectiveTo);
            Map<String, BigDecimal> entry = new LinkedHashMap<>();
            entry.put("revenue", revenue);
            entry.put("expenses", expenses);
            entry.put("profit", revenue.subtract(expenses));
            result.put(monthCursor.getYear() + "-" + String.format("%02d", monthCursor.getMonthValue()), entry);
            monthCursor = monthCursor.plusMonths(1);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/wage-trend")
    @Operation(summary = "Weekly wage totals for chart")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getWageTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Map<String, BigDecimal> trend = new LinkedHashMap<>();
        LocalDate weekStart = fromDate;
        while (!weekStart.isAfter(toDate)) {
            LocalDate weekEnd = weekStart.plusDays(6).isAfter(toDate) ? toDate : weekStart.plusDays(6);
            trend.put(weekStart.toString(), wageLedgerRepository.sumWagesForPeriod(weekStart, weekEnd));
            weekStart = weekStart.plusWeeks(1);
        }
        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    @GetMapping("/field-wise-harvest")
    @Operation(summary = "Field-wise harvest totals for chart")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getFieldWiseHarvest(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        sessionRepository.fieldWiseHarvestTotals(fromDate, toDate)
                .forEach(row -> result.put((String) row[1], (BigDecimal) row[2]));
        return ResponseEntity.ok(ApiResponse.success(result, "Field-wise harvest totals"));
    }

    @GetMapping("/sales-by-buyer")
    @Operation(summary = "Sales totals grouped by buyer for chart")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getSalesByBuyer(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        buyerRepository.findAll().forEach(buyer -> {
            BigDecimal total = saleRepository.sumRevenueForPeriod(fromDate, toDate);
            // Per-buyer sales from ledger
            BigDecimal buyerTotal = buyerPaymentRepository
                    .calculateOutstandingBalance(buyer.getId()).abs();
            result.put(buyer.getName(), buyerTotal);
        });
        // Remove zero entries and sort descending
        result.entrySet().removeIf(e -> e.getValue().compareTo(BigDecimal.ZERO) == 0);
        return ResponseEntity.ok(ApiResponse.success(result, "Sales by buyer"));
    }

    @GetMapping("/pending-payments")
    @Operation(summary = "Pending payments summary (workers + buyers)")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getPendingPayments() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        result.put("workerOutstanding", wageLedgerRepository.sumAllPositiveOutstandingBalances());
        result.put("buyerOutstanding", saleRepository.totalBuyerOutstanding());
        return ResponseEntity.ok(ApiResponse.success(result, "Pending payments summary"));
    }
}
