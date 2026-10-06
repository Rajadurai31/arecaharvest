package com.arecaharvest.dto.response;

import com.arecaharvest.entity.enums.ContractStatus;
import com.arecaharvest.entity.enums.ContractType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ContractResponse {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private Long fieldId;
    private String fieldName;
    private String contractNumber;
    private ContractType contractType;
    private BigDecimal rate;
    private BigDecimal fixedAmount;
    private BigDecimal expectedQuantityKg;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private ContractStatus status;
    private Long supervisorId;
    private String supervisorName;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private BigDecimal expectedRevenue;
    private BigDecimal actualHarvestedKg;
    private Double progressPercent;
}
