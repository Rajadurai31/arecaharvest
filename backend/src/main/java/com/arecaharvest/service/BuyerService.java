package com.arecaharvest.service;

import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Buyer;
import com.arecaharvest.entity.BuyerPayment;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.BuyerPaymentRepository;
import com.arecaharvest.repository.BuyerRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BuyerService {

    private final BuyerRepository buyerRepository;
    private final BuyerPaymentRepository buyerPaymentRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<BuyerSummary> searchBuyers(String search, String status, Pageable pageable) {
        return PagedResponse.of(buyerRepository.searchBuyers(search, status, pageable)
                .map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public BuyerSummary getById(Long id) {
        Buyer buyer = findOrThrow(id);
        BigDecimal outstanding = buyerPaymentRepository.calculateOutstandingBalance(id);
        return toSummaryWithOutstanding(buyer, outstanding);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BuyerPayment> getLedger(Long buyerId, Pageable pageable) {
        findOrThrow(buyerId);
        return PagedResponse.of(buyerPaymentRepository.findByBuyerIdOrderByPaymentDateDesc(buyerId, pageable));
    }

    @Transactional
    public BuyerSummary create(BuyerRequest req) {
        Buyer buyer = Buyer.builder()
                .name(req.getName()).phone(req.getPhone())
                .alternatePhone(req.getAlternatePhone()).companyName(req.getCompanyName())
                .address(req.getAddress()).village(req.getVillage()).district(req.getDistrict())
                .state(req.getState() != null ? req.getState() : "Karnataka")
                .paymentTerms(req.getPaymentTerms()).defaultRatePerKg(req.getDefaultRatePerKg())
                .bankName(req.getBankName()).bankAccountNumber(req.getBankAccountNumber())
                .bankIfsc(req.getBankIfsc()).upiId(req.getUpiId())
                .notes(req.getNotes()).status(Buyer.BuyerStatus.ACTIVE)
                .build();
        Buyer saved = buyerRepository.save(buyer);
        auditLog.log("CREATE", "BUYER", saved.getId(), "Buyer created: " + saved.getName());
        return toSummary(saved);
    }

    @Transactional
    public BuyerSummary update(Long id, BuyerRequest req) {
        Buyer buyer = findOrThrow(id);
        buyer.setName(req.getName()); buyer.setPhone(req.getPhone());
        buyer.setAlternatePhone(req.getAlternatePhone()); buyer.setCompanyName(req.getCompanyName());
        buyer.setAddress(req.getAddress()); buyer.setVillage(req.getVillage());
        buyer.setDistrict(req.getDistrict()); buyer.setPaymentTerms(req.getPaymentTerms());
        buyer.setDefaultRatePerKg(req.getDefaultRatePerKg()); buyer.setNotes(req.getNotes());
        if (req.getUpiId() != null) buyer.setUpiId(req.getUpiId());
        Buyer saved = buyerRepository.save(buyer);
        auditLog.log("UPDATE", "BUYER", id, "Buyer updated: " + saved.getName());
        return toSummary(saved);
    }

    private Buyer findOrThrow(Long id) {
        return buyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", "id", id));
    }

    private BuyerSummary toSummary(Buyer b) {
        BigDecimal outstanding = buyerPaymentRepository.calculateOutstandingBalance(b.getId());
        return toSummaryWithOutstanding(b, outstanding);
    }

    private BuyerSummary toSummaryWithOutstanding(Buyer b, BigDecimal outstanding) {
        return new BuyerSummary(b.getId(), b.getName(), b.getPhone(), b.getCompanyName(),
                b.getVillage(), b.getDistrict(), b.getDefaultRatePerKg(),
                b.getPaymentTerms(), b.getStatus().name(), outstanding);
    }

    public record BuyerSummary(Long id, String name, String phone, String companyName,
                                String village, String district, BigDecimal defaultRate,
                                String paymentTerms, String status, BigDecimal outstandingBalance) {}

    @Getter @Setter
    public static class BuyerRequest {
        private String name; private String phone; private String alternatePhone;
        private String companyName; private String address; private String village;
        private String district; private String state; private String paymentTerms;
        private BigDecimal defaultRatePerKg; private String bankName;
        private String bankAccountNumber; private String bankIfsc; private String upiId;
        private String notes;
    }
}
