package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "schedules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private HarvestingContract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "planned_start_time")
    private LocalTime plannedStartTime;

    @Column(name = "planned_end_time")
    private LocalTime plannedEndTime;

    @Column(name = "expected_quantity_kg", precision = 12, scale = 3)
    private BigDecimal expectedQuantityKg;

    @Column(name = "expected_worker_count")
    private Integer expectedWorkerCount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.PLANNED;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_session_id")
    private HarvestSession harvestSession;

    @ManyToMany
    @JoinTable(
        name = "schedule_workers",
        joinColumns = @JoinColumn(name = "schedule_id"),
        inverseJoinColumns = @JoinColumn(name = "worker_id")
    )
    @Builder.Default
    private Set<Worker> workers = new HashSet<>();

    public enum ScheduleStatus {
        PLANNED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
