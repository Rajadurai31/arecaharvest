package com.arecaharvest.repository;

import com.arecaharvest.entity.HarvestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface HarvestRecordRepository extends JpaRepository<HarvestRecord, Long> {

    @Query("""
        SELECT hr FROM HarvestRecord hr
        WHERE (:fieldId IS NULL OR hr.field.id = :fieldId)
          AND (:contractId IS NULL OR hr.contract.id = :contractId)
          AND (:fromDate IS NULL OR hr.recordDate >= :fromDate)
          AND (:toDate IS NULL OR hr.recordDate <= :toDate)
          AND hr.amendment = false
        """)
    Page<HarvestRecord> searchRecords(@Param("fieldId") Long fieldId,
                                      @Param("contractId") Long contractId,
                                      @Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate,
                                      Pageable pageable);

    @Query("SELECT COALESCE(SUM(hr.quantity),0) FROM HarvestRecord hr WHERE hr.contract.id = :contractId AND hr.amendment = false")
    BigDecimal sumQuantityForContract(@Param("contractId") Long contractId);

    @Query("SELECT COALESCE(SUM(hr.quantity),0) FROM HarvestRecord hr WHERE hr.field.id = :fieldId AND hr.amendment = false AND hr.recordDate BETWEEN :from AND :to")
    BigDecimal sumQuantityForFieldPeriod(@Param("fieldId") Long fieldId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);
}
