package com.arecaharvest.service;

import com.arecaharvest.dto.request.HarvestSessionRequest;
import com.arecaharvest.dto.response.HarvestSessionResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.*;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HarvestService {

    private final HarvestSessionRepository sessionRepository;
    private final FieldRepository fieldRepository;
    private final HarvestingContractRepository contractRepository;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final HarvestRecordRepository recordRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<HarvestSessionResponse> searchSessions(Long fieldId, Long contractId,
                                                                  String status,
                                                                  LocalDate from, LocalDate to,
                                                                  Pageable pageable) {
        return PagedResponse.of(
                sessionRepository.searchSessions(fieldId, contractId, status, from, to, pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public HarvestSessionResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public HarvestSessionResponse createSession(HarvestSessionRequest req) {
        Field field = fieldRepository.findById(req.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", req.getFieldId()));
        HarvestingContract contract = contractRepository.findById(req.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", req.getContractId()));

        Set<Worker> workers = resolveWorkers(req.getWorkerIds());

        HarvestSession session = HarvestSession.builder()
                .sessionDate(req.getSessionDate())
                .field(field).contract(contract)
                .startTime(req.getStartTime()).endTime(req.getEndTime())
                .totalBags(req.getTotalBags() != null ? req.getTotalBags() : 0)
                .totalQuantity(req.getTotalQuantity() != null ? req.getTotalQuantity() : BigDecimal.ZERO)
                .quantityUnit(req.getQuantityUnit() != null ? req.getQuantityUnit() : "KG")
                .workerCount(workers.size())
                .status(req.getStatus() != null ? req.getStatus() : HarvestSession.SessionStatus.PLANNED)
                .remarks(req.getRemarks())
                .workers(workers)
                .build();

        if (req.getSupervisorId() != null) {
            session.setSupervisor(userRepository.findById(req.getSupervisorId()).orElse(null));
        }

        HarvestSession saved = sessionRepository.save(session);

        // If session is COMPLETED and has quantity, auto-create inventory entry
        if (saved.getStatus() == HarvestSession.SessionStatus.COMPLETED
                && saved.getTotalQuantity().compareTo(BigDecimal.ZERO) > 0) {
            createInventoryFromSession(saved);
        }

        auditLog.log("CREATE", "HARVEST_SESSION", saved.getId(),
                "Session on " + saved.getSessionDate() + " at " + field.getFieldName());
        return toResponse(saved);
    }

    @Transactional
    public HarvestSessionResponse updateSession(Long id, HarvestSessionRequest req) {
        HarvestSession session = findOrThrow(id);
        boolean wasCompleted = session.getStatus() == HarvestSession.SessionStatus.COMPLETED;

        session.setSessionDate(req.getSessionDate());
        session.setStartTime(req.getStartTime());
        session.setEndTime(req.getEndTime());
        if (req.getTotalBags() != null) session.setTotalBags(req.getTotalBags());
        if (req.getTotalQuantity() != null) session.setTotalQuantity(req.getTotalQuantity());
        if (req.getQuantityUnit() != null) session.setQuantityUnit(req.getQuantityUnit());
        if (req.getStatus() != null) session.setStatus(req.getStatus());
        session.setRemarks(req.getRemarks());

        if (req.getWorkerIds() != null) {
            session.setWorkers(resolveWorkers(req.getWorkerIds()));
            session.setWorkerCount(session.getWorkers().size());
        }

        HarvestSession saved = sessionRepository.save(session);

        // Auto-create inventory if newly completed
        if (!wasCompleted && saved.getStatus() == HarvestSession.SessionStatus.COMPLETED
                && saved.getTotalQuantity().compareTo(BigDecimal.ZERO) > 0) {
            createInventoryFromSession(saved);
        }

        auditLog.log("UPDATE", "HARVEST_SESSION", id, "Session updated");
        return toResponse(saved);
    }

    private void createInventoryFromSession(HarvestSession session) {
        Inventory inventory = Inventory.builder()
                .field(session.getField())
                .contract(session.getContract())
                .quantityReceived(session.getTotalQuantity())
                .quantityUnit(session.getQuantityUnit())
                .quantitySold(BigDecimal.ZERO)
                .availableQuantity(session.getTotalQuantity())
                .receivedDate(session.getSessionDate())
                .status(Inventory.InventoryStatus.AVAILABLE)
                .build();
        inventoryRepository.save(inventory);
        auditLog.log("CREATE", "INVENTORY", inventory.getId(),
                "Auto-created from session " + session.getId());
    }

    private Set<Worker> resolveWorkers(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(workerRepository.findAllById(ids));
    }

    private HarvestSession findOrThrow(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HarvestSession", "id", id));
    }

    private HarvestSessionResponse toResponse(HarvestSession s) {
        List<String> workerNames = s.getWorkers().stream()
                .map(Worker::getName).collect(Collectors.toList());
        return HarvestSessionResponse.builder()
                .id(s.getId()).sessionDate(s.getSessionDate())
                .fieldId(s.getField().getId()).fieldName(s.getField().getFieldName())
                .contractId(s.getContract().getId()).contractNumber(s.getContract().getContractNumber())
                .supervisorId(s.getSupervisor() != null ? s.getSupervisor().getId() : null)
                .supervisorName(s.getSupervisor() != null ? s.getSupervisor().getFullName() : null)
                .startTime(s.getStartTime()).endTime(s.getEndTime())
                .totalBags(s.getTotalBags()).totalQuantity(s.getTotalQuantity())
                .quantityUnit(s.getQuantityUnit()).workerCount(s.getWorkerCount())
                .totalWorkingHours(s.getTotalWorkingHours()).status(s.getStatus())
                .remarks(s.getRemarks()).workerNames(workerNames)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
