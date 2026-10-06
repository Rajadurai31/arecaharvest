package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_attendance_worker_field_date",
        columnNames = {"worker_id", "field_id", "attendance_date"}
    ))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private HarvestingContract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_session_id")
    private HarvestSession harvestSession;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    /** Phase 2: working hours time series for productivity modelling */
    @Column(name = "working_hours", precision = 5, scale = 2)
    private BigDecimal workingHours;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    /** Snapshot of wage at time of attendance — immutable after creation */
    @Column(name = "daily_wage_applied", precision = 10, scale = 2)
    private BigDecimal dailyWageApplied;

    @Column(name = "overtime_rate", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overtimeRate = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private User markedBy;

    public enum AttendanceStatus {
        PRESENT, HALF_DAY, ABSENT, LEAVE
    }
}
