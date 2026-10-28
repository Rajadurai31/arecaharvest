package com.arecaharvest.config;

import com.arecaharvest.entity.*;
import com.arecaharvest.entity.enums.*;
import com.arecaharvest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Seeds maximum comprehensive realistic data for the demo (H2) profile so all
 * dashboards, charts, schedules, inventory, sales, finance, and workflows have
 * extensive operational business data ready out-of-the-box.
 */
@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OwnerRepository ownerRepository;
    private final FieldRepository fieldRepository;
    private final WorkerRepository workerRepository;
    private final HarvestingContractRepository contractRepository;
    private final BuyerRepository buyerRepository;
    private final VehicleRepository vehicleRepository;
    private final EquipmentRepository equipmentRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final ExpenseRepository expenseRepository;
    private final HarvestSessionRepository sessionRepository;
    private final HarvestRecordRepository harvestRecordRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRepository saleRepository;
    private final AttendanceRepository attendanceRepository;
    private final WageLedgerRepository wageLedgerRepository;
    private final ScheduleRepository scheduleRepository;
    private final WorkerAssignmentRepository assignmentRepository;
    private final WorkerPaymentRepository workerPaymentRepository;
    private final BuyerPaymentRepository buyerPaymentRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Demo profile — seeding maximum comprehensive operational business data...");

        // ── 1. Roles ──────────────────────────────────────────────────────────
        Role adminRole      = seedRole("ROLE_ADMIN",      "Head Harvester — full access");
        Role supervisorRole = seedRole("ROLE_SUPERVISOR", "Field Supervisor");
        Role workerRole     = seedRole("ROLE_WORKER",     "Harvesting Worker");

        // ── 2. Users ──────────────────────────────────────────────────────────
        User admin = userRepository.findByUsername("admin").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("admin")
                        .email("admin@arecaharvest.com")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .fullName("Rajesh Kumar (Admin)")
                        .phone("9876543210")
                        .active(true)
                        .roles(Set.of(adminRole))
                        .build())
        );

        User sup1 = userRepository.findByUsername("supervisor1").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("supervisor1")
                        .email("sup1@arecaharvest.com")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .fullName("Mohan Das (Supervisor)")
                        .phone("9876543211")
                        .active(true)
                        .roles(Set.of(supervisorRole))
                        .build())
        );

        User sup2 = userRepository.findByUsername("supervisor2").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("supervisor2")
                        .email("sup2@arecaharvest.com")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .fullName("Venkatesh Bhat (Field Lead)")
                        .phone("9876543219")
                        .active(true)
                        .roles(Set.of(supervisorRole))
                        .build())
        );

        User workerUser = userRepository.findByUsername("worker_raju").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("worker_raju")
                        .email("raju@arecaharvest.com")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .fullName("Raju Gowda (Worker)")
                        .phone("9876543212")
                        .active(true)
                        .roles(Set.of(workerRole))
                        .build())
        );

        // ── 3. Expense Categories ─────────────────────────────────────────────
        ExpenseCategory fuelCat = seedExpenseCategory("FUEL", "Fuel for vehicles or equipment");
        ExpenseCategory foodCat = seedExpenseCategory("FOOD", "Meals and refreshments for harvest crews");
        ExpenseCategory equipCat = seedExpenseCategory("EQUIPMENT", "Climbing ropes, ladders, and cutter maintenance");
        ExpenseCategory transportCat = seedExpenseCategory("TRANSPORT", "Transportation of workers and produce");
        ExpenseCategory chemicalsCat = seedExpenseCategory("CHEMICALS", "Bordeaux mixture for koleroga and bio-sprays");
        ExpenseCategory leaseCat = seedExpenseCategory("LEASE_CESS", "APMC market cess and seasonal lease charges");
        ExpenseCategory otherCat = seedExpenseCategory("OTHER", "Miscellaneous operational expenses");

        // ── 4. Owners (12 Landowners across Karnataka Areca Belt) ──────────────
        Owner suresh = seedOwner("Suresh Gowda", "9845001001", "Sakleshpur", "Hassan", PaymentMethod.CASH);
        Owner laxmamma = seedOwner("Laxmamma Hegde", "9845002001", "Mudigere", "Chikkamagaluru", PaymentMethod.UPI);
        Owner narayana = seedOwner("Narayana Poojary", "9845003001", "Sullia", "Dakshina Kannada", PaymentMethod.CASH);
        Owner kavitha = seedOwner("Kavitha Shetty", "9845004001", "Belthangady", "Dakshina Kannada", PaymentMethod.BANK_TRANSFER);
        Owner manjunath = seedOwner("Manjunath Nayak", "9845005001", "Chikkamagaluru", "Chikkamagaluru", PaymentMethod.CASH);
        Owner vishwanath = seedOwner("Vishwanath Hegde", "9845006001", "Sirsi", "Uttara Kannada", PaymentMethod.BANK_TRANSFER);
        Owner shridhar = seedOwner("Shridhar Rao", "9845007001", "Thirthahalli", "Shimoga", PaymentMethod.UPI);
        Owner subraya = seedOwner("Subraya Bhat", "9845008001", "Sagar", "Shimoga", PaymentMethod.BANK_TRANSFER);
        Owner chandra = seedOwner("Chandrashekar Patil", "9845009001", "Channagiri", "Davanagere", PaymentMethod.CASH);
        Owner radha = seedOwner("Radhakrishna Shenoy", "9845010001", "Karkala", "Udupi", PaymentMethod.BANK_TRANSFER);
        Owner jayaram = seedOwner("Jayaram Shetty", "9845011001", "Puttur", "Dakshina Kannada", PaymentMethod.UPI);
        Owner anand = seedOwner("Anand Kurubara", "9845012001", "Tarikere", "Chikkamagaluru", PaymentMethod.CASH);

        // ── 5. Fields (18 Plantation Plots) ────────────────────────────────────
        Field f1 = seedField(suresh, "Suresh North Plot", "Sakleshpur", "Hassan", new BigDecimal("2.50"), 120, new BigDecimal("5000.00"), FieldStatus.ACTIVE);
        Field f2 = seedField(suresh, "Suresh South Plot", "Sakleshpur", "Hassan", new BigDecimal("1.75"), 85, new BigDecimal("3500.00"), FieldStatus.ACTIVE);
        Field f3 = seedField(laxmamma, "Laxmamma Main Field", "Mudigere", "Chikkamagaluru", new BigDecimal("3.00"), 160, new BigDecimal("6500.00"), FieldStatus.HARVESTING);
        Field f4 = seedField(laxmamma, "Laxmamma Riverside Block", "Mudigere", "Chikkamagaluru", new BigDecimal("2.20"), 110, new BigDecimal("4800.00"), FieldStatus.ACTIVE);
        Field f5 = seedField(narayana, "Poojary Field A", "Sullia", "Dakshina Kannada", new BigDecimal("1.50"), 70, new BigDecimal("2800.00"), FieldStatus.ACTIVE);
        Field f6 = seedField(narayana, "Poojary Hillside Plot", "Sullia", "Dakshina Kannada", new BigDecimal("2.80"), 140, new BigDecimal("5600.00"), FieldStatus.HARVESTING);
        Field f7 = seedField(kavitha, "Kavitha Estate Block 1", "Belthangady", "Dakshina Kannada", new BigDecimal("4.00"), 200, new BigDecimal("8000.00"), FieldStatus.HARVESTING);
        Field f8 = seedField(kavitha, "Kavitha Estate Block 2", "Belthangady", "Dakshina Kannada", new BigDecimal("3.50"), 175, new BigDecimal("7200.00"), FieldStatus.ACTIVE);
        Field f9 = seedField(manjunath, "Manjunath Farm 1", "Chikkamagaluru", "Chikkamagaluru", new BigDecimal("1.25"), 60, new BigDecimal("2500.00"), FieldStatus.ACTIVE);
        Field f10 = seedField(manjunath, "Manjunath Valley Plot", "Chikkamagaluru", "Chikkamagaluru", new BigDecimal("2.00"), 95, new BigDecimal("4000.00"), FieldStatus.ACTIVE);
        Field f11 = seedField(vishwanath, "Vishwanath Sirsi Valley", "Sirsi", "Uttara Kannada", new BigDecimal("5.00"), 250, new BigDecimal("11000.00"), FieldStatus.HARVESTING);
        Field f12 = seedField(vishwanath, "Vishwanath Hill Crest", "Sirsi", "Uttara Kannada", new BigDecimal("3.20"), 155, new BigDecimal("6800.00"), FieldStatus.ACTIVE);
        Field f13 = seedField(shridhar, "Thirthahalli Tunga View", "Thirthahalli", "Shimoga", new BigDecimal("4.50"), 220, new BigDecimal("9500.00"), FieldStatus.ACTIVE);
        Field f14 = seedField(subraya, "Sagar Green Canopy", "Sagar", "Shimoga", new BigDecimal("3.80"), 190, new BigDecimal("8200.00"), FieldStatus.HARVESTING);
        Field f15 = seedField(chandra, "Channagiri Red Soil Farm", "Channagiri", "Davanagere", new BigDecimal("6.00"), 310, new BigDecimal("13500.00"), FieldStatus.ACTIVE);
        Field f16 = seedField(radha, "Karkala Hilltop Grove", "Karkala", "Udupi", new BigDecimal("2.75"), 130, new BigDecimal("5500.00"), FieldStatus.ACTIVE);
        Field f17 = seedField(jayaram, "Puttur Classic Betel Palm", "Puttur", "Dakshina Kannada", new BigDecimal("3.60"), 180, new BigDecimal("7600.00"), FieldStatus.HARVESTING);
        Field f18 = seedField(anand, "Tarikere Foothill Plot", "Tarikere", "Chikkamagaluru", new BigDecimal("4.20"), 210, new BigDecimal("8900.00"), FieldStatus.ACTIVE);

        // ── 6. Workers (16 Skilled Laborers across All Roles) ─────────────────
        Worker w1  = seedWorker("Raju Gowda", "9876543212", "Sakleshpur", SkillType.CLIMBER, new BigDecimal("8.0"), new BigDecimal("900.00"), workerUser);
        Worker w2  = seedWorker("Shiva Kumar", "9876500001", "Mudigere", SkillType.CUTTER, new BigDecimal("5.0"), new BigDecimal("850.00"), null);
        Worker w3  = seedWorker("Prasad M", "9876500002", "Sullia", SkillType.CLIMBER, new BigDecimal("12.0"), new BigDecimal("950.00"), null);
        Worker w4  = seedWorker("Geetha Bai", "9876500003", "Belthangady", SkillType.COLLECTOR, new BigDecimal("3.0"), new BigDecimal("750.00"), null);
        Worker w5  = seedWorker("Ramesh Naik", "9876500004", "Chikkamagaluru", SkillType.LOADER, new BigDecimal("6.0"), new BigDecimal("800.00"), null);
        Worker w6  = seedWorker("Venu Gopal", "9876500005", "Sakleshpur", SkillType.CUTTER, new BigDecimal("4.0"), new BigDecimal("825.00"), null);
        Worker w7  = seedWorker("Suma Rani", "9876500007", "Belthangady", SkillType.COLLECTOR, new BigDecimal("2.5"), new BigDecimal("700.00"), null);
        Worker w8  = seedWorker("Krishnamurthy R", "9876500009", "Hassan", SkillType.DRIVER, new BigDecimal("7.0"), new BigDecimal("875.00"), null);
        Worker w9  = seedWorker("Sanjeeva Poojary", "9876500010", "Sirsi", SkillType.CLIMBER, new BigDecimal("10.0"), new BigDecimal("950.00"), null);
        Worker w10 = seedWorker("Manjappa Gowda", "9876500011", "Thirthahalli", SkillType.CUTTER, new BigDecimal("8.0"), new BigDecimal("850.00"), null);
        Worker w11 = seedWorker("Lakshmi Devi", "9876500012", "Sagar", SkillType.COLLECTOR, new BigDecimal("4.0"), new BigDecimal("750.00"), null);
        Worker w12 = seedWorker("Basavaraj Patil", "9876500013", "Channagiri", SkillType.LOADER, new BigDecimal("5.0"), new BigDecimal("800.00"), null);
        Worker w13 = seedWorker("Venkatesh Naik", "9876500014", "Sirsi", SkillType.CLIMBER, new BigDecimal("7.0"), new BigDecimal("900.00"), null);
        Worker w14 = seedWorker("Nagaraj S", "9876500015", "Shimoga", SkillType.DRIVER, new BigDecimal("9.0"), new BigDecimal("875.00"), null);
        Worker w15 = seedWorker("Parvathi Bai", "9876500016", "Puttur", SkillType.COLLECTOR, new BigDecimal("3.0"), new BigDecimal("720.00"), null);
        Worker w16 = seedWorker("Chennappa K", "9876500017", "Karkala", SkillType.CLIMBER, new BigDecimal("11.0"), new BigDecimal("950.00"), null);

        // ── 7. Contracts (12 Contracts) ───────────────────────────────────────
        HarvestingContract c1 = seedContract(laxmamma, f3, "CTR-2026-001", ContractType.PER_KG, new BigDecimal("4.50"), new BigDecimal("6500.00"), LocalDate.now().minusDays(30), LocalDate.now().plusDays(30), ContractStatus.ACTIVE, sup1);
        HarvestingContract c2 = seedContract(kavitha, f7, "CTR-2026-002", ContractType.PER_TREE, new BigDecimal("120.00"), new BigDecimal("8000.00"), LocalDate.now().minusDays(40), LocalDate.now().plusDays(20), ContractStatus.ACTIVE, sup1);
        HarvestingContract c3 = seedContract(suresh, f1, "CTR-2026-003", ContractType.PER_KG, new BigDecimal("4.25"), new BigDecimal("5000.00"), LocalDate.now().minusDays(15), LocalDate.now().plusDays(45), ContractStatus.ACTIVE, sup1);
        HarvestingContract c4 = seedContract(vishwanath, f11, "CTR-2026-004", ContractType.PER_KG, new BigDecimal("4.75"), new BigDecimal("11000.00"), LocalDate.now().minusDays(20), LocalDate.now().plusDays(40), ContractStatus.ACTIVE, sup2);
        HarvestingContract c5 = seedContract(narayana, f6, "CTR-2026-005", ContractType.PER_TREE, new BigDecimal("115.00"), new BigDecimal("5600.00"), LocalDate.now().minusDays(10), LocalDate.now().plusDays(50), ContractStatus.ACTIVE, sup1);
        HarvestingContract c6 = seedContract(subraya, f14, "CTR-2026-006", ContractType.FIXED_AMOUNT, BigDecimal.ZERO, new BigDecimal("8200.00"), LocalDate.now().minusDays(5), LocalDate.now().plusDays(55), ContractStatus.ACTIVE, sup2);
        HarvestingContract c7 = seedContract(chandra, f15, "CTR-2026-007", ContractType.PER_KG, new BigDecimal("4.60"), new BigDecimal("13500.00"), LocalDate.now().minusDays(12), LocalDate.now().plusDays(60), ContractStatus.ACTIVE, sup2);
        HarvestingContract c8 = seedContract(jayaram, f17, "CTR-2026-008", ContractType.PER_TREE, new BigDecimal("125.00"), new BigDecimal("7600.00"), LocalDate.now().minusDays(8), LocalDate.now().plusDays(45), ContractStatus.ACTIVE, sup1);
        HarvestingContract c9 = seedContract(shridhar, f13, "CTR-2026-009", ContractType.PER_KG, new BigDecimal("4.40"), new BigDecimal("9500.00"), LocalDate.now().plusDays(2), LocalDate.now().plusDays(60), ContractStatus.DRAFT, sup2);
        HarvestingContract c10 = seedContract(manjunath, f10, "CTR-2026-010", ContractType.FIXED_AMOUNT, BigDecimal.ZERO, new BigDecimal("4000.00"), LocalDate.now().plusDays(5), LocalDate.now().plusDays(65), ContractStatus.DRAFT, sup1);
        HarvestingContract c11 = seedContract(suresh, f2, "CTR-2025-098", ContractType.PER_KG, new BigDecimal("4.20"), new BigDecimal("3500.00"), LocalDate.now().minusDays(90), LocalDate.now().minusDays(30), ContractStatus.COMPLETED, sup1);
        HarvestingContract c12 = seedContract(kavitha, f8, "CTR-2025-099", ContractType.PER_TREE, new BigDecimal("110.00"), new BigDecimal("7200.00"), LocalDate.now().minusDays(100), LocalDate.now().minusDays(40), ContractStatus.COMPLETED, sup1);

        // ── 8. Buyers (8 Reputable APMC & Private Buyers) ─────────────────────
        Buyer b1 = seedBuyer("Mangalore Areca Traders", "9844100001", "Mangalore Areca Traders Pvt Ltd", "Mangalore", "Dakshina Kannada", "Net 7 days", new BigDecimal("58.50"));
        Buyer b2 = seedBuyer("Hassan Nut Buyers", "9844100002", "Hassan Traders Co", "Hassan", "Hassan", "Immediate", new BigDecimal("57.00"));
        Buyer b3 = seedBuyer("Udupi Fresh Produce", "9844100003", "Udupi Fresh Pvt Ltd", "Udupi", "Udupi", "Net 15 days", new BigDecimal("59.00"));
        Buyer b4 = seedBuyer("Shimoga Supari Merchants", "9844100004", "Shimoga APMC Commission Agents", "Shimoga", "Shimoga", "Net 10 days", new BigDecimal("60.50"));
        Buyer b5 = seedBuyer("Sirsi TSS Cooperative", "9844100005", "Totgars Cooperative Sale Society Ltd", "Sirsi", "Uttara Kannada", "Immediate", new BigDecimal("62.00"));
        Buyer b6 = seedBuyer("CAMPCO Limited", "9844100006", "Central Arecanut & Cocoa Marketing Co-op", "Mangalore", "Dakshina Kannada", "Net 30 days", new BigDecimal("59.50"));
        Buyer b7 = seedBuyer("Bangalore Betel Emporium", "9844100007", "Bangalore Dry Fruit & Nut Exporters", "Bangalore", "Bangalore Urban", "Net 14 days", new BigDecimal("61.00"));
        Buyer b8 = seedBuyer("Channagiri Rashi Mandi", "9844100008", "Channagiri APMC Yard Traders", "Channagiri", "Davanagere", "Immediate", new BigDecimal("58.00"));

        // ── 9. Vehicles & Equipment ───────────────────────────────────────────
        Vehicle v1 = seedVehicle("KA-13-AB-1234", Vehicle.VehicleType.TRUCK, "TATA", "Ace Gold Diesel", 2022, w8, Vehicle.FuelType.DIESEL);
        Vehicle v2 = seedVehicle("KA-13-CD-5678", Vehicle.VehicleType.PICKUP, "Mahindra", "Bolero Maxi Truck", 2020, w14, Vehicle.FuelType.DIESEL);
        Vehicle v3 = seedVehicle("KA-13-EF-9012", Vehicle.VehicleType.BIKE, "Hero", "Splendor Plus", 2023, null, Vehicle.FuelType.PETROL);
        Vehicle v4 = seedVehicle("KA-14-GH-3456", Vehicle.VehicleType.TRUCK, "Eicher", "Pro 2049", 2021, null, Vehicle.FuelType.DIESEL);
        Vehicle v5 = seedVehicle("KA-20-JK-7890", Vehicle.VehicleType.PICKUP, "Mahindra", "Bolero Camper 4x4", 2023, null, Vehicle.FuelType.DIESEL);

        seedEquipment("Bamboo Ladders 15ft", Equipment.EquipmentCategory.LADDER, 12, new BigDecimal("1500.00"));
        seedEquipment("Nylon Climbing Ropes 20m", Equipment.EquipmentCategory.ROPE, 25, new BigDecimal("450.00"));
        seedEquipment("Forged Areca Cutters", Equipment.EquipmentCategory.CUTTER, 16, new BigDecimal("850.00"));
        seedEquipment("Heavy-duty Jute Bags", Equipment.EquipmentCategory.BAGS, 450, new BigDecimal("40.00"));
        seedEquipment("Climbing Safety Helmets", Equipment.EquipmentCategory.SAFETY_EQUIPMENT, 30, new BigDecimal("250.00"));
        seedEquipment("Carbon-Fibre Telescopic Pole 40ft", Equipment.EquipmentCategory.OTHER, 4, new BigDecimal("8500.00"));
        seedEquipment("Digital Arecanut Moisture Meter", Equipment.EquipmentCategory.OTHER, 3, new BigDecimal("4200.00"));
        seedEquipment("Harvest Heavy Canvas Tarpaulins", Equipment.EquipmentCategory.BAGS, 20, new BigDecimal("1200.00"));

        // ── 10. Work Schedules (Today, Upcoming, and Completed) ───────────────
        if (scheduleRepository.count() == 0) {
            LocalDate today = LocalDate.now();

            // Today's Work Schedules
            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today)
                    .field(f3)
                    .contract(c1)
                    .supervisor(sup1)
                    .vehicle(v1)
                    .plannedStartTime(LocalTime.of(7, 0))
                    .plannedEndTime(LocalTime.of(16, 30))
                    .expectedQuantityKg(new BigDecimal("2200.000"))
                    .expectedWorkerCount(5)
                    .status(Schedule.ScheduleStatus.IN_PROGRESS)
                    .notes("Prime harvesting session at Laxmamma Field Block A. Morning climbers ready.")
                    .workers(new HashSet<>(Set.of(w1, w2, w4, w5, w8)))
                    .build());

            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today)
                    .field(f7)
                    .contract(c2)
                    .supervisor(sup1)
                    .vehicle(v2)
                    .plannedStartTime(LocalTime.of(8, 0))
                    .plannedEndTime(LocalTime.of(17, 0))
                    .expectedQuantityKg(new BigDecimal("1800.000"))
                    .expectedWorkerCount(4)
                    .status(Schedule.ScheduleStatus.PLANNED)
                    .notes("Belthangady high canopy harvest. Inspection of climber safety harnesses required.")
                    .workers(new HashSet<>(Set.of(w3, w6, w7, w14)))
                    .build());

            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today)
                    .field(f11)
                    .contract(c4)
                    .supervisor(sup2)
                    .vehicle(v4)
                    .plannedStartTime(LocalTime.of(7, 30))
                    .plannedEndTime(LocalTime.of(16, 0))
                    .expectedQuantityKg(new BigDecimal("2800.000"))
                    .expectedWorkerCount(5)
                    .status(Schedule.ScheduleStatus.IN_PROGRESS)
                    .notes("Sirsi valley slope blocks. Excellent ripe nut bunches.")
                    .workers(new HashSet<>(Set.of(w9, w10, w11, w12, w13)))
                    .build());

            // Upcoming Work Schedules
            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today.plusDays(1))
                    .field(f6)
                    .contract(c5)
                    .supervisor(sup1)
                    .vehicle(v1)
                    .plannedStartTime(LocalTime.of(7, 30))
                    .plannedEndTime(LocalTime.of(16, 0))
                    .expectedQuantityKg(new BigDecimal("1900.000"))
                    .expectedWorkerCount(4)
                    .status(Schedule.ScheduleStatus.PLANNED)
                    .notes("Sullia hillside plot harvest batch 2.")
                    .workers(new HashSet<>(Set.of(w3, w4, w6, w8)))
                    .build());

            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today.plusDays(2))
                    .field(f14)
                    .contract(c6)
                    .supervisor(sup2)
                    .vehicle(v5)
                    .plannedStartTime(LocalTime.of(8, 0))
                    .plannedEndTime(LocalTime.of(16, 30))
                    .expectedQuantityKg(new BigDecimal("2400.000"))
                    .expectedWorkerCount(5)
                    .status(Schedule.ScheduleStatus.PLANNED)
                    .notes("Sagar Green Canopy estate. Full crew scheduled.")
                    .workers(new HashSet<>(Set.of(w9, w10, w11, w12, w13)))
                    .build());

            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today.plusDays(3))
                    .field(f17)
                    .contract(c8)
                    .supervisor(sup1)
                    .vehicle(v2)
                    .plannedStartTime(LocalTime.of(7, 30))
                    .plannedEndTime(LocalTime.of(17, 0))
                    .expectedQuantityKg(new BigDecimal("2100.000"))
                    .expectedWorkerCount(4)
                    .status(Schedule.ScheduleStatus.PLANNED)
                    .notes("Puttur betel palm grove - second round.")
                    .workers(new HashSet<>(Set.of(w1, w2, w7, w16)))
                    .build());

            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today.plusDays(4))
                    .field(f15)
                    .contract(c7)
                    .supervisor(sup2)
                    .vehicle(v4)
                    .plannedStartTime(LocalTime.of(7, 0))
                    .plannedEndTime(LocalTime.of(17, 0))
                    .expectedQuantityKg(new BigDecimal("3500.000"))
                    .expectedWorkerCount(6)
                    .status(Schedule.ScheduleStatus.PLANNED)
                    .notes("Channagiri high yield red soil farm large harvest.")
                    .workers(new HashSet<>(Set.of(w9, w10, w11, w12, w13, w14)))
                    .build());

            // Past Completed Work Schedules
            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today.minusDays(1))
                    .field(f1)
                    .contract(c3)
                    .supervisor(sup1)
                    .vehicle(v1)
                    .plannedStartTime(LocalTime.of(7, 30))
                    .plannedEndTime(LocalTime.of(16, 0))
                    .expectedQuantityKg(new BigDecimal("2500.000"))
                    .expectedWorkerCount(4)
                    .status(Schedule.ScheduleStatus.COMPLETED)
                    .notes("Completed successfully with 50 bags.")
                    .workers(new HashSet<>(Set.of(w1, w2, w5, w8)))
                    .build());

            scheduleRepository.save(Schedule.builder()
                    .scheduleDate(today.minusDays(4))
                    .field(f3)
                    .contract(c1)
                    .supervisor(sup1)
                    .vehicle(v1)
                    .plannedStartTime(LocalTime.of(7, 0))
                    .plannedEndTime(LocalTime.of(17, 0))
                    .expectedQuantityKg(new BigDecimal("3500.000"))
                    .expectedWorkerCount(5)
                    .status(Schedule.ScheduleStatus.COMPLETED)
                    .notes("Completed 70 bags harvest. Quality green arecanut.")
                    .workers(new HashSet<>(Set.of(w1, w2, w4, w5, w8)))
                    .build());
        }

        // ── 11. Worker Field Assignments ──────────────────────────────────────
        if (assignmentRepository.count() == 0) {
            LocalDate today = LocalDate.now();
            seedAssignment(w1, f3, c1, today, SkillType.CLIMBER, admin, "Lead climber on primary canopy");
            seedAssignment(w2, f3, c1, today, SkillType.CUTTER, admin, "Bunch separation & sorting");
            seedAssignment(w4, f3, c1, today, SkillType.COLLECTOR, admin, "Bag filling and tagging");
            seedAssignment(w5, f3, c1, today, SkillType.LOADER, admin, "Vehicle loading at Sakleshpur field");
            seedAssignment(w8, f3, c1, today, SkillType.DRIVER, admin, "Tata Ace truck driver");

            seedAssignment(w3, f7, c2, today, SkillType.CLIMBER, sup1, "Belthangady block 1 climbers");
            seedAssignment(w6, f7, c2, today, SkillType.CUTTER, sup1, "Cutting and grading supervisor");
            seedAssignment(w7, f7, c2, today, SkillType.COLLECTOR, sup1, "Field collection crew");
            seedAssignment(w14, f7, c2, today, SkillType.DRIVER, sup1, "Bolero truck transport to hub");

            seedAssignment(w9, f11, c4, today, SkillType.CLIMBER, sup2, "Sirsi deep valley climber lead");
            seedAssignment(w10, f11, c4, today, SkillType.CUTTER, sup2, "Cutter specialist");
            seedAssignment(w11, f11, c4, today, SkillType.COLLECTOR, sup2, "Collector and net handler");
            seedAssignment(w12, f11, c4, today, SkillType.LOADER, sup2, "Loading produce on Eicher truck");
        }

        // ── 12. Operational Harvest Sessions, Inventory & Commercial Sales ────
        if (sessionRepository.count() == 0) {
            LocalDate date1 = LocalDate.now().minusDays(8);
            HarvestSession s1 = sessionRepository.save(HarvestSession.builder()
                    .sessionDate(date1)
                    .field(f3)
                    .contract(c1)
                    .supervisor(sup1)
                    .totalBags(70)
                    .totalQuantity(new BigDecimal("3500.000"))
                    .quantityUnit("KG")
                    .workerCount(5)
                    .status(HarvestSession.SessionStatus.COMPLETED)
                    .workers(Set.of(w1, w2, w4, w5, w8))
                    .build());

            harvestRecordRepository.save(HarvestRecord.builder()
                    .harvestSession(s1)
                    .field(f3)
                    .contract(c1)
                    .owner(laxmamma)
                    .recordDate(date1)
                    .bagsCount(70)
                    .quantity(new BigDecimal("3500.000"))
                    .quantityUnit("KG")
                    .workerCount(5)
                    .remarks("Initial prime harvest batch from Laxmamma field - Sakleshpur")
                    .build());

            Inventory inv1 = inventoryRepository.save(Inventory.builder()
                    .field(f3)
                    .contract(c1)
                    .quantityReceived(new BigDecimal("3500.000"))
                    .quantitySold(new BigDecimal("2200.000"))
                    .availableQuantity(new BigDecimal("1300.000"))
                    .quantityUnit("KG")
                    .storageLocation("Godown A, Sakleshpur")
                    .receivedDate(date1)
                    .status(Inventory.InventoryStatus.PARTIALLY_SOLD)
                    .notes("Quality A green harvested areca lot #1")
                    .build());

            Sale sale1 = saleRepository.save(Sale.builder()
                    .saleNumber("SALE-2026-001")
                    .buyer(b1)
                    .inventory(inv1)
                    .saleDate(date1.plusDays(2))
                    .quantityKg(new BigDecimal("2200.000"))
                    .quantityUnit("KG")
                    .ratePerUnit(new BigDecimal("58.5000"))
                    .grossAmount(new BigDecimal("128700.00"))
                    .transportCost(new BigDecimal("2500.00"))
                    .loadingCost(new BigDecimal("1000.00"))
                    .otherCharges(BigDecimal.ZERO)
                    .netAmount(new BigDecimal("125200.00"))
                    .paymentStatus(Sale.SalePaymentStatus.PAID)
                    .amountReceived(new BigDecimal("125200.00"))
                    .notes("Direct dispatch to Mangalore Areca Traders central yard")
                    .build());

            recordBuyerTransaction(b1, sale1, date1.plusDays(2), BuyerPayment.TransactionType.SALE, new BigDecimal("125200.00"), 1, null, "INV-001", "Dispatched to Mangalore port warehouse", admin);
            recordBuyerTransaction(b1, sale1, date1.plusDays(3), BuyerPayment.TransactionType.PAYMENT, new BigDecimal("125200.00"), -1, PaymentMethod.BANK_TRANSFER, "RTGS/SBIN8293710", "Full payment received via RTGS", admin);

            // Session 2 - Suresh North Plot
            LocalDate date2 = LocalDate.now().minusDays(5);
            HarvestSession s2 = sessionRepository.save(HarvestSession.builder()
                    .sessionDate(date2)
                    .field(f1)
                    .contract(c3)
                    .supervisor(sup1)
                    .totalBags(52)
                    .totalQuantity(new BigDecimal("2600.000"))
                    .quantityUnit("KG")
                    .workerCount(4)
                    .status(HarvestSession.SessionStatus.COMPLETED)
                    .workers(Set.of(w1, w3, w5, w6))
                    .build());

            harvestRecordRepository.save(HarvestRecord.builder()
                    .harvestSession(s2)
                    .field(f1)
                    .contract(c3)
                    .owner(suresh)
                    .recordDate(date2)
                    .bagsCount(52)
                    .quantity(new BigDecimal("2600.000"))
                    .quantityUnit("KG")
                    .workerCount(4)
                    .remarks("North plot morning harvest - excellent moisture level")
                    .build());

            Inventory inv2 = inventoryRepository.save(Inventory.builder()
                    .field(f1)
                    .contract(c3)
                    .quantityReceived(new BigDecimal("2600.000"))
                    .quantitySold(new BigDecimal("1500.000"))
                    .availableQuantity(new BigDecimal("1100.000"))
                    .quantityUnit("KG")
                    .storageLocation("Godown B, Sakleshpur")
                    .receivedDate(date2)
                    .status(Inventory.InventoryStatus.PARTIALLY_SOLD)
                    .notes("Fresh stock ready for auction/direct sale")
                    .build());

            Sale sale2 = saleRepository.save(Sale.builder()
                    .saleNumber("SALE-2026-002")
                    .buyer(b2)
                    .inventory(inv2)
                    .saleDate(date2.plusDays(1))
                    .quantityKg(new BigDecimal("1500.000"))
                    .quantityUnit("KG")
                    .ratePerUnit(new BigDecimal("57.0000"))
                    .grossAmount(new BigDecimal("85500.00"))
                    .transportCost(new BigDecimal("1500.00"))
                    .loadingCost(new BigDecimal("800.00"))
                    .otherCharges(BigDecimal.ZERO)
                    .netAmount(new BigDecimal("83200.00"))
                    .paymentStatus(Sale.SalePaymentStatus.PARTIAL)
                    .amountReceived(new BigDecimal("50000.00"))
                    .notes("Hassan buyer consignment - advance received")
                    .build());

            recordBuyerTransaction(b2, sale2, date2.plusDays(1), BuyerPayment.TransactionType.SALE, new BigDecimal("83200.00"), 1, null, "INV-002", "Delivered to Hassan mandi", admin);
            recordBuyerTransaction(b2, sale2, date2.plusDays(1), BuyerPayment.TransactionType.PAYMENT, new BigDecimal("50000.00"), -1, PaymentMethod.UPI, "UPI/294810293", "Part payment via Google Pay", admin);

            // Session 3 - Sirsi Valley (Vishwanath)
            LocalDate date3 = LocalDate.now().minusDays(3);
            HarvestSession s3 = sessionRepository.save(HarvestSession.builder()
                    .sessionDate(date3)
                    .field(f11)
                    .contract(c4)
                    .supervisor(sup2)
                    .totalBags(80)
                    .totalQuantity(new BigDecimal("4000.000"))
                    .quantityUnit("KG")
                    .workerCount(6)
                    .status(HarvestSession.SessionStatus.COMPLETED)
                    .workers(Set.of(w9, w10, w11, w12, w13, w14))
                    .build());

            harvestRecordRepository.save(HarvestRecord.builder()
                    .harvestSession(s3)
                    .field(f11)
                    .contract(c4)
                    .owner(vishwanath)
                    .recordDate(date3)
                    .bagsCount(80)
                    .quantity(new BigDecimal("4000.000"))
                    .quantityUnit("KG")
                    .workerCount(6)
                    .remarks("Sirsi TSS grade A harvest session")
                    .build());

            Inventory inv3 = inventoryRepository.save(Inventory.builder()
                    .field(f11)
                    .contract(c4)
                    .quantityReceived(new BigDecimal("4000.000"))
                    .quantitySold(new BigDecimal("3000.000"))
                    .availableQuantity(new BigDecimal("1000.000"))
                    .quantityUnit("KG")
                    .storageLocation("Central Processing Hub, Sirsi")
                    .receivedDate(date3)
                    .status(Inventory.InventoryStatus.PARTIALLY_SOLD)
                    .notes("Top grade Sirsi TSS tender arecanut")
                    .build());

            Sale sale3 = saleRepository.save(Sale.builder()
                    .saleNumber("SALE-2026-003")
                    .buyer(b5)
                    .inventory(inv3)
                    .saleDate(date3.plusDays(1))
                    .quantityKg(new BigDecimal("3000.000"))
                    .quantityUnit("KG")
                    .ratePerUnit(new BigDecimal("62.0000"))
                    .grossAmount(new BigDecimal("186000.00"))
                    .transportCost(new BigDecimal("3000.00"))
                    .loadingCost(new BigDecimal("1200.00"))
                    .otherCharges(BigDecimal.ZERO)
                    .netAmount(new BigDecimal("181800.00"))
                    .paymentStatus(Sale.SalePaymentStatus.PAID)
                    .amountReceived(new BigDecimal("181800.00"))
                    .notes("Direct auction settlement at Totgars Cooperative Sirsi")
                    .build());

            recordBuyerTransaction(b5, sale3, date3.plusDays(1), BuyerPayment.TransactionType.SALE, new BigDecimal("181800.00"), 1, null, "INV-003", "Sold via TSS auction", admin);
            recordBuyerTransaction(b5, sale3, date3.plusDays(2), BuyerPayment.TransactionType.PAYMENT, new BigDecimal("181800.00"), -1, PaymentMethod.BANK_TRANSFER, "NEFT/KVB9102934", "TSS direct cooperative settlement", admin);

            // Session 4 - Belthangady Kavitha Estate
            LocalDate date4 = LocalDate.now().minusDays(2);
            HarvestSession s4 = sessionRepository.save(HarvestSession.builder()
                    .sessionDate(date4)
                    .field(f7)
                    .contract(c2)
                    .supervisor(sup1)
                    .totalBags(65)
                    .totalQuantity(new BigDecimal("3250.000"))
                    .quantityUnit("KG")
                    .workerCount(5)
                    .status(HarvestSession.SessionStatus.COMPLETED)
                    .workers(Set.of(w3, w6, w7, w8, w16))
                    .build());

            harvestRecordRepository.save(HarvestRecord.builder()
                    .harvestSession(s4)
                    .field(f7)
                    .contract(c2)
                    .owner(kavitha)
                    .recordDate(date4)
                    .bagsCount(65)
                    .quantity(new BigDecimal("3250.000"))
                    .quantityUnit("KG")
                    .workerCount(5)
                    .remarks("Kavitha estate block 1 - high density canopy")
                    .build());

            Inventory inv4 = inventoryRepository.save(Inventory.builder()
                    .field(f7)
                    .contract(c2)
                    .quantityReceived(new BigDecimal("3250.000"))
                    .quantitySold(BigDecimal.ZERO)
                    .availableQuantity(new BigDecimal("3250.000"))
                    .quantityUnit("KG")
                    .storageLocation("Coastal Terminal Godown, Mangalore")
                    .receivedDate(date4)
                    .status(Inventory.InventoryStatus.AVAILABLE)
                    .notes("Fresh arrival from Belthangady. Ready for drying or dispatch")
                    .build());

            // Session 5 - Sagar Green Canopy
            LocalDate date5 = LocalDate.now().minusDays(1);
            HarvestSession s5 = sessionRepository.save(HarvestSession.builder()
                    .sessionDate(date5)
                    .field(f14)
                    .contract(c6)
                    .supervisor(sup2)
                    .totalBags(55)
                    .totalQuantity(new BigDecimal("2750.000"))
                    .quantityUnit("KG")
                    .workerCount(4)
                    .status(HarvestSession.SessionStatus.COMPLETED)
                    .workers(Set.of(w9, w10, w11, w12))
                    .build());

            harvestRecordRepository.save(HarvestRecord.builder()
                    .harvestSession(s5)
                    .field(f14)
                    .contract(c6)
                    .owner(subraya)
                    .recordDate(date5)
                    .bagsCount(55)
                    .quantity(new BigDecimal("2750.000"))
                    .quantityUnit("KG")
                    .workerCount(4)
                    .remarks("Sagar canopy prime lot")
                    .build());

            inventoryRepository.save(Inventory.builder()
                    .field(f14)
                    .contract(c6)
                    .quantityReceived(new BigDecimal("2750.000"))
                    .quantitySold(BigDecimal.ZERO)
                    .availableQuantity(new BigDecimal("2750.000"))
                    .quantityUnit("KG")
                    .storageLocation("APMC Yard Godown, Shimoga")
                    .receivedDate(date5)
                    .status(Inventory.InventoryStatus.AVAILABLE)
                    .notes("Grade Rashi Idi under curing")
                    .build());

            // Sale 4 - Pending Payment Sale to CAMPCO Limited
            Sale sale4 = saleRepository.save(Sale.builder()
                    .saleNumber("SALE-2026-004")
                    .buyer(b6)
                    .inventory(inv1)
                    .saleDate(LocalDate.now().minusDays(1))
                    .quantityKg(new BigDecimal("1000.000"))
                    .quantityUnit("KG")
                    .ratePerUnit(new BigDecimal("59.5000"))
                    .grossAmount(new BigDecimal("59500.00"))
                    .transportCost(new BigDecimal("1200.00"))
                    .loadingCost(new BigDecimal("500.00"))
                    .otherCharges(BigDecimal.ZERO)
                    .netAmount(new BigDecimal("57800.00"))
                    .paymentStatus(Sale.SalePaymentStatus.PENDING)
                    .amountReceived(BigDecimal.ZERO)
                    .notes("CAMPCO 30-day payment term contract dispatch")
                    .build());

            recordBuyerTransaction(b6, sale4, LocalDate.now().minusDays(1), BuyerPayment.TransactionType.SALE, new BigDecimal("57800.00"), 1, null, "INV-004", "Dispatched to CAMPCO Mangalore depot", admin);
        }

        // ── 13. Operational Expenses ──────────────────────────────────────────
        if (expenseRepository.count() == 0) {
            LocalDate today = LocalDate.now();
            seedExpense(today.minusDays(7), fuelCat, new BigDecimal("2400.00"), PaymentMethod.UPI, "Diesel for Tata Ace truck KA-13-AB-1234 (Mudigere trips)");
            seedExpense(today.minusDays(6), foodCat, new BigDecimal("950.00"), PaymentMethod.CASH, "Midday lunch, snacks & tea for 6 harvesters at Laxmamma field");
            seedExpense(today.minusDays(5), equipCat, new BigDecimal("1850.00"), PaymentMethod.UPI, "High-strength climbing rope replacement & cutter sharpening");
            seedExpense(today.minusDays(4), chemicalsCat, new BigDecimal("3200.00"), PaymentMethod.BANK_TRANSFER, "Bordeaux mixture & copper sulfate for koleroga prevention");
            seedExpense(today.minusDays(3), transportCat, new BigDecimal("2800.00"), PaymentMethod.CASH, "Tempo hire for transporting 70 bags from field to Sakleshpur godown");
            seedExpense(today.minusDays(2), fuelCat, new BigDecimal("3500.00"), PaymentMethod.UPI, "Diesel fuel for Eicher Pro truck KA-14-GH-3456 (Sirsi run)");
            seedExpense(today.minusDays(1), foodCat, new BigDecimal("820.00"), PaymentMethod.CASH, "Breakfast and tea for morning harvesting crew at Suresh plot");
            seedExpense(today, leaseCat, new BigDecimal("1200.00"), PaymentMethod.UPI, "APMC Shimoga gate entry cess & weighbridge charges");
        }

        // ── 14. Attendance, Wage Ledgers & Worker Payments ─────────────────────
        if (attendanceRepository.count() == 0) {
            LocalDate day1 = LocalDate.now().minusDays(2);
            LocalDate day2 = LocalDate.now().minusDays(1);

            // Day 1 Attendance
            recordAttendanceWithWage(w1, f3, c1, day1, LocalTime.of(7, 30), LocalTime.of(17, 30), new BigDecimal("8.0"), new BigDecimal("2.0"), new BigDecimal("900.00"), new BigDecimal("112.50"), "Full day climbing + sorting", admin);
            recordAttendanceWithWage(w2, f3, c1, day1, LocalTime.of(8, 0), LocalTime.of(17, 0), new BigDecimal("8.0"), BigDecimal.ZERO, new BigDecimal("850.00"), BigDecimal.ZERO, "Full day cutting", admin);
            recordAttendanceWithWage(w4, f3, c1, day1, LocalTime.of(8, 30), LocalTime.of(16, 30), new BigDecimal("8.0"), BigDecimal.ZERO, new BigDecimal("750.00"), BigDecimal.ZERO, "Net holding & collection", admin);
            recordAttendanceWithWage(w5, f3, c1, day1, LocalTime.of(8, 0), LocalTime.of(17, 30), new BigDecimal("8.0"), new BigDecimal("1.5"), new BigDecimal("800.00"), new BigDecimal("100.00"), "Produce loading on truck", admin);
            recordAttendanceWithWage(w8, f3, c1, day1, LocalTime.of(7, 0), LocalTime.of(18, 0), new BigDecimal("8.0"), new BigDecimal("3.0"), new BigDecimal("875.00"), new BigDecimal("109.38"), "Truck driving & delivery", admin);

            // Day 2 Attendance
            recordAttendanceWithWage(w1, f1, c3, day2, LocalTime.of(7, 30), LocalTime.of(16, 30), new BigDecimal("8.0"), new BigDecimal("1.0"), new BigDecimal("900.00"), new BigDecimal("112.50"), "Climbing at Suresh north plot", admin);
            recordAttendanceWithWage(w3, f1, c3, day2, LocalTime.of(8, 0), LocalTime.of(17, 0), new BigDecimal("8.0"), BigDecimal.ZERO, new BigDecimal("950.00"), BigDecimal.ZERO, "Climber support", admin);
            recordAttendanceWithWage(w6, f1, c3, day2, LocalTime.of(8, 0), LocalTime.of(17, 0), new BigDecimal("8.0"), BigDecimal.ZERO, new BigDecimal("825.00"), BigDecimal.ZERO, "Areca bunches cutting", admin);
            recordAttendanceWithWage(w9, f11, c4, day2, LocalTime.of(7, 0), LocalTime.of(17, 0), new BigDecimal("8.0"), new BigDecimal("2.0"), new BigDecimal("950.00"), new BigDecimal("118.75"), "Sirsi valley canopy climb", sup2);
            recordAttendanceWithWage(w10, f11, c4, day2, LocalTime.of(7, 30), LocalTime.of(16, 30), new BigDecimal("8.0"), BigDecimal.ZERO, new BigDecimal("850.00"), BigDecimal.ZERO, "Sirsi cutting specialist", sup2);

            // Worker Wage Settlements (Payments)
            workerPaymentRepository.save(WorkerPayment.builder()
                    .worker(w1)
                    .paymentDate(LocalDate.now().minusDays(1))
                    .amount(new BigDecimal("1500.00"))
                    .paymentType(WorkerPayment.PaymentType.PAYMENT)
                    .paymentMethod(PaymentMethod.UPI)
                    .referenceNumber("UPI/9482018471")
                    .remarks("Weekly wage settlement via UPI to Raju Gowda")
                    .recordedBy(admin)
                    .build());

            workerPaymentRepository.save(WorkerPayment.builder()
                    .worker(w2)
                    .paymentDate(LocalDate.now().minusDays(1))
                    .amount(new BigDecimal("850.00"))
                    .paymentType(WorkerPayment.PaymentType.PAYMENT)
                    .paymentMethod(PaymentMethod.CASH)
                    .referenceNumber("VOUCHER-084")
                    .remarks("Daily wage cash payout to Shiva Kumar")
                    .recordedBy(admin)
                    .build());

            workerPaymentRepository.save(WorkerPayment.builder()
                    .worker(w3)
                    .paymentDate(LocalDate.now().minusDays(1))
                    .amount(new BigDecimal("1000.00"))
                    .paymentType(WorkerPayment.PaymentType.ADVANCE)
                    .paymentMethod(PaymentMethod.CASH)
                    .referenceNumber("ADV-019")
                    .remarks("Festival advance granted to Prasad M")
                    .recordedBy(admin)
                    .build());
        }

        // ── 15. System Notifications ──────────────────────────────────────────
        if (notificationRepository.count() == 0) {
            notificationRepository.save(Notification.builder()
                    .recipient(admin)
                    .type(Notification.NotificationType.SCHEDULED_WORK_REMINDER)
                    .title("Today's Harvest Sessions Active")
                    .message("3 harvesting operations are underway today across Mudigere, Belthangady, and Sirsi plantations.")
                    .entityType("SCHEDULE")
                    .entityId(1L)
                    .priority(Notification.Priority.HIGH)
                    .read(false)
                    .build());

            notificationRepository.save(Notification.builder()
                    .recipient(admin)
                    .type(Notification.NotificationType.BUYER_OUTSTANDING)
                    .title("Pending Payment: Hassan Nut Buyers")
                    .message("Balance of ₹33,200.00 is due on Sale SALE-2026-002 from Hassan Nut Buyers.")
                    .entityType("SALE")
                    .entityId(2L)
                    .priority(Notification.Priority.NORMAL)
                    .read(false)
                    .build());

            notificationRepository.save(Notification.builder()
                    .recipient(admin)
                    .type(Notification.NotificationType.PAYMENT_RECEIVED)
                    .title("Payment Received: Sirsi TSS Cooperative")
                    .message("Received ₹1,81,800.00 via NEFT settlement for tender arecanut auction lot.")
                    .entityType("BUYER_PAYMENT")
                    .entityId(3L)
                    .priority(Notification.Priority.LOW)
                    .read(true)
                    .build());

            notificationRepository.save(Notification.builder()
                    .recipient(admin)
                    .type(Notification.NotificationType.EQUIPMENT_MAINTENANCE_DUE)
                    .title("Safety Rope Inspection Due")
                    .message("25 nylon climbing ropes at Sakleshpur depot require weekly wear inspection.")
                    .entityType("EQUIPMENT")
                    .entityId(2L)
                    .priority(Notification.Priority.NORMAL)
                    .read(false)
                    .build());

            notificationRepository.save(Notification.builder()
                    .recipient(admin)
                    .type(Notification.NotificationType.CONTRACT_NEARING_COMPLETION)
                    .title("Contract Progress Alert")
                    .message("Contract CTR-2026-001 (Laxmamma Hegde) has reached 54% of expected yield.")
                    .entityType("CONTRACT")
                    .entityId(1L)
                    .priority(Notification.Priority.LOW)
                    .read(true)
                    .build());
        }

        log.info("Demo data seeding successfully initialized! Default Login: admin / Password@123");
    }

    private Role seedRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() ->
                roleRepository.save(Role.builder().name(name).description(description).build())
        );
    }

    private ExpenseCategory seedExpenseCategory(String name, String description) {
        return expenseCategoryRepository.findByName(name).orElseGet(() ->
                expenseCategoryRepository.save(ExpenseCategory.builder()
                        .name(name).description(description).system(true).active(true).build())
        );
    }

    private Owner seedOwner(String name, String phone, String village, String district, PaymentMethod pref) {
        return ownerRepository.findAll().stream()
                .filter(o -> o.getPhone().equals(phone))
                .findFirst()
                .orElseGet(() -> ownerRepository.save(Owner.builder()
                        .name(name).phone(phone).village(village).district(district)
                        .state("Karnataka").paymentPreference(pref).active(true).build())
                );
    }

    private Field seedField(Owner owner, String name, String village, String district, BigDecimal acreage, Integer treeCount, BigDecimal expectedYield, FieldStatus status) {
        return fieldRepository.findAll().stream()
                .filter(f -> f.getFieldName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> fieldRepository.save(Field.builder()
                        .owner(owner).fieldName(name).village(village).district(district)
                        .acreage(acreage).treeCount(treeCount).expectedYieldKg(expectedYield)
                        .status(status).build())
                );
    }

    private Worker seedWorker(String name, String phone, String village, SkillType skill, BigDecimal exp, BigDecimal dailyWage, User user) {
        return workerRepository.findAll().stream()
                .filter(w -> w.getPhone().equals(phone))
                .findFirst()
                .orElseGet(() -> workerRepository.save(Worker.builder()
                        .name(name).phone(phone).village(village).district("Hassan").state("Karnataka")
                        .skillType(skill).experienceYears(exp).dailyWage(dailyWage)
                        .joiningDate(LocalDate.now().minusMonths(6)).status(WorkerStatus.ACTIVE)
                        .user(user).build())
                );
    }

    private HarvestingContract seedContract(Owner owner, Field field, String number, ContractType type, BigDecimal rate, BigDecimal expectedQty, LocalDate start, LocalDate end, ContractStatus status, User supervisor) {
        return contractRepository.findByContractNumber(number).orElseGet(() ->
                contractRepository.save(HarvestingContract.builder()
                        .owner(owner).field(field).contractNumber(number).contractType(type)
                        .rate(rate).expectedQuantityKg(expectedQty).startDate(start).expectedEndDate(end)
                        .status(status).supervisor(supervisor).build())
        );
    }

    private Buyer seedBuyer(String name, String phone, String company, String village, String district, String terms, BigDecimal defaultRate) {
        return buyerRepository.findAll().stream()
                .filter(b -> b.getPhone().equals(phone))
                .findFirst()
                .orElseGet(() -> buyerRepository.save(Buyer.builder()
                        .name(name).phone(phone).companyName(company).village(village)
                        .district(district).paymentTerms(terms).defaultRatePerKg(defaultRate)
                        .status(Buyer.BuyerStatus.ACTIVE).build())
                );
    }

    private Vehicle seedVehicle(String regNo, Vehicle.VehicleType type, String make, String model, Integer year, Worker driver, Vehicle.FuelType fuel) {
        return vehicleRepository.findAll().stream()
                .filter(v -> v.getRegistrationNumber().equalsIgnoreCase(regNo))
                .findFirst()
                .orElseGet(() -> vehicleRepository.save(Vehicle.builder()
                        .registrationNumber(regNo).vehicleType(type).make(make).model(model)
                        .year(year).driver(driver).fuelType(fuel).status(Vehicle.VehicleStatus.ACTIVE).build())
                );
    }

    private Equipment seedEquipment(String name, Equipment.EquipmentCategory cat, int qty, BigDecimal cost) {
        return equipmentRepository.findAll().stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> equipmentRepository.save(Equipment.builder()
                        .name(name).category(cat).quantity(qty).conditionStatus(Equipment.ConditionStatus.GOOD)
                        .purchaseDate(LocalDate.now().minusMonths(4)).purchaseCost(cost)
                        .status(Equipment.EquipmentStatus.AVAILABLE).build())
                );
    }

    private void seedAssignment(Worker worker, Field field, HarvestingContract contract, LocalDate date, SkillType role, User assignedBy, String notes) {
        if (!assignmentRepository.existsByWorkerIdAndFieldIdAndAssignedDate(worker.getId(), field.getId(), date)) {
            assignmentRepository.save(WorkerAssignment.builder()
                    .worker(worker)
                    .field(field)
                    .contract(contract)
                    .assignedDate(date)
                    .assignedRole(role)
                    .assignedBy(assignedBy)
                    .notes(notes)
                    .status(WorkerAssignment.AssignmentStatus.ACTIVE)
                    .build());
        }
    }

    private void seedExpense(LocalDate date, ExpenseCategory cat, BigDecimal amount, PaymentMethod method, String desc) {
        expenseRepository.save(Expense.builder()
                .expenseDate(date)
                .category(cat)
                .amount(amount)
                .paymentMethod(method)
                .description(desc)
                .build());
    }

    private void recordAttendanceWithWage(Worker worker, Field field, HarvestingContract contract, LocalDate date,
                                          LocalTime checkIn, LocalTime checkOut, BigDecimal hours, BigDecimal otHours,
                                          BigDecimal dailyWage, BigDecimal otRate, String remarks, User markedBy) {
        Attendance att = attendanceRepository.save(Attendance.builder()
                .worker(worker)
                .field(field)
                .contract(contract)
                .attendanceDate(date)
                .status(Attendance.AttendanceStatus.PRESENT)
                .checkInTime(checkIn)
                .checkOutTime(checkOut)
                .workingHours(hours)
                .overtimeHours(otHours)
                .dailyWageApplied(dailyWage)
                .overtimeRate(otRate)
                .remarks(remarks)
                .markedBy(markedBy)
                .build());

        wageLedgerRepository.save(WageLedger.builder()
                .worker(worker)
                .transactionType(WageLedger.TransactionType.DAILY_WAGE)
                .amount(dailyWage)
                .transactionDate(date)
                .attendance(att)
                .field(field)
                .contract(contract)
                .description("Daily wage for " + remarks)
                .recordedBy(markedBy)
                .build());

        if (otHours.compareTo(BigDecimal.ZERO) > 0 && otRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal otAmount = otHours.multiply(otRate);
            wageLedgerRepository.save(WageLedger.builder()
                    .worker(worker)
                    .transactionType(WageLedger.TransactionType.OVERTIME)
                    .amount(otAmount)
                    .transactionDate(date)
                    .attendance(att)
                    .field(field)
                    .contract(contract)
                    .description("Overtime pay (" + otHours + "h) for " + remarks)
                    .recordedBy(markedBy)
                    .build());
        }
    }

    private void recordBuyerTransaction(Buyer buyer, Sale sale, LocalDate date, BuyerPayment.TransactionType type,
                                        BigDecimal amount, Integer sign, PaymentMethod method, String ref, String remarks, User user) {
        buyerPaymentRepository.save(BuyerPayment.builder()
                .buyer(buyer)
                .sale(sale)
                .paymentDate(date)
                .transactionType(type)
                .amount(amount)
                .adjustmentSign(sign)
                .paymentMethod(method)
                .referenceNumber(ref)
                .remarks(remarks)
                .recordedBy(user)
                .build());
    }
}
