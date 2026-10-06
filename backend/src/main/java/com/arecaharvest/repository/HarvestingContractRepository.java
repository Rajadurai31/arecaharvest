package com.arecaharvest.repository;

import com.arecaharvest.entity.HarvestingContract;
import com.arecaharvest.entity.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HarvestingContractRepository extends JpaRepository<HarvestingContract, Long> {

    Optional<HarvestingContract> findByContractNumber(String contractNumber);

    boolean existsByContractNumber(String contractNumber);

    List<HarvestingContract> findByFieldIdAndStatus(Long fieldId, ContractStatus status);

    boolean existsByFieldIdAndStatus(Long fieldId, ContractStatus status);

    @Query("""
        SELECT c FROM HarvestingContract c
        WHERE (:fieldId IS NULL OR c.field.id = :fieldId)
          AND (:ownerId IS NULL OR c.owner.id = :ownerId)
          AND (:status IS NULL OR c.status = :status)
          AND (:fromDate IS NULL OR c.startDate >= :fromDate)
          AND (:toDate IS NULL OR c.startDate <= :toDate)
        """)
    Page<HarvestingContract> searchContracts(@Param("fieldId") Long fieldId,
                                             @Param("ownerId") Long ownerId,
                                             @Param("status") ContractStatus status,
                                             @Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate,
                                             Pageable pageable);

    /** Contracts nearing expected end date (for notifications) */
    @Query("SELECT c FROM HarvestingContract c WHERE c.status = 'ACTIVE' AND c.expectedEndDate <= :threshold")
    List<HarvestingContract> findActiveContractsNearingCompletion(@Param("threshold") LocalDate threshold);

    long countByStatus(ContractStatus status);
}
