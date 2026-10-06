package com.arecaharvest.repository;

import com.arecaharvest.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    boolean existsBySaleNumber(String saleNumber);

    @Query("""
        SELECT s FROM Sale s
        WHERE (:buyerId IS NULL OR s.buyer.id = :buyerId)
          AND (:status IS NULL OR s.paymentStatus = :status)
          AND (:fromDate IS NULL OR s.saleDate >= :fromDate)
          AND (:toDate IS NULL OR s.saleDate <= :toDate)
        """)
    Page<Sale> searchSales(@Param("buyerId") Long buyerId,
                           @Param("status") String status,
                           @Param("fromDate") LocalDate fromDate,
                           @Param("toDate") LocalDate toDate,
                           Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.netAmount),0) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to")
    BigDecimal sumRevenueForPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(s.netAmount),0) FROM Sale s WHERE s.saleDate = :date")
    BigDecimal sumRevenueForDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.netAmount - s.amountReceived),0) FROM Sale s WHERE s.paymentStatus IN ('PENDING','PARTIAL')")
    BigDecimal totalBuyerOutstanding();

    @Query("SELECT COALESCE(SUM(s.quantityKg),0) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to")
    BigDecimal sumQuantitySoldForPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
