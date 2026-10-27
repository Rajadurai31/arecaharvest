package com.arecaharvest.dto.request;

import com.arecaharvest.entity.enums.ContractStatus;
import com.arecaharvest.entity.enums.ContractType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class ContractRequest {

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotNull(message = "Field ID is required")
    private Long fieldId;

    @NotBlank(message = "Contract number is required")
    @Size(max = 50)
    private String contractNumber;

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    @DecimalMin(value = "0.0", message = "Rate cannot be negative")
    private BigDecimal rate;

    /** Required only when contractType = FIXED_AMOUNT */
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal fixedAmount;

    @DecimalMin(value = "0.0", message = "Expected quantity cannot be negative")
    private BigDecimal expectedQuantityKg;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate expectedEndDate;

    private ContractStatus status;

    private Long supervisorId;

    private String notes;
}
