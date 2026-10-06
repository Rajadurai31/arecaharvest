package com.arecaharvest.dto.response;

import com.arecaharvest.entity.WageLedger.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class WageLedgerResponse {
    private Long id;
    private Long workerId;
    private String workerName;
    private TransactionType transactionType;
    private BigDecimal amount;
    private Integer adjustmentSign;
    private LocalDate transactionDate;
    private Long attendanceId;
    private Long paymentId;
    private String fieldName;
    private String description;
    private LocalDateTime createdAt;
}
