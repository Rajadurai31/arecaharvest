package com.arecaharvest.repository;

import com.arecaharvest.entity.Worker;
import com.arecaharvest.entity.enums.SkillType;
import com.arecaharvest.entity.enums.WorkerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {

    @Query("""
        SELECT w FROM Worker w
        WHERE (:search IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
                               OR w.phone LIKE CONCAT('%', :search, '%'))
          AND (:status IS NULL OR w.status = :status)
          AND (:skillType IS NULL OR w.skillType = :skillType)
        """)
    Page<Worker> searchWorkers(@Param("search") String search,
                               @Param("status") WorkerStatus status,
                               @Param("skillType") SkillType skillType,
                               Pageable pageable);

    boolean existsByPhone(String phone);

    long countByStatus(WorkerStatus status);
}
