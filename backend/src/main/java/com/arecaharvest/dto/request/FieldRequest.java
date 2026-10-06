package com.arecaharvest.dto.request;

import com.arecaharvest.entity.enums.FieldStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class FieldRequest {

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotBlank(message = "Field name is required")
    @Size(max = 200)
    private String fieldName;

    private String address;

    @Size(max = 200)
    private String village;

    @Size(max = 200)
    private String district;

    private String state;

    @DecimalMin(value = "0.0001", message = "Acreage must be positive")
    @Digits(integer = 6, fraction = 4)
    private BigDecimal acreage;

    @Min(value = 1, message = "Tree count must be at least 1")
    private Integer treeCount;

    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private BigDecimal gpsLatitude;

    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private BigDecimal gpsLongitude;

    @DecimalMin(value = "0.0", message = "Expected yield cannot be negative")
    private BigDecimal expectedYieldKg;

    private FieldStatus status;
    private String notes;
}
