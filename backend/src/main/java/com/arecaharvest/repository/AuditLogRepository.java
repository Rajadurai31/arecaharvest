package com.arecaharvest.repository;

import com.arecaharvest.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:entityType IS NULL OR a.entityType = :entityType)
          AND (:entityId IS NULL OR a.entityId = :entityId)
          AND (:action IS NULL OR a.action = :action)
          AND (:fromDate IS NULL OR a.createdAt >= :fromDate)
          AND (:toDate IS NULL OR a.createdAt <= :toDate)
        ORDER BY a.createdAt DESC
        """)
    Page<AuditLog> searchLogs(@Param("entityType") String entityType,
                               @Param("entityId") Long entityId,
                               @Param("action") String action,
                               @Param("fromDate") LocalDateTime fromDate,
                               @Param("toDate") LocalDateTime toDate,
                               Pageable pageable);
}
