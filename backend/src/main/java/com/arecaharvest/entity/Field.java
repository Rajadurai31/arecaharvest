package com.arecaharvest.entity;

import com.arecaharvest.entity.enums.FieldStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fields")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Field extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(name = "field_name", nullable = false, length = 200)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 200)
    private String village;

    @Column(length = 200)
    private String district;

    @Column(length = 100)
    @Builder.Default
    private String state = "Karnataka";

    /** Area in acres — critical Phase 2 yield modelling feature */
    @Column(precision = 10, scale = 4)
    private BigDecimal acreage;

    @Column(name = "tree_count")
    private Integer treeCount;

    @Column(name = "gps_latitude", precision = 10, scale = 7)
    private BigDecimal gpsLatitude;

    @Column(name = "gps_longitude", precision = 10, scale = 7)
    private BigDecimal gpsLongitude;

    @Column(name = "expected_yield_kg", precision = 12, scale = 3)
    private BigDecimal expectedYieldKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FieldStatus status = FieldStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HarvestingContract> contracts = new ArrayList<>();
}
