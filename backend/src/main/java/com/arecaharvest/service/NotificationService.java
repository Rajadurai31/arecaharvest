package com.arecaharvest.service;

import com.arecaharvest.entity.*;
import com.arecaharvest.entity.Notification.NotificationType;
import com.arecaharvest.entity.Notification.Priority;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Dispatches automatic in-app notifications on a schedule.
 * External delivery (SMS/WhatsApp/Email) can be added here in Phase 2
 * without changing the notification data model.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WageLedgerRepository wageLedgerRepository;
    private final WorkerRepository workerRepository;
    private final SaleRepository saleRepository;
    private final HarvestingContractRepository contractRepository;
    private final EquipmentRepository equipmentRepository;

    /** Run every day at 07:00 IST */
    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Kolkata")
    @Transactional
    public void runDailyNotifications() {
        log.info("Running daily notification dispatch...");
        notifyPendingWorkerWages();
        notifyBuyerOutstanding();
        notifyContractsNearingCompletion();
        notifyEquipmentMaintenanceDue();
    }

    /** Notify ADMIN users about workers with outstanding balance > ₹1000 */
    @Transactional
    public void notifyPendingWorkerWages() {
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                .toList();
        if (admins.isEmpty()) return;

        workerRepository.findAll().stream()
                .filter(w -> w.getStatus() == com.arecaharvest.entity.enums.WorkerStatus.ACTIVE)
                .forEach(worker -> {
                    BigDecimal balance = wageLedgerRepository.calculateOutstandingBalance(worker.getId());
                    if (balance.compareTo(new BigDecimal("1000")) > 0) {
                        admins.forEach(admin -> createNotification(admin,
                                NotificationType.PENDING_WORKER_WAGES, Priority.HIGH,
                                "Pending Wages: " + worker.getName(),
                                String.format("₹%.2f outstanding for %s. Consider making a payment.",
                                        balance, worker.getName()),
                                "WORKER", worker.getId()));
                    }
                });
    }

    /** Notify ADMIN about buyers with outstanding > ₹5000 */
    @Transactional
    public void notifyBuyerOutstanding() {
        BigDecimal totalOutstanding = saleRepository.totalBuyerOutstanding();
        if (totalOutstanding.compareTo(new BigDecimal("5000")) > 0) {
            userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                    .forEach(admin -> createNotification(admin,
                            NotificationType.BUYER_OUTSTANDING, Priority.NORMAL,
                            "Buyer Outstanding Payments",
                            String.format("Total buyer outstanding: ₹%.2f", totalOutstanding),
                            "SALE", null));
        }
    }

    /** Notify about contracts expected to end within 7 days */
    @Transactional
    public void notifyContractsNearingCompletion() {
        LocalDate threshold = LocalDate.now().plusDays(7);
        List<HarvestingContract> nearingEnd = contractRepository.findActiveContractsNearingCompletion(threshold);
        nearingEnd.forEach(c -> {
            userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r ->
                            r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_SUPERVISOR")))
                    .forEach(user -> createNotification(user,
                            NotificationType.CONTRACT_NEARING_COMPLETION, Priority.NORMAL,
                            "Contract Ending Soon: " + c.getContractNumber(),
                            "Contract " + c.getContractNumber() + " at " + c.getField().getFieldName() +
                            " expected to end by " + c.getExpectedEndDate(),
                            "CONTRACT", c.getId()));
        });
    }

    /** Notify about equipment due for maintenance */
    @Transactional
    public void notifyEquipmentMaintenanceDue() {
        List<Equipment> due = equipmentRepository.findEquipmentDueForMaintenance(
                LocalDate.now().plusDays(7));
        if (!due.isEmpty()) {
            userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                    .forEach(admin -> due.forEach(eq ->
                            createNotification(admin,
                                    NotificationType.EQUIPMENT_MAINTENANCE_DUE, Priority.LOW,
                                    "Maintenance Due: " + eq.getName(),
                                    eq.getName() + " is due for maintenance by " + eq.getNextMaintenanceDate(),
                                    "EQUIPMENT", eq.getId())));
        }
    }

    /** Manual dispatch — called from services when key events happen */
    @Transactional
    public void sendPaymentReceived(User recipient, BigDecimal amount, String fromName) {
        createNotification(recipient, NotificationType.PAYMENT_RECEIVED, Priority.NORMAL,
                "Payment Received",
                String.format("₹%.2f received from %s", amount, fromName),
                null, null);
    }

    @Transactional
    public void sendFieldHarvestComplete(Long contractId, String fieldName, BigDecimal totalKg) {
        userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                .forEach(admin -> createNotification(admin,
                        NotificationType.FIELD_HARVEST_COMPLETE, Priority.NORMAL,
                        "Harvest Complete: " + fieldName,
                        String.format("Harvesting complete at %s. Total: %.3f KG", fieldName, totalKg),
                        "CONTRACT", contractId));
    }

    private void createNotification(User recipient, NotificationType type, Priority priority,
                                     String title, String message, String entityType, Long entityId) {
        // Avoid duplicate notifications: skip if same type+entity already unread today
        boolean alreadyNotified = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                        recipient.getId(), org.springframework.data.domain.PageRequest.of(0, 10))
                .getContent().stream()
                .anyMatch(n -> n.getType() == type
                        && !n.isRead()
                        && java.util.Objects.equals(n.getEntityId(), entityId)
                        && n.getCreatedAt().toLocalDate().equals(LocalDate.now()));

        if (alreadyNotified) return;

        Notification notification = Notification.builder()
                .recipient(recipient).type(type).priority(priority)
                .title(title).message(message)
                .entityType(entityType).entityId(entityId)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }
}
