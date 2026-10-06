package com.arecaharvest.repository;

import com.arecaharvest.entity.WorkerPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface WorkerPaymentRepository extends JpaRepository<WorkerPayment, Long> {

    Page<WorkerPayment> findByWorkerId(Long workerId, Pageable pageable);

    @Query("""
        SELECT wp FROM WorkerPayment wp
        WHERE (:workerId IS NULL OR wp.worker.id = :workerId)
          AND (:fromDate IS NULL OR wp.paymentDate >= :fromDate)
          AND (:toDate IS NULL OR wp.paymentDate <= :toDate)
        """)
    Page<WorkerPayment> searchPayments(@Param("workerId") Long workerId,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate,
                                       Pageable pageable);

    @Query("SELECT COALESCE(SUM(wp.amount),0) FROM WorkerPayment wp WHERE wp.paymentDate BETWEEN :from AND :to")
    BigDecimal sumPaymentsForPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
