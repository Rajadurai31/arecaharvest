package com.arecaharvest.service;

import com.arecaharvest.dto.request.SaleRequest;
import com.arecaharvest.entity.*;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.DuplicateResourceException;
import com.arecaharvest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SaleService — Inventory validation")
@ExtendWith(MockitoExtension.class)
class SaleServiceInventoryTest {

    @Mock private SaleRepository saleRepository;
    @Mock private BuyerRepository buyerRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private BuyerPaymentRepository buyerPaymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLog;

    @InjectMocks private SaleService saleService;

    private Buyer buyer;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        buyer = new Buyer();
        buyer.setId(1L);
        buyer.setName("Mangalore Traders");

        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setQuantityReceived(new BigDecimal("1000"));
        inventory.setQuantitySold(new BigDecimal("200"));
        inventory.setAvailableQuantity(new BigDecimal("800"));
        inventory.setQuantityUnit("KG");
        inventory.setStatus(Inventory.InventoryStatus.PARTIALLY_SOLD);
    }

    @Test
    @DisplayName("Sale within available inventory succeeds and reduces stock")
    void sale_withinInventory_succeeds() {
        when(saleRepository.existsBySaleNumber("SAL-001")).thenReturn(false);
        when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        Sale savedSale = new Sale();
        savedSale.setId(1L);
        savedSale.setBuyer(buyer);
        savedSale.setInventory(inventory);
        savedSale.setQuantityKg(new BigDecimal("500"));
        savedSale.setRatePerUnit(new BigDecimal("58.50"));
        savedSale.setGrossAmount(new BigDecimal("29250.00"));
        savedSale.setNetAmount(new BigDecimal("29250.00"));
        savedSale.setAmountReceived(BigDecimal.ZERO);
        savedSale.setPaymentStatus(Sale.SalePaymentStatus.PENDING);
        savedSale.setSaleNumber("SAL-001");
        savedSale.setSaleDate(LocalDate.now());
        savedSale.setQuantityUnit("KG");
        savedSale.setTransportCost(BigDecimal.ZERO);
        savedSale.setLoadingCost(BigDecimal.ZERO);
        savedSale.setOtherCharges(BigDecimal.ZERO);

        when(saleRepository.save(any())).thenReturn(savedSale);
        when(inventoryRepository.save(any())).thenReturn(inventory);

        BuyerPayment bp = new BuyerPayment();
        bp.setId(1L);
        when(buyerPaymentRepository.save(any())).thenReturn(bp);

        SaleRequest req = new SaleRequest();
        req.setSaleNumber("SAL-001");
        req.setBuyerId(1L);
        req.setInventoryId(1L);
        req.setSaleDate(LocalDate.now());
        req.setQuantityKg(new BigDecimal("500"));   // 500 ≤ 800 available ✓
        req.setRatePerUnit(new BigDecimal("58.50"));

        assertThatNoException().isThrownBy(() -> saleService.createSale(req));

        // Verify inventory was updated
        ArgumentCaptor<Inventory> cap = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(cap.capture());
        assertThat(cap.getValue().getQuantitySold()).isEqualByComparingTo(new BigDecimal("700")); // 200 + 500
        assertThat(cap.getValue().getAvailableQuantity()).isEqualByComparingTo(new BigDecimal("300")); // 800 - 500
    }

    @Test
    @DisplayName("Sale exceeding available inventory throws BusinessException")
    void sale_exceedsInventory_throws() {
        when(saleRepository.existsBySaleNumber("SAL-002")).thenReturn(false);
        when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        SaleRequest req = new SaleRequest();
        req.setSaleNumber("SAL-002");
        req.setBuyerId(1L);
        req.setInventoryId(1L);
        req.setSaleDate(LocalDate.now());
        req.setQuantityKg(new BigDecimal("900"));   // 900 > 800 available ✗
        req.setRatePerUnit(new BigDecimal("58.50"));

        assertThatThrownBy(() -> saleService.createSale(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exceeds available inventory");
    }

    @Test
    @DisplayName("Duplicate sale number throws DuplicateResourceException")
    void duplicateSaleNumber_throws() {
        when(saleRepository.existsBySaleNumber("SAL-001")).thenReturn(true);

        SaleRequest req = new SaleRequest();
        req.setSaleNumber("SAL-001");
        req.setBuyerId(1L);
        req.setInventoryId(1L);
        req.setSaleDate(LocalDate.now());
        req.setQuantityKg(new BigDecimal("100"));
        req.setRatePerUnit(new BigDecimal("58.50"));

        assertThatThrownBy(() -> saleService.createSale(req))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
