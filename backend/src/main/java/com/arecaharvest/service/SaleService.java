package com.arecaharvest.service;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.dto.request.BuyerPaymentRequest;
import com.arecaharvest.dto.request.SaleRequest;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.dto.response.SaleResponse;
import com.arecaharvest.entity.*;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.DuplicateResourceException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final BuyerRepository buyerRepository;
    private final InventoryRepository inventoryRepository;
    private final BuyerPaymentRepository buyerPaymentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<SaleResponse> searchSales(Long buyerId, String status,
                                                    LocalDate from, LocalDate to,
                                                    Pageable pageable) {
        return PagedResponse.of(
                saleRepository.searchSales(buyerId, status, from, to, pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public SaleResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Creates a sale with full inventory and ledger integrity.
     * All steps are in one @Transactional block — if any fail, all roll back.
     *
     * Steps:
     * 1. Validate inventory availability
     * 2. Calculate amounts
     * 3. Create sale
     * 4. Reduce inventory
     * 5. Create buyer ledger entry
     */
    @Transactional
    public SaleResponse createSale(SaleRequest req) {
        if (saleRepository.existsBySaleNumber(req.getSaleNumber())) {
            throw new DuplicateResourceException("Sale", "saleNumber", req.getSaleNumber());
        }

        Buyer buyer = buyerRepository.findById(req.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", "id", req.getBuyerId()));

        Inventory inventory = inventoryRepository.findById(req.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", req.getInventoryId()));

        // Step 1: Check inventory availability
        if (req.getQuantityKg().compareTo(inventory.getAvailableQuantity()) > 0) {
            throw new BusinessException(
                String.format("Sale quantity %.3f %s exceeds available inventory %.3f %s",
                        req.getQuantityKg(), req.getQuantityUnit(),
                        inventory.getAvailableQuantity(), inventory.getQuantityUnit()));
        }

        // Step 2: Calculate amounts
        BigDecimal gross = req.getQuantityKg().multiply(req.getRatePerUnit()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal transport = req.getTransportCost() != null ? req.getTransportCost() : BigDecimal.ZERO;
        BigDecimal loading = req.getLoadingCost() != null ? req.getLoadingCost() : BigDecimal.ZERO;
        BigDecimal other = req.getOtherCharges() != null ? req.getOtherCharges() : BigDecimal.ZERO;
        BigDecimal net = gross.subtract(transport).subtract(loading).subtract(other).setScale(2, RoundingMode.HALF_UP);

        // Step 3: Create sale
        Sale sale = Sale.builder()
                .saleNumber(req.getSaleNumber()).buyer(buyer).inventory(inventory)
                .saleDate(req.getSaleDate()).quantityKg(req.getQuantityKg())
                .quantityUnit(req.getQuantityUnit() != null ? req.getQuantityUnit() : "KG")
                .ratePerUnit(req.getRatePerUnit()).grossAmount(gross)
                .transportCost(transport).loadingCost(loading).otherCharges(other)
                .otherChargesDesc(req.getOtherChargesDesc())
                .netAmount(net).paymentStatus(Sale.SalePaymentStatus.PENDING)
                .amountReceived(BigDecimal.ZERO).notes(req.getNotes())
                .build();
        Sale savedSale = saleRepository.save(sale);

        // Step 4: Reduce inventory
        inventory.setQuantitySold(inventory.getQuantitySold().add(req.getQuantityKg()));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(req.getQuantityKg()));
        inventory.setStatus(inventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) == 0
                ? Inventory.InventoryStatus.SOLD : Inventory.InventoryStatus.PARTIALLY_SOLD);
        inventoryRepository.save(inventory);

        // Step 5: Create buyer ledger entry (SALE debit)
        BuyerPayment ledger = BuyerPayment.builder()
                .buyer(buyer).sale(savedSale)
                .paymentDate(req.getSaleDate())
                .transactionType(BuyerPayment.TransactionType.SALE)
                .amount(net).recordedBy(getCurrentUser())
                .remarks("Sale " + req.getSaleNumber())
                .build();
        buyerPaymentRepository.save(ledger);

        auditLog.log("CREATE", "SALE", savedSale.getId(),
                String.format("Sale %s: %.3f KG to %s, net ₹%.2f",
                        req.getSaleNumber(), req.getQuantityKg(), buyer.getName(), net));

        return toResponse(savedSale);
    }

    @Transactional
    public SaleResponse recordBuyerPayment(BuyerPaymentRequest req) {
        Sale sale = req.getSaleId() != null ? findOrThrow(req.getSaleId()) : null;
        if (sale != null) {
            BigDecimal outstanding = sale.getNetAmount().subtract(sale.getAmountReceived());
            if (req.getAmount().compareTo(outstanding) > 0) {
                throw new BusinessException(
                    String.format("Payment ₹%.2f exceeds outstanding ₹%.2f for this sale",
                            req.getAmount(), outstanding));
            }
            sale.setAmountReceived(sale.getAmountReceived().add(req.getAmount()));
            sale.setPaymentStatus(sale.getAmountReceived().compareTo(sale.getNetAmount()) >= 0
                    ? Sale.SalePaymentStatus.PAID : Sale.SalePaymentStatus.PARTIAL);
            saleRepository.save(sale);
        }

        Buyer buyer = buyerRepository.findById(req.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", "id", req.getBuyerId()));

        BuyerPayment payment = BuyerPayment.builder()
                .buyer(buyer).sale(sale)
                .paymentDate(req.getPaymentDate())
                .transactionType(req.getTransactionType())
                .amount(req.getAmount())
                .adjustmentSign(req.getAdjustmentSign())
                .paymentMethod(req.getPaymentMethod())
                .referenceNumber(req.getReferenceNumber())
                .remarks(req.getRemarks())
                .recordedBy(getCurrentUser())
                .build();
        buyerPaymentRepository.save(payment);

        auditLog.log("PAYMENT", "BUYER_PAYMENT", payment.getId(),
                String.format("₹%.2f from %s", req.getAmount(), buyer.getName()));

        return sale != null ? toResponse(sale) : null;
    }

    private Sale findOrThrow(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return userRepository.findById(ud.getId()).orElse(null);
        }
        return null;
    }

    private SaleResponse toResponse(Sale s) {
        return SaleResponse.builder()
                .id(s.getId()).saleNumber(s.getSaleNumber())
                .buyerId(s.getBuyer().getId()).buyerName(s.getBuyer().getName())
                .inventoryId(s.getInventory().getId())
                .saleDate(s.getSaleDate()).quantityKg(s.getQuantityKg())
                .quantityUnit(s.getQuantityUnit()).ratePerUnit(s.getRatePerUnit())
                .grossAmount(s.getGrossAmount()).transportCost(s.getTransportCost())
                .loadingCost(s.getLoadingCost()).otherCharges(s.getOtherCharges())
                .otherChargesDesc(s.getOtherChargesDesc()).netAmount(s.getNetAmount())
                .paymentStatus(s.getPaymentStatus()).amountReceived(s.getAmountReceived())
                .outstanding(s.getNetAmount().subtract(s.getAmountReceived()))
                .notes(s.getNotes()).createdAt(s.getCreatedAt())
                .build();
    }
}
