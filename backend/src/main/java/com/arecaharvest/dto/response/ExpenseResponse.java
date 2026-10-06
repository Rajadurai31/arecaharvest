package com.arecaharvest.dto.response;

import com.arecaharvest.entity.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ExpenseResponse {
    private Long id;
    private LocalDate expenseDate;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String description;
    private Long fieldId;
    private String fieldName;
    private Long contractId;
    private Long vehicleId;
    private String receiptUrl;
    private LocalDateTime createdAt;
}
