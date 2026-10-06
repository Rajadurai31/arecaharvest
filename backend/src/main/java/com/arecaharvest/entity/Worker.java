package com.arecaharvest.entity;

import com.arecaharvest.entity.enums.SkillType;
import com.arecaharvest.entity.enums.WorkerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "workers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Worker extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optional link to a system user account */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 200)
    private String village;

    @Column(length = 200)
    private String district;

    @Column(length = 100)
    @Builder.Default
    private String state = "Karnataka";

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", nullable = false, length = 20)
    @Builder.Default
    private SkillType skillType = SkillType.HELPER;

    /** Phase 2 feature variable: experience drives productivity predictions */
    @Column(name = "experience_years", precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal experienceYears = BigDecimal.ZERO;

    @Column(name = "daily_wage", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyWage;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WorkerStatus status = WorkerStatus.ACTIVE;

    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
