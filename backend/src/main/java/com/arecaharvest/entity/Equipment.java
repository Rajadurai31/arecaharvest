package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Equipment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EquipmentCategory category;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", nullable = false, length = 20)
    @Builder.Default
    private ConditionStatus conditionStatus = ConditionStatus.GOOD;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 10, scale = 2)
    private BigDecimal purchaseCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_worker_id")
    private Worker assignedWorker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_field_id")
    private Field assignedField;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum EquipmentCategory {
        LADDER, ROPE, CUTTER, HARVESTING_MACHINE, BAGS, SAFETY_EQUIPMENT, TARPAULIN, OTHER
    }

    public enum ConditionStatus { GOOD, FAIR, POOR, DAMAGED }

    public enum EquipmentStatus { AVAILABLE, IN_USE, MAINTENANCE, RETIRED }
}
