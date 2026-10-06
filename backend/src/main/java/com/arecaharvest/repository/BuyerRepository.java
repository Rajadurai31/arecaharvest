package com.arecaharvest.repository;

import com.arecaharvest.entity.Buyer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    @Query("""
        SELECT b FROM Buyer b
        WHERE (:search IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))
                               OR b.phone LIKE CONCAT('%', :search, '%'))
          AND (:status IS NULL OR b.status = :status)
        """)
    Page<Buyer> searchBuyers(@Param("search") String search,
                             @Param("status") String status,
                             Pageable pageable);
}
