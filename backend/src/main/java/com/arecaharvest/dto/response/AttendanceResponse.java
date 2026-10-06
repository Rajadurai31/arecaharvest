package com.arecaharvest.dto.response;

import com.arecaharvest.entity.Attendance.AttendanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class AttendanceResponse {
    private Long id;
    private Long workerId;
    private String workerName;
    private Long fieldId;
    private String fieldName;
    private Long contractId;
    private Long harvestSessionId;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private BigDecimal workingHours;
    private BigDecimal overtimeHours;
    private BigDecimal dailyWageApplied;
    private BigDecimal overtimeRate;
    private BigDecimal wageForDay;   // computed: (dailyWage × factor) + (overtime × rate)
    private String remarks;
    private String markedByName;
    private LocalDateTime createdAt;
}
