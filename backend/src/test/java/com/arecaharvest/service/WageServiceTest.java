package com.arecaharvest.service;

import com.arecaharvest.dto.request.WorkerPaymentRequest;
import com.arecaharvest.entity.*;
import com.arecaharvest.entity.enums.PaymentMethod;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("WageService — Payment validation")
@ExtendWith(MockitoExtension.class)
class WageServiceTest {

    @Mock private WageLedgerRepository wageLedgerRepository;
    @Mock private WorkerPaymentRepository workerPaymentRepository;
    @Mock private WorkerRepository workerRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLog;

    @InjectMocks private WageService wageService;

    private Worker worker;

    @BeforeEach
    void setUp() {
        worker = new Worker();
        worker.setId(1L);
        worker.setName("Raju Gowda");
        worker.setDailyWage(new BigDecimal("900"));
    }

    @Test
    @DisplayName("Payment within outstanding balance succeeds")
    void payment_withinBalance_succeeds() {
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(wageLedgerRepository.calculateOutstandingBalance(1L)).thenReturn(new BigDecimal("3600"));

        WorkerPayment savedPayment = new WorkerPayment();
        savedPayment.setId(1L);
        savedPayment.setWorker(worker);
        savedPayment.setPaymentDate(LocalDate.now());
        savedPayment.setAmount(new BigDecimal("500"));
        savedPayment.setPaymentType(WorkerPayment.PaymentType.PAYMENT);
        savedPayment.setPaymentMethod(PaymentMethod.CASH);

        WageLedger savedLedger = new WageLedger();
        savedLedger.setId(1L);

        when(workerPaymentRepository.save(any())).thenReturn(savedPayment);
        when(wageLedgerRepository.save(any())).thenReturn(savedLedger);

        WorkerPaymentRequest req = new WorkerPaymentRequest();
        req.setWorkerId(1L);
        req.setAmount(new BigDecimal("500"));
        req.setPaymentType(WorkerPayment.PaymentType.PAYMENT);
        req.setPaymentMethod(PaymentMethod.CASH);
        req.setPaymentDate(LocalDate.now());

        assertThatNoException().isThrownBy(() -> wageService.recordPayment(req));
    }

    @Test
    @DisplayName("Payment exceeding outstanding balance throws BusinessException")
    void payment_exceedsBalance_throwsBusinessException() {
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(wageLedgerRepository.calculateOutstandingBalance(1L)).thenReturn(new BigDecimal("500"));

        WorkerPaymentRequest req = new WorkerPaymentRequest();
        req.setWorkerId(1L);
        req.setAmount(new BigDecimal("1000"));    // exceeds ₹500 balance
        req.setPaymentType(WorkerPayment.PaymentType.PAYMENT);
        req.setPaymentMethod(PaymentMethod.CASH);
        req.setPaymentDate(LocalDate.now());

        assertThatThrownBy(() -> wageService.recordPayment(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exceeds outstanding balance");
    }

    @Test
    @DisplayName("ADVANCE type allowed even when balance is zero")
    void advance_allowedWhenBalanceIsZero() {
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        // Outstanding = 0, but ADVANCE should be allowed

        WorkerPayment savedPayment = new WorkerPayment();
        savedPayment.setId(2L);
        savedPayment.setWorker(worker);
        savedPayment.setPaymentDate(LocalDate.now());
        savedPayment.setAmount(new BigDecimal("200"));
        savedPayment.setPaymentType(WorkerPayment.PaymentType.ADVANCE);
        savedPayment.setPaymentMethod(PaymentMethod.CASH);

        WageLedger savedLedger = new WageLedger();
        savedLedger.setId(2L);

        when(workerPaymentRepository.save(any())).thenReturn(savedPayment);
        when(wageLedgerRepository.save(any())).thenReturn(savedLedger);

        WorkerPaymentRequest req = new WorkerPaymentRequest();
        req.setWorkerId(1L);
        req.setAmount(new BigDecimal("200"));
        req.setPaymentType(WorkerPayment.PaymentType.ADVANCE);
        req.setPaymentMethod(PaymentMethod.CASH);
        req.setPaymentDate(LocalDate.now());

        // Should not query balance for ADVANCE type
        assertThatNoException().isThrownBy(() -> wageService.recordPayment(req));
        verify(wageLedgerRepository, never()).calculateOutstandingBalance(anyLong());
    }
}
