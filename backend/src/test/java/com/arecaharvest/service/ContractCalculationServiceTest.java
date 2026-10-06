package com.arecaharvest.service;

import com.arecaharvest.entity.Field;
import com.arecaharvest.entity.HarvestingContract;
import com.arecaharvest.entity.enums.ContractType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContractCalculationService")
class ContractCalculationServiceTest {

    private ContractCalculationService service;

    @BeforeEach
    void setUp() {
        service = new ContractCalculationService();
    }

    // ── Expected Revenue ────────────────────────────────────────────────────

    @Nested
    @DisplayName("calculateExpectedRevenue")
    class CalculateExpectedRevenue {

        @Test
        @DisplayName("PER_KG: 5000 KG × ₹4.50 = ₹22,500")
        void perKg_returnsQuantityTimesRate() {
            HarvestingContract c = contract(ContractType.PER_KG, bd("4.50"), null, bd("5000"));
            BigDecimal result = service.calculateExpectedRevenue(c);
            assertThat(result).isEqualByComparingTo(bd("22500.00"));
        }

        @Test
        @DisplayName("PER_TREE: 120 trees × ₹120 = ₹14,400")
        void perTree_returnsTreeCountTimesRate() {
            Field field = new Field();
            field.setTreeCount(120);
            HarvestingContract c = contractWithField(ContractType.PER_TREE, bd("120.00"), field);
            BigDecimal result = service.calculateExpectedRevenue(c);
            assertThat(result).isEqualByComparingTo(bd("14400.00"));
        }

        @Test
        @DisplayName("FIXED_AMOUNT: returns fixedAmount regardless of qty")
        void fixedAmount_returnsFixed() {
            HarvestingContract c = contract(ContractType.FIXED_AMOUNT, BigDecimal.ZERO, bd("28000.00"), null);
            BigDecimal result = service.calculateExpectedRevenue(c);
            assertThat(result).isEqualByComparingTo(bd("28000.00"));
        }

        @Test
        @DisplayName("PER_KG with null quantity returns zero")
        void perKg_nullQuantity_returnsZero() {
            HarvestingContract c = contract(ContractType.PER_KG, bd("4.50"), null, null);
            assertThat(service.calculateExpectedRevenue(c)).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── Daily Wage Calculation ──────────────────────────────────────────────

    @Nested
    @DisplayName("calculateDailyWage")
    class CalculateDailyWage {

        @Test
        @DisplayName("PRESENT: full wage = ₹900")
        void present_fullWage() {
            BigDecimal wage = service.calculateDailyWage(bd("900"), "PRESENT", BigDecimal.ZERO, BigDecimal.ZERO);
            assertThat(wage).isEqualByComparingTo(bd("900.00"));
        }

        @Test
        @DisplayName("HALF_DAY: half wage = ₹450")
        void halfDay_halfWage() {
            BigDecimal wage = service.calculateDailyWage(bd("900"), "HALF_DAY", BigDecimal.ZERO, BigDecimal.ZERO);
            assertThat(wage).isEqualByComparingTo(bd("450.00"));
        }

        @Test
        @DisplayName("ABSENT: zero wage")
        void absent_zeroWage() {
            BigDecimal wage = service.calculateDailyWage(bd("900"), "ABSENT", BigDecimal.ZERO, BigDecimal.ZERO);
            assertThat(wage).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("LEAVE: zero wage (unpaid leave assumption)")
        void leave_zeroWage() {
            BigDecimal wage = service.calculateDailyWage(bd("900"), "LEAVE", BigDecimal.ZERO, BigDecimal.ZERO);
            assertThat(wage).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("PRESENT + 2h overtime at ₹100/h = ₹900 + ₹200 = ₹1100")
        void present_withOvertime() {
            BigDecimal wage = service.calculateDailyWage(bd("900"), "PRESENT", bd("2"), bd("100"));
            assertThat(wage).isEqualByComparingTo(bd("1100.00"));
        }

        @Test
        @DisplayName("HALF_DAY + 1h overtime at ₹100/h = ₹450 + ₹100 = ₹550")
        void halfDay_withOvertime() {
            BigDecimal wage = service.calculateDailyWage(bd("900"), "HALF_DAY", bd("1"), bd("100"));
            assertThat(wage).isEqualByComparingTo(bd("550.00"));
        }

        @Test
        @DisplayName("Null daily wage returns zero")
        void nullWage_returnsZero() {
            assertThat(service.calculateDailyWage(null, "PRESENT", BigDecimal.ZERO, BigDecimal.ZERO))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── Progress Percent ───────────────────────────────────────────────────

    @Nested
    @DisplayName("calculateProgressPercent")
    class CalculateProgressPercent {

        @Test
        @DisplayName("3500 harvested of 5000 expected = 70%")
        void typical_70percent() {
            double p = service.calculateProgressPercent(bd("5000"), bd("3500"));
            assertThat(p).isCloseTo(70.0, org.assertj.core.data.Offset.offset(0.01));
        }

        @Test
        @DisplayName("Harvested > expected caps at 100%")
        void overHarvest_capAt100() {
            double p = service.calculateProgressPercent(bd("5000"), bd("6000"));
            assertThat(p).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Zero expected returns 0%")
        void zeroExpected_returns0() {
            double p = service.calculateProgressPercent(BigDecimal.ZERO, bd("1000"));
            assertThat(p).isEqualTo(0.0);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    private HarvestingContract contract(ContractType type, BigDecimal rate,
                                         BigDecimal fixed, BigDecimal expected) {
        Field field = new Field();
        field.setTreeCount(0);
        HarvestingContract c = new HarvestingContract();
        c.setContractType(type);
        c.setRate(rate);
        c.setFixedAmount(fixed);
        c.setExpectedQuantityKg(expected);
        c.setField(field);
        return c;
    }

    private HarvestingContract contractWithField(ContractType type, BigDecimal rate, Field field) {
        HarvestingContract c = new HarvestingContract();
        c.setContractType(type);
        c.setRate(rate);
        c.setField(field);
        return c;
    }
}
