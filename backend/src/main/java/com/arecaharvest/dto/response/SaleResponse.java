package com.arecaharvest.dto.response;

import com.arecaharvest.entity.Sale.SalePaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SaleResponse {
    private Long id;
    private String saleNumber;
    private Long buyerId;
    private String buyerName;
    private Long inventoryId;
    private LocalDate saleDate;
    private BigDecimal quantityKg;
    private String quantityUnit;
    private BigDecimal ratePerUnit;
    private BigDecimal grossAmount;
    private BigDecimal transportCost;
    private BigDecimal loadingCost;
    private BigDecimal otherCharges;
    private String otherChargesDesc;
    private BigDecimal netAmount;
    private SalePaymentStatus paymentStatus;
    private BigDecimal amountReceived;
    private BigDecimal outstanding;
    private String notes;
    private LocalDateTime createdAt;
}
