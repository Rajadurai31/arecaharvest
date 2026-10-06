package com.arecaharvest.dto.response;

import com.arecaharvest.entity.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OwnerResponse {
    private Long id;
    private String name;
    private String phone;
    private String alternatePhone;
    private String email;
    private String address;
    private String village;
    private String district;
    private String state;
    private String pincode;
    private PaymentMethod paymentPreference;
    private String bankName;
    private String bankAccountNumber;
    private String bankIfsc;
    private String upiId;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Summary stats (populated in detail view)
    private Long totalFields;
    private Long activeContracts;
    private Long completedContracts;
}
