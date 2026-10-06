package com.arecaharvest.entity;

import com.arecaharvest.entity.enums.ContractStatus;
import com.arecaharvest.entity.enums.ContractType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "harvesting_contracts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HarvestingContract extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Column(name = "contract_number", nullable = false, unique = true, length = 50)
    private String contractNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 20)
    private ContractType contractType;

    /** Rate per unit — interpretation depends on contractType */
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal rate;

    @Column(name = "fixed_amount", precision = 12, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "expected_quantity_kg", precision = 12, scale = 3)
    private BigDecimal expectedQuantityKg;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
