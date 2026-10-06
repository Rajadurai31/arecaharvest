package com.arecaharvest.repository;

import com.arecaharvest.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @Query("""
        SELECT e FROM Equipment e
        WHERE (:category IS NULL OR e.category = :category)
          AND (:status IS NULL OR e.status = :status)
        """)
    Page<Equipment> searchEquipment(@Param("category") String category,
                                    @Param("status") String status,
                                    Pageable pageable);

    @Query("SELECT e FROM Equipment e WHERE e.nextMaintenanceDate <= :threshold AND e.status != 'RETIRED'")
    List<Equipment> findEquipmentDueForMaintenance(@Param("threshold") LocalDate threshold);
}
