package com.arecaharvest.dto.request;

import com.arecaharvest.entity.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class ExpenseRequest {

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Description is required")
    private String description;

    private Long fieldId;
    private Long contractId;
    private Long vehicleId;
    private String receiptUrl;
}
