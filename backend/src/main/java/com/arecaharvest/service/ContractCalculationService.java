package com.arecaharvest.service;

import com.arecaharvest.entity.HarvestingContract;
import com.arecaharvest.entity.enums.ContractType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Dedicated service for contract revenue calculations.
 * Business rules are isolated here so they can be tested independently
 * and changed without touching controllers or other services.
 *
 * Assumption: All monetary calculations round to 2 decimal places using HALF_UP.
 *
 * Contract types:
 *   PER_KG      → revenue = harvestedKg × rate
 *   PER_TREE    → revenue = treeCount × rate   (fixed at contract start)
 *   FIXED_AMOUNT→ revenue = fixedAmount         (regardless of harvest quantity)
 */
@Service
public class ContractCalculationService {

    /**
     * Calculate expected revenue based on contract type and expected quantity/tree count.
     */
    public BigDecimal calculateExpectedRevenue(HarvestingContract contract) {
        return switch (contract.getContractType()) {
            case PER_KG -> {
                BigDecimal qty = contract.getExpectedQuantityKg();
                if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) yield BigDecimal.ZERO;
                yield qty.multiply(contract.getRate()).setScale(2, RoundingMode.HALF_UP);
            }
            case PER_TREE -> {
                Integer treeCount = contract.getField().getTreeCount();
                if (treeCount == null || treeCount <= 0) yield BigDecimal.ZERO;
                yield BigDecimal.valueOf(treeCount)
                        .multiply(contract.getRate())
                        .setScale(2, RoundingMode.HALF_UP);
            }
            case FIXED_AMOUNT -> {
                BigDecimal fixed = contract.getFixedAmount();
                yield (fixed != null) ? fixed.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            }
        };
    }

    /**
     * Calculate actual revenue earned based on what has actually been harvested.
     */
    public BigDecimal calculateActualRevenue(HarvestingContract contract,
                                             BigDecimal actualHarvestedKg) {
        if (actualHarvestedKg == null || actualHarvestedKg.compareTo(BigDecimal.ZERO) < 0) {
            actualHarvestedKg = BigDecimal.ZERO;
        }

        return switch (contract.getContractType()) {
            case PER_KG ->
                actualHarvestedKg.multiply(contract.getRate())
                        .setScale(2, RoundingMode.HALF_UP);

            case PER_TREE -> {
                // PER_TREE: full amount is earned once the job is done
                // (same as expected — tree count doesn't change mid-harvest)
                yield calculateExpectedRevenue(contract);
            }

            case FIXED_AMOUNT -> {
                BigDecimal fixed = contract.getFixedAmount();
                yield (fixed != null) ? fixed.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            }
        };
    }

    /**
     * Returns the progress percentage (0–100) based on harvested vs expected quantity.
     * For FIXED_AMOUNT or PER_TREE contracts, progress is based on quantity comparison.
     */
    public double calculateProgressPercent(BigDecimal expectedKg, BigDecimal harvestedKg) {
        if (expectedKg == null || expectedKg.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        if (harvestedKg == null || harvestedKg.compareTo(BigDecimal.ZERO) <= 0) return 0.0;

        double progress = harvestedKg.divide(expectedKg, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        return Math.min(100.0, progress);
    }

    /**
     * Compute the wage for a single attendance record.
     *
     * PRESENT   → dailyWage × 1.0
     * HALF_DAY  → dailyWage × 0.5
     * ABSENT    → 0
     * LEAVE     → 0  (assumption: unpaid leave; configurable if paid leave is needed)
     *
     * Overtime  → overtimeHours × overtimeRate
     */
    public BigDecimal calculateDailyWage(BigDecimal dailyWage,
                                         String attendanceStatus,
                                         BigDecimal overtimeHours,
                                         BigDecimal overtimeRate) {
        if (dailyWage == null) return BigDecimal.ZERO;

        BigDecimal baseWage = switch (attendanceStatus) {
            case "PRESENT"   -> dailyWage;
            case "HALF_DAY"  -> dailyWage.multiply(new BigDecimal("0.5"));
            default          -> BigDecimal.ZERO;  // ABSENT, LEAVE
        };

        BigDecimal overtime = BigDecimal.ZERO;
        if (overtimeHours != null && overtimeRate != null
                && overtimeHours.compareTo(BigDecimal.ZERO) > 0
                && overtimeRate.compareTo(BigDecimal.ZERO) > 0) {
            overtime = overtimeHours.multiply(overtimeRate);
        }

        return baseWage.add(overtime).setScale(2, RoundingMode.HALF_UP);
    }
}
