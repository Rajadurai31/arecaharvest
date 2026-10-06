package com.arecaharvest.repository;

import com.arecaharvest.entity.WorkerAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, Long> {

    boolean existsByWorkerIdAndFieldIdAndAssignedDate(Long workerId, Long fieldId, LocalDate date);

    List<WorkerAssignment> findByFieldIdAndAssignedDate(Long fieldId, LocalDate date);

    @Query("""
        SELECT wa FROM WorkerAssignment wa
        WHERE (:workerId IS NULL OR wa.worker.id = :workerId)
          AND (:fieldId IS NULL OR wa.field.id = :fieldId)
          AND (:fromDate IS NULL OR wa.assignedDate >= :fromDate)
          AND (:toDate IS NULL OR wa.assignedDate <= :toDate)
        """)
    Page<WorkerAssignment> searchAssignments(@Param("workerId") Long workerId,
                                             @Param("fieldId") Long fieldId,
                                             @Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate,
                                             Pageable pageable);
}
