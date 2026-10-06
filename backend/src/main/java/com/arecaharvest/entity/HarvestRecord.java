package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "harvest_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HarvestRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_session_id", nullable = false)
    private HarvestSession harvestSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private HarvestingContract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "bags_count")
    @Builder.Default
    private Integer bagsCount = 0;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    /** Always stored alongside quantity for data quality */
    @Column(name = "quantity_unit", nullable = false, length = 10)
    @Builder.Default
    private String quantityUnit = "KG";

    @Column(name = "worker_count")
    private Integer workerCount;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    /** Amendment trail — historical records are never overwritten */
    @Column(name = "is_amendment", nullable = false)
    @Builder.Default
    private boolean amendment = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_record_id")
    private HarvestRecord originalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amended_by")
    private User amendedBy;

    @Column(name = "amendment_reason", columnDefinition = "TEXT")
    private String amendmentReason;
}
