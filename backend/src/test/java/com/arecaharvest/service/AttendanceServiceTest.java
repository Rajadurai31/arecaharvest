package com.arecaharvest.service;

import com.arecaharvest.dto.request.AttendanceRequest;
import com.arecaharvest.dto.response.AttendanceResponse;
import com.arecaharvest.entity.Attendance;
import com.arecaharvest.entity.Field;
import com.arecaharvest.entity.Owner;
import com.arecaharvest.entity.Worker;
import com.arecaharvest.exception.DuplicateResourceException;
import com.arecaharvest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AttendanceService Unit Tests")
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private WorkerRepository workerRepository;
    @Mock private FieldRepository fieldRepository;
    @Mock private HarvestingContractRepository contractRepository;
    @Mock private HarvestSessionRepository sessionRepository;
    @Mock private UserRepository userRepository;
    @Mock private WageLedgerRepository wageLedgerRepository;
    @Mock private ContractCalculationService calculationService;
    @Mock private AuditLogService auditLog;

    @InjectMocks private AttendanceService attendanceService;

    private Worker worker;
    private Field field;

    @BeforeEach
    void setUp() {
        worker = Worker.builder()
                .name("Ramesh")
                .dailyWage(new BigDecimal("700.00"))
                .build();
        worker.setId(1L);

        Owner owner = Owner.builder().name("Appaiah").build();
        owner.setId(1L);

        field = Field.builder()
                .fieldName("North Estate")
                .owner(owner)
                .build();
        field.setId(1L);
    }

    @Test
    @DisplayName("Mark attendance fails when duplicate record exists for date and field")
    void markAttendance_duplicate_throwsException() {
        AttendanceRequest req = new AttendanceRequest();
        req.setWorkerId(1L);
        req.setFieldId(1L);
        req.setAttendanceDate(LocalDate.now());

        when(attendanceRepository.existsByWorkerIdAndFieldIdAndAttendanceDate(1L, 1L, req.getAttendanceDate()))
                .thenReturn(true);

        assertThatThrownBy(() -> attendanceService.markAttendance(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already marked");
    }

    @Test
    @DisplayName("Mark attendance PRESENT calculates wage and creates ledger entry")
    void markAttendance_present_success() {
        LocalDate today = LocalDate.now();
        AttendanceRequest req = new AttendanceRequest();
        req.setWorkerId(1L);
        req.setFieldId(1L);
        req.setAttendanceDate(today);
        req.setStatus(Attendance.AttendanceStatus.PRESENT);

        when(attendanceRepository.existsByWorkerIdAndFieldIdAndAttendanceDate(1L, 1L, today)).thenReturn(false);
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(fieldRepository.findById(1L)).thenReturn(Optional.of(field));
        when(calculationService.calculateDailyWage(any(), eq("PRESENT"), any(), any()))
                .thenReturn(new BigDecimal("700.00"));

        Attendance savedAttendance = Attendance.builder()
                .worker(worker)
                .field(field)
                .attendanceDate(today)
                .status(Attendance.AttendanceStatus.PRESENT)
                .dailyWageApplied(new BigDecimal("700.00"))
                .build();
        savedAttendance.setId(10L);

        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);

        AttendanceResponse res = attendanceService.markAttendance(req);

        assertThat(res).isNotNull();
        assertThat(res.getWorkerId()).isEqualTo(1L);
        assertThat(res.getStatus()).isEqualTo(Attendance.AttendanceStatus.PRESENT);
        verify(wageLedgerRepository, times(1)).save(any());
        verify(auditLog, times(1)).log(eq("MARK_ATTENDANCE"), eq("ATTENDANCE"), eq(10L), anyString());
    }
}
