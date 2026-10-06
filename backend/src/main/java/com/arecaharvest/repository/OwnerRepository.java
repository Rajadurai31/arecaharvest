package com.arecaharvest.repository;

import com.arecaharvest.entity.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Page<Owner> findByActiveTrue(Pageable pageable);

    @Query("""
        SELECT o FROM Owner o
        WHERE (:search IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%'))
                              OR o.phone LIKE CONCAT('%', :search, '%')
                              OR LOWER(o.village) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:active IS NULL OR o.active = :active)
        """)
    Page<Owner> searchOwners(@Param("search") String search,
                             @Param("active") Boolean active,
                             Pageable pageable);

    boolean existsByPhone(String phone);
}
