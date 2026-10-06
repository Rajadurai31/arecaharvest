package com.arecaharvest.repository;

import com.arecaharvest.entity.Field;
import com.arecaharvest.entity.enums.FieldStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {

    List<Field> findByOwnerIdAndStatusNot(Long ownerId, FieldStatus status);

    Page<Field> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("""
        SELECT f FROM Field f
        WHERE (:ownerId IS NULL OR f.owner.id = :ownerId)
          AND (:status IS NULL OR f.status = :status)
          AND (:search IS NULL OR LOWER(f.fieldName) LIKE LOWER(CONCAT('%', :search, '%'))
                               OR LOWER(f.village) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Field> searchFields(@Param("ownerId") Long ownerId,
                             @Param("status") FieldStatus status,
                             @Param("search") String search,
                             Pageable pageable);

    long countByOwnerId(Long ownerId);

    long countByStatus(FieldStatus status);
}
