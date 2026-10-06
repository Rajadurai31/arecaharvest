package com.arecaharvest.dto.request;

import com.arecaharvest.entity.BuyerPayment.TransactionType;
import com.arecaharvest.entity.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class BuyerPaymentRequest {

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;

    private Long saleId;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private Integer adjustmentSign = 1;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String remarks;
}
