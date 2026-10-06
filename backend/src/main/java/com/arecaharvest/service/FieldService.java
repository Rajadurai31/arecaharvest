package com.arecaharvest.service;

import com.arecaharvest.dto.request.FieldRequest;
import com.arecaharvest.dto.response.FieldResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Field;
import com.arecaharvest.entity.HarvestingContract;
import com.arecaharvest.entity.Owner;
import com.arecaharvest.entity.enums.ContractStatus;
import com.arecaharvest.entity.enums.FieldStatus;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.FieldRepository;
import com.arecaharvest.repository.HarvestingContractRepository;
import com.arecaharvest.repository.HarvestSessionRepository;
import com.arecaharvest.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldService {

    private final FieldRepository fieldRepository;
    private final OwnerRepository ownerRepository;
    private final HarvestingContractRepository contractRepository;
    private final HarvestSessionRepository sessionRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<FieldResponse> searchFields(Long ownerId, FieldStatus status,
                                                      String search, Pageable pageable) {
        Page<Field> page = fieldRepository.searchFields(ownerId, status, search, pageable);
        return PagedResponse.of(page.map(f -> toResponse(f, false)));
    }

    @Transactional(readOnly = true)
    public FieldResponse getById(Long id) {
        Field field = findOrThrow(id);
        return toResponse(field, true);
    }

    @Transactional
    public FieldResponse create(FieldRequest request) {
        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner", "id", request.getOwnerId()));

        Field field = Field.builder()
                .owner(owner).fieldName(request.getFieldName())
                .address(request.getAddress()).village(request.getVillage())
                .district(request.getDistrict())
                .state(request.getState() != null ? request.getState() : "Karnataka")
                .acreage(request.getAcreage()).treeCount(request.getTreeCount())
                .gpsLatitude(request.getGpsLatitude()).gpsLongitude(request.getGpsLongitude())
                .expectedYieldKg(request.getExpectedYieldKg())
                .status(request.getStatus() != null ? request.getStatus() : FieldStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        Field saved = fieldRepository.save(field);
        auditLog.log("CREATE", "FIELD", saved.getId(), "Field created: " + saved.getFieldName());
        return toResponse(saved, false);
    }

    @Transactional
    public FieldResponse update(Long id, FieldRequest request) {
        Field field = findOrThrow(id);
        if (request.getOwnerId() != null && !request.getOwnerId().equals(field.getOwner().getId())) {
            Owner owner = ownerRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner", "id", request.getOwnerId()));
            field.setOwner(owner);
        }
        field.setFieldName(request.getFieldName());
        field.setAddress(request.getAddress());
        field.setVillage(request.getVillage());
        field.setDistrict(request.getDistrict());
        if (request.getState() != null) field.setState(request.getState());
        field.setAcreage(request.getAcreage());
        field.setTreeCount(request.getTreeCount());
        field.setGpsLatitude(request.getGpsLatitude());
        field.setGpsLongitude(request.getGpsLongitude());
        field.setExpectedYieldKg(request.getExpectedYieldKg());
        if (request.getStatus() != null) field.setStatus(request.getStatus());
        field.setNotes(request.getNotes());
        Field saved = fieldRepository.save(field);
        auditLog.log("UPDATE", "FIELD", id, "Field updated: " + saved.getFieldName());
        return toResponse(saved, false);
    }

    @Transactional
    public FieldResponse updateStatus(Long id, FieldStatus status) {
        Field field = findOrThrow(id);
        field.setStatus(status);
        fieldRepository.save(field);
        auditLog.log("STATUS_CHANGE", "FIELD", id, "Field status → " + status);
        return toResponse(field, false);
    }

    private Field findOrThrow(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", id));
    }

    private FieldResponse toResponse(Field f, boolean withProgress) {
        FieldResponse.FieldResponseBuilder builder = FieldResponse.builder()
                .id(f.getId())
                .ownerId(f.getOwner().getId())
                .ownerName(f.getOwner().getName())
                .fieldName(f.getFieldName())
                .address(f.getAddress()).village(f.getVillage())
                .district(f.getDistrict()).state(f.getState())
                .acreage(f.getAcreage()).treeCount(f.getTreeCount())
                .gpsLatitude(f.getGpsLatitude()).gpsLongitude(f.getGpsLongitude())
                .expectedYieldKg(f.getExpectedYieldKg())
                .status(f.getStatus()).notes(f.getNotes())
                .createdAt(f.getCreatedAt()).updatedAt(f.getUpdatedAt());

        if (withProgress) {
            BigDecimal fieldHarvested = sessionRepository.sumQuantityForField(f.getId());
            BigDecimal expected = f.getExpectedYieldKg() != null ? f.getExpectedYieldKg() : BigDecimal.ZERO;
            BigDecimal remaining = expected.subtract(fieldHarvested).max(BigDecimal.ZERO);
            double progress = expected.compareTo(BigDecimal.ZERO) > 0
                    ? fieldHarvested.doubleValue() / expected.doubleValue() * 100.0 : 0.0;

            List<HarvestingContract> active = contractRepository.findByFieldIdAndStatus(f.getId(), ContractStatus.ACTIVE);
            Long activeContractId = active.isEmpty() ? null : active.get(0).getId();

            builder.harvestedQuantityKg(fieldHarvested)
                    .remainingQuantityKg(remaining)
                    .progressPercent(Math.min(100.0, progress))
                    .activeContractId(activeContractId);
        }
        return builder.build();
    }
}
