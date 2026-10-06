package com.arecaharvest.dto.response;

import com.arecaharvest.entity.WorkerPayment.PaymentType;
import com.arecaharvest.entity.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class WorkerPaymentResponse {
    private Long id;
    private Long workerId;
    private String workerName;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String remarks;
    private LocalDateTime createdAt;
}
