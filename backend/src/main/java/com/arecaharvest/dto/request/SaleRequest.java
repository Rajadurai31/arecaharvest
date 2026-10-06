package com.arecaharvest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class SaleRequest {

    @NotBlank(message = "Sale number is required")
    private String saleNumber;

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;

    @NotNull(message = "Inventory ID is required")
    private Long inventoryId;

    @NotNull(message = "Sale date is required")
    private LocalDate saleDate;

    @NotNull @DecimalMin(value = "0.001", message = "Quantity must be positive")
    private BigDecimal quantityKg;

    private String quantityUnit = "KG";

    @NotNull @DecimalMin(value = "0.01", message = "Rate must be positive")
    private BigDecimal ratePerUnit;

    @DecimalMin("0.0") private BigDecimal transportCost;
    @DecimalMin("0.0") private BigDecimal loadingCost;
    @DecimalMin("0.0") private BigDecimal otherCharges;
    private String otherChargesDesc;
    private String notes;
}
