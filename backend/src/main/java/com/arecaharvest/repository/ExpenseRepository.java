package com.arecaharvest.repository;

import com.arecaharvest.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
        SELECT e FROM Expense e
        WHERE (:categoryId IS NULL OR e.category.id = :categoryId)
          AND (:fieldId IS NULL OR e.field.id = :fieldId)
          AND (:fromDate IS NULL OR e.expenseDate >= :fromDate)
          AND (:toDate IS NULL OR e.expenseDate <= :toDate)
        """)
    Page<Expense> searchExpenses(@Param("categoryId") Long categoryId,
                                 @Param("fieldId") Long fieldId,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate,
                                 Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumExpensesForPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.expenseDate = :date")
    BigDecimal sumExpensesForDate(@Param("date") LocalDate date);
}
