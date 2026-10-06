package com.arecaharvest.dto.response;

import com.arecaharvest.entity.enums.SkillType;
import com.arecaharvest.entity.enums.WorkerStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class WorkerResponse {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String alternatePhone;
    private String address;
    private String village;
    private String district;
    private String state;
    private SkillType skillType;
    private BigDecimal experienceYears;
    private BigDecimal dailyWage;
    private LocalDate joiningDate;
    private WorkerStatus status;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Ledger summary (populated on demand)
    private BigDecimal totalEarned;
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;
}
