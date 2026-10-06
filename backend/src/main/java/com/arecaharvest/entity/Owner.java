package com.arecaharvest.entity;

import com.arecaharvest.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "owners")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Owner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(length = 255)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 200)
    private String village;

    @Column(length = 200)
    private String district;

    @Column(length = 100)
    @Builder.Default
    private String state = "Karnataka";

    @Column(length = 10)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_preference", length = 30)
    @Builder.Default
    private PaymentMethod paymentPreference = PaymentMethod.CASH;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc", length = 20)
    private String bankIfsc;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Field> fields = new ArrayList<>();
}
