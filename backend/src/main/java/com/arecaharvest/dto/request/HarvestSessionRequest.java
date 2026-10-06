package com.arecaharvest.dto.request;

import com.arecaharvest.entity.HarvestSession.SessionStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter @Setter
public class HarvestSessionRequest {

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotNull(message = "Field ID is required")
    private Long fieldId;

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    private Long supervisorId;
    private LocalTime startTime;
    private LocalTime endTime;

    @Min(0) private Integer totalBags;

    @DecimalMin("0.0") private BigDecimal totalQuantity;

    private String quantityUnit = "KG";

    private SessionStatus status;
    private String remarks;
    private Set<Long> workerIds;
}
