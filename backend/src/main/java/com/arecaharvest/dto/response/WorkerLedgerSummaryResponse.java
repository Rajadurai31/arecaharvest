package com.arecaharvest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class WorkerLedgerSummaryResponse {
    private Long workerId;
    private String workerName;
    private BigDecimal totalEarned;
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;
    private List<WageLedgerResponse> transactions;
}
