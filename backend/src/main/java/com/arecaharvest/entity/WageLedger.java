package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "wage_ledger")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WageLedger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    /**
     * Amount is always positive.
     * Direction (debit/credit) is determined by transactionType:
     *   DAILY_WAGE, OVERTIME, BONUS → owed TO worker (debit against contractor)
     *   ADVANCE, PAYMENT           → paid BY contractor (credit against liability)
     *   ADJUSTMENT                 → adjustmentSign determines direction
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** For ADJUSTMENT: +1 = adds to worker balance, -1 = reduces balance */
    @Column(name = "adjustment_sign")
    @Builder.Default
    private Integer adjustmentSign = 1;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private WorkerPayment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private HarvestingContract contract;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    public enum TransactionType {
        DAILY_WAGE, OVERTIME, BONUS, ADVANCE, PAYMENT, ADJUSTMENT
    }
}
