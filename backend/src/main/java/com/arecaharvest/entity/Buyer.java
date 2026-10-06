package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "buyers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Buyer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 200)
    private String village;

    @Column(length = 200)
    private String district;

    @Column(length = 100)
    @Builder.Default
    private String state = "Karnataka";

    @Column(name = "payment_terms", length = 200)
    private String paymentTerms;

    @Column(name = "default_rate_per_kg", precision = 10, scale = 4)
    private BigDecimal defaultRatePerKg;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc", length = 20)
    private String bankIfsc;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BuyerStatus status = BuyerStatus.ACTIVE;

    public enum BuyerStatus {
        ACTIVE, INACTIVE
    }
}
