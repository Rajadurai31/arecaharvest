package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_number", nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(length = 100)
    private String make;

    @Column(length = 100)
    private String model;

    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Worker driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    @Builder.Default
    private FuelType fuelType = FuelType.DIESEL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum VehicleType { TRUCK, TRACTOR, PICKUP, AUTO, BIKE, OTHER }
    public enum FuelType { PETROL, DIESEL, ELECTRIC, OTHER }
    public enum VehicleStatus { ACTIVE, INACTIVE, MAINTENANCE }
}
