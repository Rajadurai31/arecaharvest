package com.arecaharvest.service;

import com.arecaharvest.dto.request.ContractRequest;
import com.arecaharvest.dto.response.ContractResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.*;
import com.arecaharvest.entity.enums.ContractStatus;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.DuplicateResourceException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final HarvestingContractRepository contractRepository;
    private final OwnerRepository ownerRepository;
    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final HarvestSessionRepository sessionRepository;
    private final ContractCalculationService calculationService;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<ContractResponse> searchContracts(Long fieldId, Long ownerId,
                                                            ContractStatus status,
                                                            LocalDate fromDate, LocalDate toDate,
                                                            Pageable pageable) {
        Page<HarvestingContract> page = contractRepository.searchContracts(
                fieldId, ownerId, status, fromDate, toDate, pageable);
        return PagedResponse.of(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ContractResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ContractResponse create(ContractRequest request) {
        if (contractRepository.existsByFieldIdAndStatus(request.getFieldId(), ContractStatus.ACTIVE)) {
            throw new BusinessException("Field already has an ACTIVE contract. Complete or cancel it first.");
        }
        if (contractRepository.existsByContractNumber(request.getContractNumber())) {
            throw new DuplicateResourceException("Contract", "contractNumber", request.getContractNumber());
        }
        validateDates(request.getStartDate(), request.getExpectedEndDate());

        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner", "id", request.getOwnerId()));
        Field field = fieldRepository.findById(request.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", request.getFieldId()));

        if (!field.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("Field does not belong to the specified owner");
        }

        HarvestingContract contract = HarvestingContract.builder()
                .owner(owner).field(field)
                .contractNumber(request.getContractNumber())
                .contractType(request.getContractType())
                .rate(request.getRate())
                .fixedAmount(request.getFixedAmount())
                .expectedQuantityKg(request.getExpectedQuantityKg())
                .startDate(request.getStartDate())
                .expectedEndDate(request.getExpectedEndDate())
                .status(request.getStatus() != null ? request.getStatus() : ContractStatus.DRAFT)
                .notes(request.getNotes())
                .build();

        if (request.getSupervisorId() != null) {
            contract.setSupervisor(userRepository.findById(request.getSupervisorId()).orElse(null));
        }

        HarvestingContract saved = contractRepository.save(contract);
        auditLog.log("CREATE", "CONTRACT", saved.getId(), "Contract created: " + saved.getContractNumber());
        return toResponse(saved);
    }

    @Transactional
    public ContractResponse update(Long id, ContractRequest request) {
        HarvestingContract contract = findOrThrow(id);
        if (contract.getStatus() == ContractStatus.COMPLETED || contract.getStatus() == ContractStatus.CANCELLED) {
            throw new BusinessException("Cannot modify a " + contract.getStatus() + " contract");
        }
        validateDates(request.getStartDate(), request.getExpectedEndDate());

        contract.setContractType(request.getContractType());
        contract.setRate(request.getRate());
        contract.setFixedAmount(request.getFixedAmount());
        contract.setExpectedQuantityKg(request.getExpectedQuantityKg());
        contract.setStartDate(request.getStartDate());
        contract.setExpectedEndDate(request.getExpectedEndDate());
        contract.setNotes(request.getNotes());
        if (request.getStatus() != null) contract.setStatus(request.getStatus());
        if (request.getSupervisorId() != null) {
            contract.setSupervisor(userRepository.findById(request.getSupervisorId()).orElse(null));
        }

        HarvestingContract saved = contractRepository.save(contract);
        auditLog.log("UPDATE", "CONTRACT", id, "Contract updated: " + contract.getContractNumber());
        return toResponse(saved);
    }

    @Transactional
    public ContractResponse updateStatus(Long id, ContractStatus status) {
        HarvestingContract contract = findOrThrow(id);
        ContractStatus prev = contract.getStatus();
        contract.setStatus(status);
        if (status == ContractStatus.COMPLETED) {
            contract.setActualEndDate(LocalDate.now());
        }
        contractRepository.save(contract);
        auditLog.log("STATUS_CHANGE", "CONTRACT", id, prev + " → " + status);
        return toResponse(contract);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null) throw new BusinessException("Start date is required");
        if (end != null && end.isBefore(start)) {
            throw new BusinessException("Expected end date cannot be before start date");
        }
    }

    private HarvestingContract findOrThrow(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));
    }

    private boolean existsByContractNumber(String num) {
        return contractRepository.existsByContractNumber(num);
    }

    private ContractResponse toResponse(HarvestingContract c) {
        BigDecimal harvested = sessionRepository.sumQuantityForContract(c.getId());
        BigDecimal expectedRevenue = calculationService.calculateExpectedRevenue(c);
        double progress = calculationService.calculateProgressPercent(c.getExpectedQuantityKg(), harvested);

        return ContractResponse.builder()
                .id(c.getId())
                .ownerId(c.getOwner().getId()).ownerName(c.getOwner().getName())
                .fieldId(c.getField().getId()).fieldName(c.getField().getFieldName())
                .contractNumber(c.getContractNumber())
                .contractType(c.getContractType()).rate(c.getRate())
                .fixedAmount(c.getFixedAmount())
                .expectedQuantityKg(c.getExpectedQuantityKg())
                .startDate(c.getStartDate()).expectedEndDate(c.getExpectedEndDate())
                .actualEndDate(c.getActualEndDate()).status(c.getStatus())
                .supervisorId(c.getSupervisor() != null ? c.getSupervisor().getId() : null)
                .supervisorName(c.getSupervisor() != null ? c.getSupervisor().getFullName() : null)
                .notes(c.getNotes())
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt())
                .expectedRevenue(expectedRevenue)
                .actualHarvestedKg(harvested)
                .progressPercent(progress)
                .build();
    }
}
