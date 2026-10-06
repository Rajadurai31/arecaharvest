package com.arecaharvest.dto.request;

import com.arecaharvest.entity.enums.SkillType;
import com.arecaharvest.entity.enums.WorkerStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class WorkerRequest {

    private Long userId;

    @NotBlank(message = "Worker name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String phone;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String alternatePhone;

    private String address;

    @Size(max = 200)
    private String village;

    @Size(max = 200)
    private String district;

    private String state;

    @NotNull(message = "Skill type is required")
    private SkillType skillType;

    @DecimalMin(value = "0.0", message = "Experience cannot be negative")
    @Digits(integer = 2, fraction = 1)
    private BigDecimal experienceYears;

    @NotNull(message = "Daily wage is required")
    @DecimalMin(value = "1.0", message = "Daily wage must be at least ₹1")
    private BigDecimal dailyWage;

    private LocalDate joiningDate;

    private WorkerStatus status;

    private String emergencyContactName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String emergencyContactPhone;

    private String notes;
}
