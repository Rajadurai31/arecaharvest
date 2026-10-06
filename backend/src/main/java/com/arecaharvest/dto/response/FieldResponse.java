package com.arecaharvest.dto.response;

import com.arecaharvest.entity.enums.FieldStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class FieldResponse {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String fieldName;
    private String address;
    private String village;
    private String district;
    private String state;
    private BigDecimal acreage;
    private Integer treeCount;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;
    private BigDecimal expectedYieldKg;
    private FieldStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Progress stats (populated in detail view)
    private BigDecimal harvestedQuantityKg;
    private BigDecimal remainingQuantityKg;
    private Double progressPercent;
    private Long activeContractId;
}
