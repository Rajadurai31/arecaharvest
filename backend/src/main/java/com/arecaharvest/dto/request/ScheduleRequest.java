package com.arecaharvest.dto.request;

import com.arecaharvest.entity.Schedule.ScheduleStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter @Setter
public class ScheduleRequest {

    @NotNull(message = "Schedule date is required")
    private LocalDate scheduleDate;

    @NotNull(message = "Field ID is required")
    private Long fieldId;

    private Long contractId;
    private Long supervisorId;
    private Long vehicleId;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;

    @DecimalMin("0.0")
    private BigDecimal expectedQuantityKg;

    @Min(0)
    private Integer expectedWorkerCount;

    private String notes;
    private ScheduleStatus status;
    private Set<Long> workerIds;
}
