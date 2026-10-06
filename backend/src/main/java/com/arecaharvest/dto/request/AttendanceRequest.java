package com.arecaharvest.dto.request;

import com.arecaharvest.entity.Attendance.AttendanceStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
public class AttendanceRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;

    @NotNull(message = "Field ID is required")
    private Long fieldId;

    private Long contractId;
    private Long harvestSessionId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    @DecimalMin(value = "0.0") @DecimalMax(value = "24.0")
    private BigDecimal workingHours;

    @DecimalMin(value = "0.0") @DecimalMax(value = "8.0")
    private BigDecimal overtimeHours;

    /** If null, the worker's current daily wage is used */
    @DecimalMin(value = "0.0")
    private BigDecimal dailyWageOverride;

    @DecimalMin(value = "0.0")
    private BigDecimal overtimeRate;

    private String remarks;
}
