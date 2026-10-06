package com.arecaharvest.service;

import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Equipment;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.EquipmentRepository;
import com.arecaharvest.repository.FieldRepository;
import com.arecaharvest.repository.WorkerRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final WorkerRepository workerRepository;
    private final FieldRepository fieldRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<Equipment> search(String category, String status, Pageable pageable) {
        return PagedResponse.of(equipmentRepository.searchEquipment(category, status, pageable));
    }

    @Transactional(readOnly = true)
    public List<Equipment> getMaintenanceDue() {
        return equipmentRepository.findEquipmentDueForMaintenance(LocalDate.now().plusDays(7));
    }

    @Transactional
    public Equipment create(EquipmentRequest req) {
        Equipment eq = Equipment.builder()
                .name(req.getName()).category(req.getCategory())
                .quantity(req.getQuantity() != null ? req.getQuantity() : 1)
                .conditionStatus(req.getConditionStatus() != null
                        ? req.getConditionStatus() : Equipment.ConditionStatus.GOOD)
                .purchaseDate(req.getPurchaseDate()).purchaseCost(req.getPurchaseCost())
                .lastMaintenanceDate(req.getLastMaintenanceDate())
                .nextMaintenanceDate(req.getNextMaintenanceDate())
                .status(Equipment.EquipmentStatus.AVAILABLE)
                .notes(req.getNotes())
                .build();
        if (req.getAssignedWorkerId() != null)
            eq.setAssignedWorker(workerRepository.findById(req.getAssignedWorkerId()).orElse(null));
        if (req.getAssignedFieldId() != null)
            eq.setAssignedField(fieldRepository.findById(req.getAssignedFieldId()).orElse(null));

        Equipment saved = equipmentRepository.save(eq);
        auditLog.log("CREATE", "EQUIPMENT", saved.getId(), "Equipment: " + saved.getName());
        return saved;
    }

    @Transactional
    public Equipment update(Long id, EquipmentRequest req) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", id));
        eq.setName(req.getName()); eq.setCategory(req.getCategory());
        if (req.getQuantity() != null) eq.setQuantity(req.getQuantity());
        eq.setPurchaseCost(req.getPurchaseCost());
        eq.setLastMaintenanceDate(req.getLastMaintenanceDate());
        eq.setNextMaintenanceDate(req.getNextMaintenanceDate());
        if (req.getStatus() != null) eq.setStatus(req.getStatus());
        if (req.getConditionStatus() != null) eq.setConditionStatus(req.getConditionStatus());
        if (req.getAssignedWorkerId() != null)
            eq.setAssignedWorker(workerRepository.findById(req.getAssignedWorkerId()).orElse(null));
        if (req.getAssignedFieldId() != null)
            eq.setAssignedField(fieldRepository.findById(req.getAssignedFieldId()).orElse(null));
        eq.setNotes(req.getNotes());
        auditLog.log("UPDATE", "EQUIPMENT", id, "Equipment updated: " + eq.getName());
        return equipmentRepository.save(eq);
    }

    @Getter @Setter
    public static class EquipmentRequest {
        private String name;
        private Equipment.EquipmentCategory category;
        private Integer quantity;
        private Equipment.ConditionStatus conditionStatus;
        private LocalDate purchaseDate;
        private BigDecimal purchaseCost;
        private Long assignedWorkerId; private Long assignedFieldId;
        private LocalDate lastMaintenanceDate; private LocalDate nextMaintenanceDate;
        private Equipment.EquipmentStatus status;
        private String notes;
    }
}
