package com.arecaharvest.service;

import com.arecaharvest.dto.request.WorkerRequest;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.dto.response.WorkerResponse;
import com.arecaharvest.entity.Worker;
import com.arecaharvest.entity.enums.SkillType;
import com.arecaharvest.entity.enums.WorkerStatus;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final WageLedgerRepository wageLedgerRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<WorkerResponse> searchWorkers(String search, WorkerStatus status,
                                                        SkillType skillType, Pageable pageable) {
        return PagedResponse.of(
                workerRepository.searchWorkers(search, status, skillType, pageable)
                        .map(w -> toResponse(w, false)));
    }

    @Transactional(readOnly = true)
    public WorkerResponse getById(Long id) {
        return toResponse(findOrThrow(id), true);
    }

    @Transactional
    public WorkerResponse create(WorkerRequest req) {
        Worker worker = Worker.builder()
                .name(req.getName()).phone(req.getPhone())
                .alternatePhone(req.getAlternatePhone()).address(req.getAddress())
                .village(req.getVillage()).district(req.getDistrict())
                .state(req.getState() != null ? req.getState() : "Karnataka")
                .skillType(req.getSkillType())
                .experienceYears(req.getExperienceYears() != null ? req.getExperienceYears() : BigDecimal.ZERO)
                .dailyWage(req.getDailyWage())
                .joiningDate(req.getJoiningDate())
                .status(req.getStatus() != null ? req.getStatus() : WorkerStatus.ACTIVE)
                .emergencyContactName(req.getEmergencyContactName())
                .emergencyContactPhone(req.getEmergencyContactPhone())
                .notes(req.getNotes())
                .build();

        if (req.getUserId() != null) {
            worker.setUser(userRepository.findById(req.getUserId()).orElse(null));
        }

        Worker saved = workerRepository.save(worker);
        auditLog.log("CREATE", "WORKER", saved.getId(), "Worker created: " + saved.getName());
        return toResponse(saved, false);
    }

    @Transactional
    public WorkerResponse update(Long id, WorkerRequest req) {
        Worker worker = findOrThrow(id);
        worker.setName(req.getName());
        worker.setPhone(req.getPhone());
        worker.setAlternatePhone(req.getAlternatePhone());
        worker.setAddress(req.getAddress());
        worker.setVillage(req.getVillage());
        worker.setDistrict(req.getDistrict());
        if (req.getState() != null) worker.setState(req.getState());
        worker.setSkillType(req.getSkillType());
        if (req.getExperienceYears() != null) worker.setExperienceYears(req.getExperienceYears());
        worker.setDailyWage(req.getDailyWage());
        if (req.getJoiningDate() != null) worker.setJoiningDate(req.getJoiningDate());
        if (req.getStatus() != null) worker.setStatus(req.getStatus());
        worker.setEmergencyContactName(req.getEmergencyContactName());
        worker.setEmergencyContactPhone(req.getEmergencyContactPhone());
        worker.setNotes(req.getNotes());
        Worker saved = workerRepository.save(worker);
        auditLog.log("UPDATE", "WORKER", id, "Worker updated: " + saved.getName());
        return toResponse(saved, false);
    }

    @Transactional
    public WorkerResponse updateStatus(Long id, WorkerStatus status) {
        Worker worker = findOrThrow(id);
        worker.setStatus(status);
        workerRepository.save(worker);
        return toResponse(worker, false);
    }

    private Worker findOrThrow(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", id));
    }

    private WorkerResponse toResponse(Worker w, boolean withLedger) {
        WorkerResponse.WorkerResponseBuilder b = WorkerResponse.builder()
                .id(w.getId())
                .userId(w.getUser() != null ? w.getUser().getId() : null)
                .name(w.getName()).phone(w.getPhone()).alternatePhone(w.getAlternatePhone())
                .address(w.getAddress()).village(w.getVillage()).district(w.getDistrict())
                .state(w.getState()).skillType(w.getSkillType())
                .experienceYears(w.getExperienceYears()).dailyWage(w.getDailyWage())
                .joiningDate(w.getJoiningDate()).status(w.getStatus())
                .emergencyContactName(w.getEmergencyContactName())
                .emergencyContactPhone(w.getEmergencyContactPhone())
                .notes(w.getNotes())
                .createdAt(w.getCreatedAt()).updatedAt(w.getUpdatedAt());

        if (withLedger) {
            b.totalEarned(wageLedgerRepository.calculateTotalEarned(w.getId()))
             .totalPaid(wageLedgerRepository.calculateTotalPaid(w.getId()))
             .outstandingBalance(wageLedgerRepository.calculateOutstandingBalance(w.getId()));
        }
        return b.build();
    }
}
