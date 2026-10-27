package com.arecaharvest.repository;

import com.arecaharvest.entity.HarvestSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HarvestSessionRepository extends JpaRepository<HarvestSession, Long> {

    List<HarvestSession> findBySessionDate(LocalDate date);

    @Query("""
        SELECT hs FROM HarvestSession hs
        WHERE (:fieldId IS NULL OR hs.field.id = :fieldId)
          AND (:contractId IS NULL OR hs.contract.id = :contractId)
          AND (:status IS NULL OR CAST(hs.status AS string) = :status)
          AND (:fromDate IS NULL OR hs.sessionDate >= :fromDate)
          AND (:toDate IS NULL OR hs.sessionDate <= :toDate)
        """)
    Page<HarvestSession> searchSessions(@Param("fieldId") Long fieldId,
                                        @Param("contractId") Long contractId,
                                        @Param("status") String status,
                                        @Param("fromDate") LocalDate fromDate,
                                        @Param("toDate") LocalDate toDate,
                                        Pageable pageable);

    @Query("SELECT COALESCE(SUM(hs.totalQuantity),0) FROM HarvestSession hs WHERE hs.sessionDate = :date")
    BigDecimal sumQuantityForDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(hs.totalQuantity),0) FROM HarvestSession hs WHERE hs.sessionDate BETWEEN :from AND :to")
    BigDecimal sumQuantityForPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(hs.totalQuantity),0) FROM HarvestSession hs WHERE hs.contract.id = :contractId")
    BigDecimal sumQuantityForContract(@Param("contractId") Long contractId);

    /** Field-specific harvest total — fixes FieldService progress calculation */
    @Query("SELECT COALESCE(SUM(hs.totalQuantity),0) FROM HarvestSession hs WHERE hs.field.id = :fieldId AND hs.status = 'COMPLETED'")
    BigDecimal sumQuantityForField(@Param("fieldId") Long fieldId);

    /** Field-wise harvest totals for dashboard chart */
    @Query("""
        SELECT hs.field.id, hs.field.fieldName, COALESCE(SUM(hs.totalQuantity),0)
        FROM HarvestSession hs
        WHERE hs.status = 'COMPLETED'
          AND hs.sessionDate BETWEEN :from AND :to
        GROUP BY hs.field.id, hs.field.fieldName
        ORDER BY SUM(hs.totalQuantity) DESC
        """)
    List<Object[]> fieldWiseHarvestTotals(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
