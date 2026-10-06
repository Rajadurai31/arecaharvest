package com.arecaharvest.dto.request;

import com.arecaharvest.entity.WorkerPayment.PaymentType;
import com.arecaharvest.entity.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class WorkerPaymentRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Payment amount must be at least ₹1")
    private BigDecimal amount;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String referenceNumber;
    private String remarks;
}
