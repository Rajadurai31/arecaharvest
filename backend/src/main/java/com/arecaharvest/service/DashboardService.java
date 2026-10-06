package com.arecaharvest.service;

import com.arecaharvest.entity.enums.ContractStatus;
import com.arecaharvest.entity.enums.FieldStatus;
import com.arecaharvest.entity.enums.WorkerStatus;
import com.arecaharvest.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FieldRepository fieldRepository;
    private final AttendanceRepository attendanceRepository;
    private final HarvestSessionRepository sessionRepository;
    private final WageLedgerRepository wageLedgerRepository;
    private final ExpenseRepository expenseRepository;
    private final SaleRepository saleRepository;
    private final InventoryRepository inventoryRepository;
    private final WorkerRepository workerRepository;
    private final HarvestingContractRepository contractRepository;

    @Transactional(readOnly = true)
    public DashboardKpis getTodayKpis() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        return DashboardKpis.builder()
                // Counts
                .activeFields(fieldRepository.countByStatus(FieldStatus.HARVESTING))
                .totalActiveWorkers(workerRepository.countByStatus(WorkerStatus.ACTIVE))
                .workersWorkingToday(attendanceRepository.countWorkersOnDate(today))
                .activeContracts(contractRepository.countByStatus(ContractStatus.ACTIVE))
                // Today's operations
                .todayHarvestKg(sessionRepository.sumQuantityForDate(today))
                .todayWorkerWage(wageLedgerRepository.sumWagesForPeriod(today, today))
                .todayExpenses(expenseRepository.sumExpensesForDate(today))
                .todaySalesRevenue(saleRepository.sumRevenueForDate(today))
                // Outstanding
                .pendingWorkerWages(sumAllOutstandingWages())
                .buyerOutstanding(saleRepository.totalBuyerOutstanding())
                .currentInventoryKg(inventoryRepository.totalAvailableQuantity())
                // Month-to-date
                .monthRevenue(saleRepository.sumRevenueForPeriod(monthStart, today))
                .monthExpenses(expenseRepository.sumExpensesForPeriod(monthStart, today))
                .monthHarvestKg(sessionRepository.sumQuantityForPeriod(monthStart, today))
                .build();
    }

    private BigDecimal sumAllOutstandingWages() {
        try {
            return wageLedgerRepository.sumAllPositiveOutstandingBalances();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Getter
    @Builder
    public static class DashboardKpis {
        private long activeFields;
        private long totalActiveWorkers;
        private long workersWorkingToday;
        private long activeContracts;
        private BigDecimal todayHarvestKg;
        private BigDecimal todayWorkerWage;
        private BigDecimal todayExpenses;
        private BigDecimal todaySalesRevenue;
        private BigDecimal pendingWorkerWages;
        private BigDecimal buyerOutstanding;
        private BigDecimal currentInventoryKg;
        private BigDecimal monthRevenue;
        private BigDecimal monthExpenses;
        private BigDecimal monthHarvestKg;
    }
}
