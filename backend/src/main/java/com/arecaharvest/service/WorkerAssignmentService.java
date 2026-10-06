package com.arecaharvest.service;

import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.WorkerAssignment;
import com.arecaharvest.entity.WorkerAssignment.AssignmentStatus;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.DuplicateResourceException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WorkerAssignmentService {

    private final WorkerAssignmentRepository assignmentRepository;
    private final WorkerRepository workerRepository;
    private final FieldRepository fieldRepository;
    private final HarvestingContractRepository contractRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<WorkerAssignment> search(Long workerId, Long fieldId,
                                                   LocalDate fromDate, LocalDate toDate,
                                                   Pageable pageable) {
        return PagedResponse.of(
                assignmentRepository.searchAssignments(workerId, fieldId, fromDate, toDate, pageable));
    }

    @Transactional
    public WorkerAssignment create(AssignmentRequest req) {
        if (assignmentRepository.existsByWorkerIdAndFieldIdAndAssignedDate(
                req.getWorkerId(), req.getFieldId(), req.getAssignedDate())) {
            throw new DuplicateResourceException(
                    "Worker already assigned to this field on " + req.getAssignedDate());
        }

        var worker = workerRepository.findById(req.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", req.getWorkerId()));
        var field = fieldRepository.findById(req.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", req.getFieldId()));

        var assignment = WorkerAssignment.builder()
                .worker(worker).field(field).assignedDate(req.getAssignedDate())
                .assignedRole(req.getAssignedRole()).notes(req.getNotes())
                .status(AssignmentStatus.ACTIVE)
                .build();

        if (req.getContractId() != null) {
            assignment.setContract(contractRepository.findById(req.getContractId()).orElse(null));
        }
        if (req.getAssignedById() != null) {
            assignment.setAssignedBy(userRepository.findById(req.getAssignedById()).orElse(null));
        }

        var saved = assignmentRepository.save(assignment);
        auditLog.log("CREATE", "WORKER_ASSIGNMENT", saved.getId(),
                worker.getName() + " → " + field.getFieldName() + " on " + req.getAssignedDate());
        return saved;
    }

    @Transactional
    public WorkerAssignment cancel(Long id) {
        var assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkerAssignment", "id", id));
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE assignments can be cancelled");
        }
        assignment.setStatus(AssignmentStatus.CANCELLED);
        auditLog.log("CANCEL", "WORKER_ASSIGNMENT", id, "Assignment cancelled");
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public WorkerAssignment complete(Long id) {
        var assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkerAssignment", "id", id));
        assignment.setStatus(AssignmentStatus.COMPLETED);
        return assignmentRepository.save(assignment);
    }

    @Getter @Setter
    public static class AssignmentRequest {
        private Long workerId;
        private Long fieldId;
        private Long contractId;
        private LocalDate assignedDate;
        private com.arecaharvest.entity.enums.SkillType assignedRole;
        private Long assignedById;
        private String notes;
    }
}
