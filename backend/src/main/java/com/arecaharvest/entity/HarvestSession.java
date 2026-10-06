package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "harvest_sessions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HarvestSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private HarvestingContract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "total_bags")
    @Builder.Default
    private Integer totalBags = 0;

    @Column(name = "total_quantity", precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal totalQuantity = BigDecimal.ZERO;

    @Column(name = "quantity_unit", nullable = false, length = 10)
    @Builder.Default
    private String quantityUnit = "KG";

    @Column(name = "worker_count")
    @Builder.Default
    private Integer workerCount = 0;

    @Column(name = "total_working_hours", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal totalWorkingHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToMany
    @JoinTable(
        name = "session_workers",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "worker_id")
    )
    @Builder.Default
    private Set<Worker> workers = new HashSet<>();

    public enum SessionStatus {
        PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
