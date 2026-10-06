package com.arecaharvest.service;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.dto.request.ExpenseRequest;
import com.arecaharvest.dto.response.ExpenseResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.*;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final FieldRepository fieldRepository;
    private final HarvestingContractRepository contractRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> searchExpenses(Long categoryId, Long fieldId,
                                                          LocalDate from, LocalDate to,
                                                          Pageable pageable) {
        return PagedResponse.of(
                expenseRepository.searchExpenses(categoryId, fieldId, from, to, pageable)
                        .map(this::toResponse));
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest req) {
        ExpenseCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", req.getCategoryId()));

        Expense expense = Expense.builder()
                .expenseDate(req.getExpenseDate()).category(category)
                .amount(req.getAmount()).paymentMethod(req.getPaymentMethod())
                .description(req.getDescription()).receiptUrl(req.getReceiptUrl())
                .recordedBy(getCurrentUser())
                .build();

        if (req.getFieldId() != null) {
            expense.setField(fieldRepository.findById(req.getFieldId()).orElse(null));
        }
        if (req.getContractId() != null) {
            expense.setContract(contractRepository.findById(req.getContractId()).orElse(null));
        }
        if (req.getVehicleId() != null) {
            expense.setVehicle(vehicleRepository.findById(req.getVehicleId()).orElse(null));
        }

        Expense saved = expenseRepository.save(expense);
        auditLog.log("CREATE", "EXPENSE", saved.getId(),
                category.getName() + " ₹" + req.getAmount() + " on " + req.getExpenseDate());
        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest req) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        ExpenseCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", req.getCategoryId()));

        expense.setExpenseDate(req.getExpenseDate());
        expense.setCategory(category);
        expense.setAmount(req.getAmount());
        expense.setPaymentMethod(req.getPaymentMethod());
        expense.setDescription(req.getDescription());
        expense.setReceiptUrl(req.getReceiptUrl());
        if (req.getFieldId() != null) expense.setField(fieldRepository.findById(req.getFieldId()).orElse(null));
        if (req.getContractId() != null) expense.setContract(contractRepository.findById(req.getContractId()).orElse(null));
        if (req.getVehicleId() != null) expense.setVehicle(vehicleRepository.findById(req.getVehicleId()).orElse(null));

        Expense saved = expenseRepository.save(expense);
        auditLog.log("UPDATE", "EXPENSE", id, "Expense updated: ₹" + saved.getAmount());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        expenseRepository.delete(expense);
        auditLog.log("DELETE", "EXPENSE", id, "Expense deleted");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return userRepository.findById(ud.getId()).orElse(null);
        }
        return null;
    }

    private ExpenseResponse toResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId()).expenseDate(e.getExpenseDate())
                .categoryId(e.getCategory().getId()).categoryName(e.getCategory().getName())
                .amount(e.getAmount()).paymentMethod(e.getPaymentMethod())
                .description(e.getDescription())
                .fieldId(e.getField() != null ? e.getField().getId() : null)
                .fieldName(e.getField() != null ? e.getField().getFieldName() : null)
                .contractId(e.getContract() != null ? e.getContract().getId() : null)
                .vehicleId(e.getVehicle() != null ? e.getVehicle().getId() : null)
                .receiptUrl(e.getReceiptUrl()).createdAt(e.getCreatedAt())
                .build();
    }
}
