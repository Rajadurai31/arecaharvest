package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sales")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Sale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_number", nullable = false, unique = true, length = 50)
    private String saleNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(name = "quantity_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityKg;

    @Column(name = "quantity_unit", nullable = false, length = 10)
    @Builder.Default
    private String quantityUnit = "KG";

    @Column(name = "rate_per_unit", nullable = false, precision = 10, scale = 4)
    private BigDecimal ratePerUnit;

    /** gross_amount = quantity_kg × rate_per_unit — computed, stored for audit */
    @Column(name = "gross_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "transport_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal transportCost = BigDecimal.ZERO;

    @Column(name = "loading_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal loadingCost = BigDecimal.ZERO;

    @Column(name = "other_charges", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal otherCharges = BigDecimal.ZERO;

    @Column(name = "other_charges_desc", length = 200)
    private String otherChargesDesc;

    /** net_amount = gross_amount − transport_cost − loading_cost − other_charges */
    @Column(name = "net_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private SalePaymentStatus paymentStatus = SalePaymentStatus.PENDING;

    @Column(name = "amount_received", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal amountReceived = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum SalePaymentStatus {
        PENDING, PARTIAL, PAID
    }
}
