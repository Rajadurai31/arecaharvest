package com.arecaharvest.repository;

import com.arecaharvest.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByWorkerIdAndFieldIdAndAttendanceDate(
            Long workerId, Long fieldId, LocalDate date);

    boolean existsByWorkerIdAndFieldIdAndAttendanceDate(
            Long workerId, Long fieldId, LocalDate date);

    List<Attendance> findByFieldIdAndAttendanceDate(Long fieldId, LocalDate date);

    List<Attendance> findByAttendanceDate(LocalDate date);

    @Query("""
        SELECT a FROM Attendance a
        WHERE (:workerId IS NULL OR a.worker.id = :workerId)
          AND (:fieldId IS NULL OR a.field.id = :fieldId)
          AND (:fromDate IS NULL OR a.attendanceDate >= :fromDate)
          AND (:toDate IS NULL OR a.attendanceDate <= :toDate)
        """)
    Page<Attendance> searchAttendance(@Param("workerId") Long workerId,
                                      @Param("fieldId") Long fieldId,
                                      @Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate,
                                      Pageable pageable);

    @Query("SELECT COUNT(DISTINCT a.worker.id) FROM Attendance a WHERE a.attendanceDate = :date AND a.status IN ('PRESENT','HALF_DAY')")
    long countWorkersOnDate(@Param("date") LocalDate date);
}
