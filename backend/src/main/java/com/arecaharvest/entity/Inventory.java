package com.arecaharvest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventory")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_record_id")
    private HarvestRecord harvestRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private HarvestingContract contract;

    @Column(name = "quantity_received", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityReceived;

    @Column(name = "quantity_unit", nullable = false, length = 10)
    @Builder.Default
    private String quantityUnit = "KG";

    @Column(name = "quantity_sold", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal quantitySold = BigDecimal.ZERO;

    /** Redundantly stored for fast reads — kept in sync via @Transactional service */
    @Column(name = "available_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal availableQuantity;

    @Column(name = "storage_location", length = 200)
    private String storageLocation;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum InventoryStatus {
        AVAILABLE, PARTIALLY_SOLD, SOLD, DAMAGED
    }
}
