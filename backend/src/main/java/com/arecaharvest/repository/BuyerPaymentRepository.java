package com.arecaharvest.repository;

import com.arecaharvest.entity.BuyerPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface BuyerPaymentRepository extends JpaRepository<BuyerPayment, Long> {

    Page<BuyerPayment> findByBuyerIdOrderByPaymentDateDesc(Long buyerId, Pageable pageable);

    /**
     * Outstanding = sum(SALE) − sum(PAYMENT) ± sum(ADJUSTMENT × sign)
     */
    @Query("""
        SELECT COALESCE(SUM(CASE
            WHEN CAST(bp.transactionType AS string) = 'SALE'       THEN bp.amount
            WHEN CAST(bp.transactionType AS string) = 'PAYMENT'    THEN -bp.amount
            WHEN CAST(bp.transactionType AS string) = 'ADJUSTMENT' THEN bp.amount * bp.adjustmentSign
            ELSE 0
        END), 0)
        FROM BuyerPayment bp WHERE bp.buyer.id = :buyerId
        """)
    BigDecimal calculateOutstandingBalance(@Param("buyerId") Long buyerId);
}
