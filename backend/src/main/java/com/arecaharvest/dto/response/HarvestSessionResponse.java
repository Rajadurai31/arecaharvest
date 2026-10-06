package com.arecaharvest.dto.response;

import com.arecaharvest.entity.HarvestSession.SessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class HarvestSessionResponse {
    private Long id;
    private LocalDate sessionDate;
    private Long fieldId;
    private String fieldName;
    private Long contractId;
    private String contractNumber;
    private Long supervisorId;
    private String supervisorName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalBags;
    private BigDecimal totalQuantity;
    private String quantityUnit;
    private Integer workerCount;
    private BigDecimal totalWorkingHours;
    private SessionStatus status;
    private String remarks;
    private List<String> workerNames;
    private LocalDateTime createdAt;
}
