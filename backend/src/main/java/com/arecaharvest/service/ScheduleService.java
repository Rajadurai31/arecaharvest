package com.arecaharvest.service;

import com.arecaharvest.dto.request.ScheduleRequest;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Schedule;
import com.arecaharvest.entity.Worker;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final FieldRepository fieldRepository;
    private final HarvestingContractRepository contractRepository;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<Schedule> search(Long fieldId, String status,
                                           LocalDate fromDate, LocalDate toDate,
                                           Pageable pageable) {
        return PagedResponse.of(
                scheduleRepository.searchSchedules(fieldId, status, fromDate, toDate, pageable));
    }

    @Transactional(readOnly = true)
    public List<Schedule> getToday() {
        return scheduleRepository.findByScheduleDateOrderByPlannedStartTimeAsc(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Schedule> getUpcoming() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 50);
        return scheduleRepository.searchSchedules(null, "PLANNED",
                LocalDate.now(), LocalDate.now().plusDays(7), pageable).getContent();
    }

    @Transactional
    public Schedule create(ScheduleRequest req) {
        var field = fieldRepository.findById(req.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", req.getFieldId()));

        Set<Worker> workers = resolveWorkers(req.getWorkerIds());

        // Check worker scheduling conflicts
        for (Worker w : workers) {
            if (scheduleRepository.hasWorkerConflict(w.getId(), req.getScheduleDate(), null)) {
                throw new BusinessException("Worker " + w.getName() +
                        " is already scheduled on " + req.getScheduleDate());
            }
        }

        Schedule schedule = Schedule.builder()
                .scheduleDate(req.getScheduleDate()).field(field)
                .plannedStartTime(req.getPlannedStartTime()).plannedEndTime(req.getPlannedEndTime())
                .expectedQuantityKg(req.getExpectedQuantityKg())
                .expectedWorkerCount(req.getExpectedWorkerCount())
                .notes(req.getNotes())
                .status(req.getStatus() != null ? req.getStatus() : Schedule.ScheduleStatus.PLANNED)
                .workers(workers)
                .build();

        if (req.getContractId() != null)
            schedule.setContract(contractRepository.findById(req.getContractId()).orElse(null));
        if (req.getSupervisorId() != null)
            schedule.setSupervisor(userRepository.findById(req.getSupervisorId()).orElse(null));
        if (req.getVehicleId() != null)
            schedule.setVehicle(vehicleRepository.findById(req.getVehicleId()).orElse(null));

        Schedule saved = scheduleRepository.save(schedule);
        auditLog.log("CREATE", "SCHEDULE", saved.getId(),
                "Schedule on " + req.getScheduleDate() + " at " + field.getFieldName());
        return saved;
    }

    @Transactional
    public Schedule updateStatus(Long id, Schedule.ScheduleStatus status) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", id));
        s.setStatus(status);
        auditLog.log("STATUS_CHANGE", "SCHEDULE", id, "Schedule → " + status);
        return scheduleRepository.save(s);
    }

    private Set<Worker> resolveWorkers(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(workerRepository.findAllById(ids));
    }
}
