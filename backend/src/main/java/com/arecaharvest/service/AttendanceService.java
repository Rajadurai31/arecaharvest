package com.arecaharvest.service;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.dto.request.AttendanceRequest;
import com.arecaharvest.dto.response.AttendanceResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.*;
import com.arecaharvest.entity.WageLedger.TransactionType;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.DuplicateResourceException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkerRepository workerRepository;
    private final FieldRepository fieldRepository;
    private final HarvestingContractRepository contractRepository;
    private final HarvestSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final WageLedgerRepository wageLedgerRepository;
    private final ContractCalculationService calculationService;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<AttendanceResponse> searchAttendance(Long workerId, Long fieldId,
                                                               LocalDate fromDate, LocalDate toDate,
                                                               Pageable pageable) {
        return PagedResponse.of(
                attendanceRepository.searchAttendance(workerId, fieldId, fromDate, toDate, pageable)
                        .map(this::toResponse));
    }

    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest req) {
        if (attendanceRepository.existsByWorkerIdAndFieldIdAndAttendanceDate(
                req.getWorkerId(), req.getFieldId(), req.getAttendanceDate())) {
            throw new DuplicateResourceException("Attendance already marked for this worker on this date at this field");
        }

        Worker worker = workerRepository.findById(req.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", req.getWorkerId()));
        Field field = fieldRepository.findById(req.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", req.getFieldId()));

        // Use override wage or default worker wage
        BigDecimal dailyWage = req.getDailyWageOverride() != null
                ? req.getDailyWageOverride() : worker.getDailyWage();
        BigDecimal overtime = req.getOvertimeHours() != null ? req.getOvertimeHours() : BigDecimal.ZERO;
        BigDecimal overtimeRate = req.getOvertimeRate() != null ? req.getOvertimeRate() : BigDecimal.ZERO;

        Attendance attendance = Attendance.builder()
                .worker(worker).field(field)
                .attendanceDate(req.getAttendanceDate())
                .status(req.getStatus())
                .checkInTime(req.getCheckInTime()).checkOutTime(req.getCheckOutTime())
                .workingHours(req.getWorkingHours())
                .overtimeHours(overtime).overtimeRate(overtimeRate)
                .dailyWageApplied(dailyWage)
                .remarks(req.getRemarks())
                .markedBy(getCurrentUser())
                .build();

        if (req.getContractId() != null) {
            attendance.setContract(contractRepository.findById(req.getContractId()).orElse(null));
        }
        if (req.getHarvestSessionId() != null) {
            attendance.setHarvestSession(sessionRepository.findById(req.getHarvestSessionId()).orElse(null));
        }

        Attendance saved = attendanceRepository.save(attendance);

        // Auto-create wage ledger entries for productive attendance
        if (req.getStatus() == Attendance.AttendanceStatus.PRESENT
                || req.getStatus() == Attendance.AttendanceStatus.HALF_DAY) {
            createWageLedgerEntry(saved, worker, field, dailyWage, overtime, overtimeRate,
                    req.getStatus().name());
        }

        auditLog.log("MARK_ATTENDANCE", "ATTENDANCE", saved.getId(),
                worker.getName() + " — " + req.getStatus() + " on " + req.getAttendanceDate());
        return toResponse(saved);
    }

    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceRequest req) {
        Attendance existing = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        // If changing date/worker/field, check for duplicate
        if (!existing.getAttendanceDate().equals(req.getAttendanceDate())
                || !existing.getWorker().getId().equals(req.getWorkerId())
                || !existing.getField().getId().equals(req.getFieldId())) {
            if (attendanceRepository.existsByWorkerIdAndFieldIdAndAttendanceDate(
                    req.getWorkerId(), req.getFieldId(), req.getAttendanceDate())) {
                throw new DuplicateResourceException("Duplicate attendance entry");
            }
        }

        existing.setStatus(req.getStatus());
        existing.setCheckInTime(req.getCheckInTime());
        existing.setCheckOutTime(req.getCheckOutTime());
        existing.setWorkingHours(req.getWorkingHours());
        existing.setOvertimeHours(req.getOvertimeHours() != null ? req.getOvertimeHours() : BigDecimal.ZERO);
        existing.setRemarks(req.getRemarks());

        attendanceRepository.save(existing);
        auditLog.log("UPDATE", "ATTENDANCE", id, "Attendance updated");
        return toResponse(existing);
    }

    private void createWageLedgerEntry(Attendance attendance, Worker worker, Field field,
                                        BigDecimal dailyWage, BigDecimal overtimeHours,
                                        BigDecimal overtimeRate, String statusName) {
        BigDecimal wageAmount = calculationService.calculateDailyWage(
                dailyWage, statusName, overtimeHours, overtimeRate);

        if (wageAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal baseWage = calculationService.calculateDailyWage(dailyWage, statusName, BigDecimal.ZERO, BigDecimal.ZERO);
        BigDecimal overtimeAmount = wageAmount.subtract(baseWage);

        // Daily wage entry
        WageLedger entry = WageLedger.builder()
                .worker(worker).transactionType(TransactionType.DAILY_WAGE)
                .amount(baseWage).transactionDate(attendance.getAttendanceDate())
                .attendance(attendance).field(field)
                .contract(attendance.getContract())
                .description("Daily wage — " + statusName + " on " + attendance.getAttendanceDate())
                .recordedBy(attendance.getMarkedBy())
                .build();
        wageLedgerRepository.save(entry);

        // Separate overtime entry if applicable
        if (overtimeAmount.compareTo(BigDecimal.ZERO) > 0) {
            WageLedger otEntry = WageLedger.builder()
                    .worker(worker).transactionType(TransactionType.OVERTIME)
                    .amount(overtimeAmount).transactionDate(attendance.getAttendanceDate())
                    .attendance(attendance).field(field)
                    .description("Overtime " + overtimeHours + "h × ₹" + overtimeRate)
                    .build();
            wageLedgerRepository.save(otEntry);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return userRepository.findById(ud.getId()).orElse(null);
        }
        return null;
    }

    private AttendanceResponse toResponse(Attendance a) {
        BigDecimal wage = calculationService.calculateDailyWage(
                a.getDailyWageApplied(), a.getStatus().name(),
                a.getOvertimeHours(), a.getOvertimeRate());
        return AttendanceResponse.builder()
                .id(a.getId())
                .workerId(a.getWorker().getId()).workerName(a.getWorker().getName())
                .fieldId(a.getField().getId()).fieldName(a.getField().getFieldName())
                .contractId(a.getContract() != null ? a.getContract().getId() : null)
                .harvestSessionId(a.getHarvestSession() != null ? a.getHarvestSession().getId() : null)
                .attendanceDate(a.getAttendanceDate()).status(a.getStatus())
                .checkInTime(a.getCheckInTime()).checkOutTime(a.getCheckOutTime())
                .workingHours(a.getWorkingHours()).overtimeHours(a.getOvertimeHours())
                .dailyWageApplied(a.getDailyWageApplied()).overtimeRate(a.getOvertimeRate())
                .wageForDay(wage)
                .remarks(a.getRemarks())
                .markedByName(a.getMarkedBy() != null ? a.getMarkedBy().getFullName() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
