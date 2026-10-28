package com.arecaharvest.repository;

import com.arecaharvest.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByScheduleDateOrderByPlannedStartTimeAsc(LocalDate date);

    @Query("""
        SELECT s FROM Schedule s
        WHERE (:fieldId IS NULL OR s.field.id = :fieldId)
          AND (:status IS NULL OR CAST(s.status AS string) = :status)
          AND (:fromDate IS NULL OR s.scheduleDate >= :fromDate)
          AND (:toDate IS NULL OR s.scheduleDate <= :toDate)
        ORDER BY s.scheduleDate ASC, s.plannedStartTime ASC
        """)
    Page<Schedule> searchSchedules(@Param("fieldId") Long fieldId,
                                   @Param("status") String status,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate,
                                   Pageable pageable);

    /** Check for worker scheduling conflict on same date */
    @Query("""
        SELECT COUNT(sw.id) > 0 FROM Schedule s JOIN s.workers sw
        WHERE sw.id = :workerId AND s.scheduleDate = :date
          AND CAST(s.status AS string) NOT IN ('CANCELLED','COMPLETED')
          AND (:excludeId IS NULL OR s.id != :excludeId)
        """)
    boolean hasWorkerConflict(@Param("workerId") Long workerId,
                              @Param("date") LocalDate date,
                              @Param("excludeId") Long excludeId);
}
