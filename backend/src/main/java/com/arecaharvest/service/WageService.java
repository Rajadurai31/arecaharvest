package com.arecaharvest.service;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.dto.request.WorkerPaymentRequest;
import com.arecaharvest.dto.response.*;
import com.arecaharvest.entity.*;
import com.arecaharvest.entity.WageLedger.TransactionType;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WageService {

    private final WageLedgerRepository wageLedgerRepository;
    private final WorkerPaymentRepository workerPaymentRepository;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public WorkerLedgerSummaryResponse getWorkerLedger(Long workerId, Pageable pageable) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", workerId));

        Page<WageLedger> page = wageLedgerRepository.findByWorkerId(workerId, pageable);
        List<WageLedgerResponse> transactions = page.getContent().stream()
                .map(this::toLedgerResponse).collect(Collectors.toList());

        return WorkerLedgerSummaryResponse.builder()
                .workerId(workerId).workerName(worker.getName())
                .totalEarned(wageLedgerRepository.calculateTotalEarned(workerId))
                .totalPaid(wageLedgerRepository.calculateTotalPaid(workerId))
                .outstandingBalance(wageLedgerRepository.calculateOutstandingBalance(workerId))
                .transactions(transactions)
                .build();
    }

    @Transactional
    public WorkerPaymentResponse recordPayment(WorkerPaymentRequest req) {
        Worker worker = workerRepository.findById(req.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", req.getWorkerId()));

        // Validate: do not allow PAYMENT > outstanding balance
        // ADVANCE is allowed even if balance is zero (will go negative — worker owes back)
        if (req.getPaymentType() == WorkerPayment.PaymentType.PAYMENT) {
            BigDecimal outstanding = wageLedgerRepository.calculateOutstandingBalance(req.getWorkerId());
            if (req.getAmount().compareTo(outstanding) > 0) {
                throw new BusinessException(
                    String.format("Payment amount ₹%.2f exceeds outstanding balance ₹%.2f. " +
                                  "Record as ADVANCE if intentional.", req.getAmount(), outstanding));
            }
        }

        User currentUser = getCurrentUser();

        // 1. Create the payment record
        WorkerPayment payment = WorkerPayment.builder()
                .worker(worker).paymentDate(req.getPaymentDate())
                .amount(req.getAmount()).paymentType(req.getPaymentType())
                .paymentMethod(req.getPaymentMethod())
                .referenceNumber(req.getReferenceNumber())
                .remarks(req.getRemarks()).recordedBy(currentUser)
                .build();
        WorkerPayment savedPayment = workerPaymentRepository.save(payment);

        // 2. Create corresponding ledger entry
        TransactionType ledgerType = req.getPaymentType() == WorkerPayment.PaymentType.ADVANCE
                ? TransactionType.ADVANCE : TransactionType.PAYMENT;

        WageLedger ledgerEntry = WageLedger.builder()
                .worker(worker).transactionType(ledgerType)
                .amount(req.getAmount()).transactionDate(req.getPaymentDate())
                .payment(savedPayment)
                .description(ledgerType + " — " + req.getPaymentMethod()
                        + (req.getReferenceNumber() != null ? " ref:" + req.getReferenceNumber() : ""))
                .recordedBy(currentUser)
                .build();
        WageLedger savedLedger = wageLedgerRepository.save(ledgerEntry);

        // 3. Link ledger entry back to payment
        savedPayment.setWageLedger(savedLedger);
        workerPaymentRepository.save(savedPayment);

        auditLog.log("PAYMENT", "WORKER_PAYMENT", savedPayment.getId(),
                String.format("%s ₹%.2f to %s via %s",
                        ledgerType, req.getAmount(), worker.getName(), req.getPaymentMethod()));

        return toPaymentResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkerPaymentResponse> getPaymentsForWorker(Long workerId,
                                                                       LocalDate from, LocalDate to,
                                                                       Pageable pageable) {
        return PagedResponse.of(
                workerPaymentRepository.searchPayments(workerId, from, to, pageable)
                        .map(this::toPaymentResponse));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return userRepository.findById(ud.getId()).orElse(null);
        }
        return null;
    }

    private WageLedgerResponse toLedgerResponse(WageLedger l) {
        return WageLedgerResponse.builder()
                .id(l.getId())
                .workerId(l.getWorker().getId()).workerName(l.getWorker().getName())
                .transactionType(l.getTransactionType()).amount(l.getAmount())
                .adjustmentSign(l.getAdjustmentSign()).transactionDate(l.getTransactionDate())
                .attendanceId(l.getAttendance() != null ? l.getAttendance().getId() : null)
                .paymentId(l.getPayment() != null ? l.getPayment().getId() : null)
                .fieldName(l.getField() != null ? l.getField().getFieldName() : null)
                .description(l.getDescription()).createdAt(l.getCreatedAt())
                .build();
    }

    private WorkerPaymentResponse toPaymentResponse(WorkerPayment p) {
        return WorkerPaymentResponse.builder()
                .id(p.getId())
                .workerId(p.getWorker().getId()).workerName(p.getWorker().getName())
                .paymentDate(p.getPaymentDate()).amount(p.getAmount())
                .paymentType(p.getPaymentType()).paymentMethod(p.getPaymentMethod())
                .referenceNumber(p.getReferenceNumber()).remarks(p.getRemarks())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
