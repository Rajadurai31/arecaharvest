package com.arecaharvest.dto.request;

import com.arecaharvest.entity.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OwnerRequest {

    @NotBlank(message = "Owner name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String phone;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String alternatePhone;

    @Email(message = "Invalid email format")
    private String email;

    private String address;

    @Size(max = 200)
    private String village;

    @Size(max = 200)
    private String district;

    @Size(max = 100)
    private String state;

    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    private PaymentMethod paymentPreference;

    private String bankName;
    private String bankAccountNumber;

    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    private String bankIfsc;

    private String upiId;
    private String notes;
    private Boolean active;
}
