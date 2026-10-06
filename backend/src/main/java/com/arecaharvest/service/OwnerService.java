package com.arecaharvest.service;

import com.arecaharvest.dto.request.OwnerRequest;
import com.arecaharvest.dto.response.OwnerResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Owner;
import com.arecaharvest.entity.enums.ContractStatus;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.FieldRepository;
import com.arecaharvest.repository.HarvestingContractRepository;
import com.arecaharvest.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final FieldRepository fieldRepository;
    private final HarvestingContractRepository contractRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<OwnerResponse> searchOwners(String search, Boolean active, Pageable pageable) {
        Page<Owner> page = ownerRepository.searchOwners(search, active, pageable);
        return PagedResponse.of(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public OwnerResponse getById(Long id) {
        Owner owner = findOrThrow(id);
        OwnerResponse response = toResponse(owner);
        // Populate summary stats for detail view
        long totalFields = fieldRepository.countByOwnerId(id);
        long activeContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);
        long completedContracts = contractRepository.countByStatus(ContractStatus.COMPLETED);
        return OwnerResponse.builder()
                .id(owner.getId()).name(owner.getName()).phone(owner.getPhone())
                .alternatePhone(owner.getAlternatePhone()).email(owner.getEmail())
                .address(owner.getAddress()).village(owner.getVillage())
                .district(owner.getDistrict()).state(owner.getState())
                .pincode(owner.getPincode()).paymentPreference(owner.getPaymentPreference())
                .bankName(owner.getBankName()).bankAccountNumber(owner.getBankAccountNumber())
                .bankIfsc(owner.getBankIfsc()).upiId(owner.getUpiId())
                .notes(owner.getNotes()).active(owner.isActive())
                .createdAt(owner.getCreatedAt()).updatedAt(owner.getUpdatedAt())
                .totalFields(totalFields)
                .activeContracts(activeContracts)
                .completedContracts(completedContracts)
                .build();
    }

    @Transactional
    public OwnerResponse create(OwnerRequest request) {
        Owner owner = Owner.builder()
                .name(request.getName()).phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone()).email(request.getEmail())
                .address(request.getAddress()).village(request.getVillage())
                .district(request.getDistrict())
                .state(request.getState() != null ? request.getState() : "Karnataka")
                .pincode(request.getPincode())
                .paymentPreference(request.getPaymentPreference())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankIfsc(request.getBankIfsc()).upiId(request.getUpiId())
                .notes(request.getNotes()).active(true)
                .build();
        Owner saved = ownerRepository.save(owner);
        auditLog.log("CREATE", "OWNER", saved.getId(), "Owner created: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public OwnerResponse update(Long id, OwnerRequest request) {
        Owner owner = findOrThrow(id);
        owner.setName(request.getName());
        owner.setPhone(request.getPhone());
        owner.setAlternatePhone(request.getAlternatePhone());
        owner.setEmail(request.getEmail());
        owner.setAddress(request.getAddress());
        owner.setVillage(request.getVillage());
        owner.setDistrict(request.getDistrict());
        if (request.getState() != null) owner.setState(request.getState());
        owner.setPincode(request.getPincode());
        if (request.getPaymentPreference() != null) owner.setPaymentPreference(request.getPaymentPreference());
        owner.setBankName(request.getBankName());
        owner.setBankAccountNumber(request.getBankAccountNumber());
        owner.setBankIfsc(request.getBankIfsc());
        owner.setUpiId(request.getUpiId());
        owner.setNotes(request.getNotes());
        if (request.getActive() != null) owner.setActive(request.getActive());
        Owner saved = ownerRepository.save(owner);
        auditLog.log("UPDATE", "OWNER", saved.getId(), "Owner updated: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public void deactivate(Long id) {
        Owner owner = findOrThrow(id);
        owner.setActive(false);
        ownerRepository.save(owner);
        auditLog.log("DEACTIVATE", "OWNER", id, "Owner deactivated: " + owner.getName());
    }

    private Owner findOrThrow(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner", "id", id));
    }

    private OwnerResponse toResponse(Owner o) {
        return OwnerResponse.builder()
                .id(o.getId()).name(o.getName()).phone(o.getPhone())
                .alternatePhone(o.getAlternatePhone()).email(o.getEmail())
                .address(o.getAddress()).village(o.getVillage())
                .district(o.getDistrict()).state(o.getState())
                .pincode(o.getPincode()).paymentPreference(o.getPaymentPreference())
                .bankName(o.getBankName()).bankAccountNumber(o.getBankAccountNumber())
                .bankIfsc(o.getBankIfsc()).upiId(o.getUpiId())
                .notes(o.getNotes()).active(o.isActive())
                .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt())
                .build();
    }
}
