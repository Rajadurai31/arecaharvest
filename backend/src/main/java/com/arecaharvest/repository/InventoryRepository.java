package com.arecaharvest.repository;

import com.arecaharvest.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("""
        SELECT i FROM Inventory i
        WHERE (:fieldId IS NULL OR i.field.id = :fieldId)
          AND (:status IS NULL OR CAST(i.status AS string) = :status)
        """)
    Page<Inventory> searchInventory(@Param("fieldId") Long fieldId,
                                    @Param("status") String status,
                                    Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.availableQuantity),0) FROM Inventory i WHERE i.status IN ('AVAILABLE','PARTIALLY_SOLD')")
    BigDecimal totalAvailableQuantity();
}
