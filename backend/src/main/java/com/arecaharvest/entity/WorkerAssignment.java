package com.arecaharvest.entity;

import com.arecaharvest.entity.enums.SkillType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "worker_assignments",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_assignment_worker_field_date",
        columnNames = {"worker_id", "field_id", "assigned_date"}
    ))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WorkerAssignment extends BaseEntity {

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

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_role", length = 20)
    private SkillType assignedRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

    public enum AssignmentStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}
