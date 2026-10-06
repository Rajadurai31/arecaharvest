package com.arecaharvest.repository;

import com.arecaharvest.entity.WageLedger;
import com.arecaharvest.entity.WageLedger.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WageLedgerRepository extends JpaRepository<WageLedger, Long> {

    List<WageLedger> findByWorkerIdOrderByTransactionDateDesc(Long workerId);

    Page<WageLedger> findByWorkerId(Long workerId, Pageable pageable);

    /**
     * Calculates outstanding balance for a worker.
     * DEBIT types: DAILY_WAGE, OVERTIME, BONUS → positive (owed to worker)
     * CREDIT types: ADVANCE, PAYMENT → negative (paid to worker)
     * ADJUSTMENT: adjustmentSign determines direction
     */
    @Query("""
        SELECT
          COALESCE(SUM(CASE
            WHEN l.transactionType IN ('DAILY_WAGE','OVERTIME','BONUS') THEN l.amount
            WHEN l.transactionType IN ('ADVANCE','PAYMENT') THEN -l.amount
            WHEN l.transactionType = 'ADJUSTMENT' THEN l.amount * l.adjustmentSign
            ELSE 0
          END), 0)
        FROM WageLedger l
        WHERE l.worker.id = :workerId
        """)
    BigDecimal calculateOutstandingBalance(@Param("workerId") Long workerId);

    @Query("""
        SELECT COALESCE(SUM(l.amount), 0) FROM WageLedger l
        WHERE l.worker.id = :workerId AND l.transactionType IN ('DAILY_WAGE','OVERTIME','BONUS')
        """)
    BigDecimal calculateTotalEarned(@Param("workerId") Long workerId);

    @Query("""
        SELECT COALESCE(SUM(l.amount), 0) FROM WageLedger l
        WHERE l.worker.id = :workerId AND l.transactionType IN ('ADVANCE','PAYMENT')
        """)
    BigDecimal calculateTotalPaid(@Param("workerId") Long workerId);

    @Query("""
        SELECT COALESCE(SUM(CASE
            WHEN l.transactionType IN ('DAILY_WAGE','OVERTIME','BONUS') THEN l.amount
            ELSE 0 END), 0)
        FROM WageLedger l
        WHERE l.transactionType IN ('DAILY_WAGE','OVERTIME','BONUS')
          AND l.transactionDate >= :fromDate AND l.transactionDate <= :toDate
        """)
    BigDecimal sumWagesForPeriod(@Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate);

    /**
     * Sum of all positive outstanding balances across ALL workers.
     * Uses native SQL for the subquery aggregation which JPQL doesn't support well.
     */
    @Query(value = """
        SELECT COALESCE(SUM(CASE WHEN balance > 0 THEN balance ELSE 0 END), 0)
        FROM (
            SELECT SUM(CASE
                WHEN transaction_type IN ('DAILY_WAGE','OVERTIME','BONUS') THEN amount
                WHEN transaction_type IN ('ADVANCE','PAYMENT') THEN -amount
                WHEN transaction_type = 'ADJUSTMENT' THEN amount * adjustment_sign
                ELSE 0
            END) AS balance
            FROM wage_ledger
            GROUP BY worker_id
        ) t
        """, nativeQuery = true)
    BigDecimal sumAllPositiveOutstandingBalances();
}
