package com.arecaharvest.repository;

import com.arecaharvest.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByRegistrationNumber(String registrationNumber);

    @Query("""
        SELECT v FROM Vehicle v
        WHERE (:status IS NULL OR v.status = :status)
          AND (:type IS NULL OR v.vehicleType = :type)
        """)
    Page<Vehicle> searchVehicles(@Param("status") String status,
                                 @Param("type") String type,
                                 Pageable pageable);
}
