package com.arecaharvest.integration;

import com.arecaharvest.dto.request.AttendanceRequest;
import com.arecaharvest.dto.request.BuyerPaymentRequest;
import com.arecaharvest.dto.request.SaleRequest;
import com.arecaharvest.dto.request.WorkerPaymentRequest;
import com.arecaharvest.dto.response.AttendanceResponse;
import com.arecaharvest.dto.response.SaleResponse;
import com.arecaharvest.dto.response.WorkerPaymentResponse;
import com.arecaharvest.entity.*;
import com.arecaharvest.entity.enums.PaymentMethod;
import com.arecaharvest.entity.enums.SkillType;
import com.arecaharvest.entity.enums.WorkerStatus;
import com.arecaharvest.repository.*;
import com.arecaharvest.service.AttendanceService;
import com.arecaharvest.service.SaleService;
import com.arecaharvest.service.WageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Critical Path Integration Tests (Sale, Attendance, Wage)")
class CriticalPathIntegrationTest {

    @Autowired private SaleService saleService;
    @Autowired private AttendanceService attendanceService;
    @Autowired private WageService wageService;

    @Autowired private BuyerRepository buyerRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private WorkerRepository workerRepository;
    @Autowired private OwnerRepository ownerRepository;
    @Autowired private FieldRepository fieldRepository;
    @Autowired private WageLedgerRepository wageLedgerRepository;

    @Test
    @DisplayName("Sale Critical Path: Create sale -> Deduct inventory -> Post payment -> Balance updated")
    void saleCriticalPath_endToEnd() {
        // 1. Setup Buyer, Field and Inventory
        Buyer buyer = Buyer.builder()
                .name("Coastal Traders Ltd")
                .phone("9876543210")
                .status(Buyer.BuyerStatus.ACTIVE)
                .build();
        buyer = buyerRepository.save(buyer);

        Owner owner = Owner.builder()
                .name("Plantation Owner")
                .phone("9876543299")
                .active(true)
                .build();
        owner = ownerRepository.save(owner);

        Field field = Field.builder()
                .owner(owner)
                .fieldName("Estate Inventory Plot")
                .acreage(new BigDecimal("10.0"))
                .build();
        field = fieldRepository.save(field);

        Inventory inventory = Inventory.builder()
                .field(field)
                .receivedDate(LocalDate.now())
                .quantityReceived(new BigDecimal("1000.000"))
                .quantitySold(BigDecimal.ZERO)
                .availableQuantity(new BigDecimal("1000.000"))
                .quantityUnit("KG")
                .status(Inventory.InventoryStatus.AVAILABLE)
                .build();
        inventory = inventoryRepository.save(inventory);

        // 2. Record a sale of 400 KG @ 60/KG
        SaleRequest saleReq = new SaleRequest();
        saleReq.setBuyerId(buyer.getId());
        saleReq.setInventoryId(inventory.getId());
        saleReq.setSaleNumber("SAL-INT-001");
        saleReq.setSaleDate(LocalDate.now());
        saleReq.setQuantityKg(new BigDecimal("400.000"));
        saleReq.setRatePerUnit(new BigDecimal("60.00"));
        saleReq.setTransportCost(new BigDecimal("200.00"));
        saleReq.setLoadingCost(new BigDecimal("100.00"));
        saleReq.setOtherCharges(BigDecimal.ZERO);

        SaleResponse saleRes = saleService.createSale(saleReq);

        assertThat(saleRes).isNotNull();
        // Gross: 400 * 60 = 24000. Deductions: 300. Net: 23700.
        assertThat(saleRes.getNetAmount()).isEqualByComparingTo(new BigDecimal("23700.00"));

        // Verify inventory stock was reduced
        Inventory updatedInv = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertThat(updatedInv.getAvailableQuantity()).isEqualByComparingTo(new BigDecimal("600.000"));
        assertThat(updatedInv.getQuantitySold()).isEqualByComparingTo(new BigDecimal("400.000"));

        // 3. Post a partial buyer payment
        BuyerPaymentRequest payReq = new BuyerPaymentRequest();
        payReq.setBuyerId(buyer.getId());
        payReq.setSaleId(saleRes.getId());
        payReq.setPaymentDate(LocalDate.now());
        payReq.setTransactionType(BuyerPayment.TransactionType.PAYMENT);
        payReq.setAmount(new BigDecimal("15000.00"));
        payReq.setPaymentMethod(PaymentMethod.UPI);
        payReq.setReferenceNumber("UPI-TXN-12345");

        SaleResponse updatedSale = saleService.recordBuyerPayment(payReq);

        assertThat(updatedSale.getAmountReceived()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(updatedSale.getOutstanding()).isEqualByComparingTo(new BigDecimal("8700.00"));
        assertThat(updatedSale.getPaymentStatus()).isEqualTo(Sale.SalePaymentStatus.PARTIAL);
    }

    @Test
    @DisplayName("Attendance and Wage Critical Path: Mark Attendance -> Accrue Wage -> Pay Worker")
    void attendanceAndWage_endToEnd() {
        // 1. Setup Worker, Owner, Field
        Worker worker = Worker.builder()
                .name("Manjunath")
                .phone("9845012345")
                .skillType(SkillType.CLIMBER)
                .dailyWage(new BigDecimal("800.00"))
                .status(WorkerStatus.ACTIVE)
                .build();
        worker = workerRepository.save(worker);

        Owner owner = Owner.builder()
                .name("Ganesh Hegde")
                .phone("9880011223")
                .active(true)
                .build();
        owner = ownerRepository.save(owner);

        Field field = Field.builder()
                .owner(owner)
                .fieldName("South Garden")
                .acreage(new BigDecimal("5.0"))
                .build();
        field = fieldRepository.save(field);

        // 2. Mark attendance PRESENT
        AttendanceRequest attReq = new AttendanceRequest();
        attReq.setWorkerId(worker.getId());
        attReq.setFieldId(field.getId());
        attReq.setAttendanceDate(LocalDate.now());
        attReq.setStatus(Attendance.AttendanceStatus.PRESENT);
        attReq.setWorkingHours(new BigDecimal("8.0"));

        AttendanceResponse attRes = attendanceService.markAttendance(attReq);
        assertThat(attRes).isNotNull();

        // Verify Wage Ledger has positive balance
        BigDecimal earned = wageLedgerRepository.calculateTotalEarned(worker.getId());
        BigDecimal outstanding = wageLedgerRepository.calculateOutstandingBalance(worker.getId());
        assertThat(earned).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(outstanding).isEqualByComparingTo(new BigDecimal("800.00"));

        // 3. Record Worker Payment against accrued wage
        WorkerPaymentRequest payReq = new WorkerPaymentRequest();
        payReq.setWorkerId(worker.getId());
        payReq.setPaymentDate(LocalDate.now());
        payReq.setAmount(new BigDecimal("500.00"));
        payReq.setPaymentType(WorkerPayment.PaymentType.PAYMENT);
        payReq.setPaymentMethod(PaymentMethod.CASH);

        WorkerPaymentResponse payRes = wageService.recordPayment(payReq);
        assertThat(payRes).isNotNull();
        assertThat(payRes.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));

        // Verify remaining balance
        BigDecimal finalOutstanding = wageLedgerRepository.calculateOutstandingBalance(worker.getId());
        assertThat(finalOutstanding).isEqualByComparingTo(new BigDecimal("300.00"));
    }
}
