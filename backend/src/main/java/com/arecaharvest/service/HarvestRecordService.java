package com.arecaharvest.service;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.HarvestRecord;
import com.arecaharvest.entity.Inventory;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class HarvestRecordService {

    private final HarvestRecordRepository recordRepository;
    private final HarvestSessionRepository sessionRepository;
    private final FieldRepository fieldRepository;
    private final HarvestingContractRepository contractRepository;
    private final OwnerRepository ownerRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<HarvestRecord> search(Long fieldId, Long contractId,
                                                LocalDate fromDate, LocalDate toDate,
                                                Pageable pageable) {
        return PagedResponse.of(
                recordRepository.searchRecords(fieldId, contractId, fromDate, toDate, pageable));
    }

    @Transactional
    public HarvestRecord create(HarvestRecordRequest req) {
        var session = sessionRepository.findById(req.getHarvestSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("HarvestSession", "id", req.getHarvestSessionId()));
        var field = fieldRepository.findById(req.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", req.getFieldId()));
        var contract = contractRepository.findById(req.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", req.getContractId()));
        var owner = ownerRepository.findById(req.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner", "id", req.getOwnerId()));

        var record = HarvestRecord.builder()
                .harvestSession(session).field(field).contract(contract).owner(owner)
                .recordDate(req.getRecordDate())
                .bagsCount(req.getBagsCount() != null ? req.getBagsCount() : 0)
                .quantity(req.getQuantity())
                .quantityUnit(req.getQuantityUnit() != null ? req.getQuantityUnit() : "KG")
                .workerCount(req.getWorkerCount())
                .startTime(req.getStartTime()).endTime(req.getEndTime())
                .remarks(req.getRemarks()).amendment(false)
                .build();

        var saved = recordRepository.save(record);

        // Auto-create inventory entry for this record
        var inventory = Inventory.builder()
                .harvestRecord(saved).field(field).contract(contract)
                .quantityReceived(req.getQuantity())
                .quantityUnit(record.getQuantityUnit())
                .quantitySold(BigDecimal.ZERO)
                .availableQuantity(req.getQuantity())
                .receivedDate(req.getRecordDate())
                .status(Inventory.InventoryStatus.AVAILABLE)
                .build();
        inventoryRepository.save(inventory);

        auditLog.log("CREATE", "HARVEST_RECORD", saved.getId(),
                String.format("%.3f KG at field %s on %s", req.getQuantity(), field.getFieldName(), req.getRecordDate()));
        return saved;
    }

    @Transactional
    public HarvestRecord amend(Long id, AmendmentRequest req) {
        var original = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HarvestRecord", "id", id));

        if (original.isAmendment()) {
            throw new BusinessException("Cannot amend an already-amended record. Amend the original.");
        }
        if (req.getAmendmentReason() == null || req.getAmendmentReason().isBlank()) {
            throw new BusinessException("Amendment reason is required");
        }

        var amendment = HarvestRecord.builder()
                .harvestSession(original.getHarvestSession())
                .field(original.getField()).contract(original.getContract()).owner(original.getOwner())
                .recordDate(original.getRecordDate())
                .bagsCount(req.getBagsCount() != null ? req.getBagsCount() : original.getBagsCount())
                .quantity(req.getQuantity() != null ? req.getQuantity() : original.getQuantity())
                .quantityUnit(original.getQuantityUnit())
                .workerCount(original.getWorkerCount())
                .startTime(original.getStartTime()).endTime(original.getEndTime())
                .remarks(req.getRemarks() != null ? req.getRemarks() : original.getRemarks())
                .amendment(true)
                .originalRecord(original)
                .amendedBy(getCurrentUser())
                .amendmentReason(req.getAmendmentReason())
                .build();

        var saved = recordRepository.save(amendment);
        auditLog.log("AMEND", "HARVEST_RECORD", id,
                "Amended → record #" + saved.getId() + ": " + req.getAmendmentReason());
        return saved;
    }

    private com.arecaharvest.entity.User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return userRepository.findById(ud.getId()).orElse(null);
        }
        return null;
    }

    @Getter @Setter
    public static class HarvestRecordRequest {
        private Long harvestSessionId;
        private Long fieldId;
        private Long contractId;
        private Long ownerId;
        private LocalDate recordDate;
        private Integer bagsCount;
        private BigDecimal quantity;
        private String quantityUnit;
        private Integer workerCount;
        private LocalTime startTime;
        private LocalTime endTime;
        private String remarks;
    }

    @Getter @Setter
    public static class AmendmentRequest {
        private Integer bagsCount;
        private BigDecimal quantity;
        private String remarks;
        private String amendmentReason;
    }
}
