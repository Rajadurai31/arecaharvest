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
import java.time.LocalTime;
import java.util.Set;

/**
 * Seeds comprehensive data for the demo (H2) profile so all dashboards,
 * charts, modules, and workflows have realistic sample data ready out-of-the-box.
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Demo profile — seeding essential and operational business data...");

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
        ExpenseCategory foodCat = seedExpenseCategory("FOOD", "Meals and food for workers");
        ExpenseCategory equipCat = seedExpenseCategory("EQUIPMENT", "Equipment purchase or rental");
        ExpenseCategory transportCat = seedExpenseCategory("TRANSPORT", "Transportation of workers or produce");
        ExpenseCategory otherCat = seedExpenseCategory("OTHER", "Miscellaneous expenses");

        // ── 4. Owners ─────────────────────────────────────────────────────────
        Owner suresh = seedOwner("Suresh Gowda", "9845001001", "Sakleshpur", "Hassan", PaymentMethod.CASH);
        Owner laxmamma = seedOwner("Laxmamma Hegde", "9845002001", "Mudigere", "Chikkamagaluru", PaymentMethod.UPI);
        Owner narayana = seedOwner("Narayana Poojary", "9845003001", "Sullia", "Dakshina Kannada", PaymentMethod.CASH);
        Owner kavitha = seedOwner("Kavitha Shetty", "9845004001", "Belthangady", "Dakshina Kannada", PaymentMethod.BANK_TRANSFER);
        Owner manjunath = seedOwner("Manjunath Nayak", "9845005001", "Chikkamagaluru", "Chikkamagaluru", PaymentMethod.CASH);

        // ── 5. Fields ─────────────────────────────────────────────────────────
        Field field1 = seedField(suresh, "Suresh North Plot", "Sakleshpur", "Hassan", new BigDecimal("2.50"), 120, new BigDecimal("5000.00"), FieldStatus.ACTIVE);
        Field field2 = seedField(suresh, "Suresh South Plot", "Sakleshpur", "Hassan", new BigDecimal("1.75"), 85, new BigDecimal("3500.00"), FieldStatus.ACTIVE);
        Field field3 = seedField(laxmamma, "Laxmamma Main Field", "Mudigere", "Chikkamagaluru", new BigDecimal("3.00"), 160, new BigDecimal("6500.00"), FieldStatus.HARVESTING);
        Field field4 = seedField(narayana, "Poojary Field A", "Sullia", "Dakshina Kannada", new BigDecimal("1.50"), 70, new BigDecimal("2800.00"), FieldStatus.ACTIVE);
        Field field5 = seedField(kavitha, "Kavitha Estate", "Belthangady", "Dakshina Kannada", new BigDecimal("4.00"), 200, new BigDecimal("8000.00"), FieldStatus.HARVESTING);
        Field field6 = seedField(manjunath, "Manjunath Farm 1", "Chikkamagaluru", "Chikkamagaluru", new BigDecimal("1.25"), 60, new BigDecimal("2500.00"), FieldStatus.ACTIVE);

        // ── 6. Workers ────────────────────────────────────────────────────────
        Worker w1 = seedWorker("Raju Gowda", "9876543212", "Sakleshpur", SkillType.CLIMBER, new BigDecimal("8.0"), new BigDecimal("900.00"), workerUser);
        Worker w2 = seedWorker("Shiva Kumar", "9876500001", "Mudigere", SkillType.CUTTER, new BigDecimal("5.0"), new BigDecimal("850.00"), null);
        Worker w3 = seedWorker("Prasad M", "9876500002", "Sullia", SkillType.CLIMBER, new BigDecimal("12.0"), new BigDecimal("950.00"), null);
        Worker w4 = seedWorker("Geetha Bai", "9876500003", "Belthangady", SkillType.COLLECTOR, new BigDecimal("3.0"), new BigDecimal("750.00"), null);
        Worker w5 = seedWorker("Ramesh Naik", "9876500004", "Chikkamagaluru", SkillType.LOADER, new BigDecimal("6.0"), new BigDecimal("800.00"), null);
        Worker w6 = seedWorker("Venu Gopal", "9876500005", "Sakleshpur", SkillType.CUTTER, new BigDecimal("4.0"), new BigDecimal("825.00"), null);
        Worker w7 = seedWorker("Suma Rani", "9876500007", "Belthangady", SkillType.COLLECTOR, new BigDecimal("2.5"), new BigDecimal("700.00"), null);
        Worker w8 = seedWorker("Krishnamurthy R", "9876500009", "Hassan", SkillType.DRIVER, new BigDecimal("7.0"), new BigDecimal("875.00"), null);

        // ── 7. Contracts ──────────────────────────────────────────────────────
        HarvestingContract c1 = seedContract(laxmamma, field3, "CTR-2026-001", ContractType.PER_KG, new BigDecimal("4.50"), new BigDecimal("6500.00"), LocalDate.now().minusDays(30), LocalDate.now().plusDays(30), ContractStatus.ACTIVE, sup1);
        HarvestingContract c2 = seedContract(kavitha, field5, "CTR-2026-002", ContractType.PER_TREE, new BigDecimal("120.00"), new BigDecimal("8000.00"), LocalDate.now().minusDays(40), LocalDate.now().plusDays(20), ContractStatus.ACTIVE, sup1);
        HarvestingContract c3 = seedContract(suresh, field1, "CTR-2026-003", ContractType.PER_KG, new BigDecimal("4.25"), new BigDecimal("5000.00"), LocalDate.now().minusDays(15), LocalDate.now().plusDays(45), ContractStatus.ACTIVE, sup1);

        // ── 8. Buyers ─────────────────────────────────────────────────────────
        Buyer b1 = seedBuyer("Mangalore Areca Traders", "9844100001", "Mangalore Areca Traders Pvt Ltd", "Mangalore", "Dakshina Kannada", "Net 7 days", new BigDecimal("58.50"));
        Buyer b2 = seedBuyer("Hassan Nut Buyers", "9844100002", "Hassan Traders Co", "Hassan", "Hassan", "Immediate", new BigDecimal("57.00"));
        Buyer b3 = seedBuyer("Udupi Fresh Produce", "9844100003", "Udupi Fresh Pvt Ltd", "Udupi", "Udupi", "Net 15 days", new BigDecimal("59.00"));

        // ── 9. Vehicles & Equipment ───────────────────────────────────────────
        seedVehicle("KA-13-AB-1234", Vehicle.VehicleType.TRUCK, "TATA", "Ace", 2022, w8, Vehicle.FuelType.DIESEL);
        seedVehicle("KA-13-CD-5678", Vehicle.VehicleType.PICKUP, "Mahindra", "Bolero", 2020, null, Vehicle.FuelType.DIESEL);
        seedVehicle("KA-13-EF-9012", Vehicle.VehicleType.BIKE, "Hero", "Splendor", 2023, null, Vehicle.FuelType.PETROL);

        seedEquipment("Bamboo Ladders 15ft", Equipment.EquipmentCategory.LADDER, 10, new BigDecimal("1500.00"));
        seedEquipment("Climbing Ropes 20m", Equipment.EquipmentCategory.ROPE, 15, new BigDecimal("450.00"));
        seedEquipment("Areca Cutters", Equipment.EquipmentCategory.CUTTER, 8, new BigDecimal("850.00"));
        seedEquipment("Jute Collection Bags", Equipment.EquipmentCategory.BAGS, 200, new BigDecimal("35.00"));
        seedEquipment("Safety Helmets", Equipment.EquipmentCategory.SAFETY_EQUIPMENT, 20, new BigDecimal("250.00"));

        // ── 10. Operational Harvest Sessions, Inventory & Sales ────────────────
        if (sessionRepository.count() == 0) {
            LocalDate date1 = LocalDate.now().minusDays(5);
            HarvestSession s1 = sessionRepository.save(HarvestSession.builder()
                    .sessionDate(date1)
                    .field(field3)
                    .contract(c1)
                    .supervisor(sup1)
                    .totalBags(70)
                    .totalQuantity(new BigDecimal("3500.000"))
                    .quantityUnit("KG")
                    .workerCount(4)
                    .status(HarvestSession.SessionStatus.COMPLETED)
                    .workers(Set.of(w1, w2, w4, w5))
                    .build());

            harvestRecordRepository.save(HarvestRecord.builder()
                    .harvestSession(s1)
                    .field(field3)
                    .contract(c1)
                    .owner(laxmamma)
                    .recordDate(date1)
                    .bagsCount(70)
                    .quantity(new BigDecimal("3500.000"))
                    .quantityUnit("KG")
                    .workerCount(4)
                    .remarks("Initial prime harvest batch from Laxmamma field")
                    .build());

            Inventory inv1 = inventoryRepository.save(Inventory.builder()
                    .field(field3)
                    .contract(c1)
                    .quantityReceived(new BigDecimal("3500.000"))
                    .quantitySold(new BigDecimal("2000.000"))
                    .availableQuantity(new BigDecimal("1500.000"))
                    .quantityUnit("KG")
                    .storageLocation("Godown A, Sakleshpur")
                    .receivedDate(date1)
                    .status(Inventory.InventoryStatus.PARTIALLY_SOLD)
                    .notes("Quality A green harvested areca")
                    .build());

            saleRepository.save(Sale.builder()
                    .saleNumber("SALE-2026-001")
                    .buyer(b1)
                    .inventory(inv1)
                    .saleDate(LocalDate.now().minusDays(3))
                    .quantityKg(new BigDecimal("2000.000"))
                    .quantityUnit("KG")
                    .ratePerUnit(new BigDecimal("58.5000"))
                    .grossAmount(new BigDecimal("117000.00"))
                    .transportCost(new BigDecimal("2500.00"))
                    .loadingCost(new BigDecimal("1000.00"))
                    .otherCharges(BigDecimal.ZERO)
                    .netAmount(new BigDecimal("113500.00"))
                    .paymentStatus(Sale.SalePaymentStatus.PAID)
                    .amountReceived(new BigDecimal("113500.00"))
                    .notes("Direct dispatch to Mangalore godown")
                    .build());

            LocalDate date2 = LocalDate.now().minusDays(1);
            HarvestSession s2 = sessionRepository.save(HarvestSession.builder()
                    .sessionDate(date2)
                    .field(field1)
                    .contract(c3)
                    .supervisor(sup1)
                    .totalBags(50)
                    .totalQuantity(new BigDecimal("2500.000"))
                    .quantityUnit("KG")
                    .workerCount(3)
                    .status(HarvestSession.SessionStatus.COMPLETED)
                    .workers(Set.of(w1, w3, w6))
                    .build());

            harvestRecordRepository.save(HarvestRecord.builder()
                    .harvestSession(s2)
                    .field(field1)
                    .contract(c3)
                    .owner(suresh)
                    .recordDate(date2)
                    .bagsCount(50)
                    .quantity(new BigDecimal("2500.000"))
                    .quantityUnit("KG")
                    .workerCount(3)
                    .remarks("North plot morning harvest")
                    .build());

            inventoryRepository.save(Inventory.builder()
                    .field(field1)
                    .contract(c3)
                    .quantityReceived(new BigDecimal("2500.000"))
                    .quantitySold(BigDecimal.ZERO)
                    .availableQuantity(new BigDecimal("2500.000"))
                    .quantityUnit("KG")
                    .storageLocation("Godown B, Sakleshpur")
                    .receivedDate(date2)
                    .status(Inventory.InventoryStatus.AVAILABLE)
                    .notes("Fresh stock ready for auction/direct sale")
                    .build());
        }

        // ── 11. Expenses ──────────────────────────────────────────────────────
        if (expenseRepository.count() == 0) {
            expenseRepository.save(Expense.builder()
                    .expenseDate(LocalDate.now().minusDays(4))
                    .category(fuelCat)
                    .amount(new BigDecimal("1850.00"))
                    .paymentMethod(PaymentMethod.UPI)
                    .description("Diesel for Tata Ace truck KA-13-AB-1234 (Ref: UPI/948210)")
                    .build());

            expenseRepository.save(Expense.builder()
                    .expenseDate(LocalDate.now().minusDays(2))
                    .category(foodCat)
                    .amount(new BigDecimal("650.00"))
                    .paymentMethod(PaymentMethod.CASH)
                    .description("Midday lunch and tea for harvest crew at Suresh plot")
                    .build());

            expenseRepository.save(Expense.builder()
                    .expenseDate(LocalDate.now().minusDays(1))
                    .category(equipCat)
                    .amount(new BigDecimal("450.00"))
                    .paymentMethod(PaymentMethod.CASH)
                    .description("Climbing rope replacement and cutter sharpening")
                    .build());
        }

        // ── 12. Attendance & Wages ────────────────────────────────────────────
        if (attendanceRepository.count() == 0) {
            LocalDate attDate = LocalDate.now().minusDays(1);
            Attendance a1 = attendanceRepository.save(Attendance.builder()
                    .worker(w1)
                    .field(field1)
                    .contract(c3)
                    .attendanceDate(attDate)
                    .status(Attendance.AttendanceStatus.PRESENT)
                    .checkInTime(LocalTime.of(7, 30))
                    .checkOutTime(LocalTime.of(17, 30))
                    .workingHours(new BigDecimal("8.00"))
                    .overtimeHours(new BigDecimal("2.00"))
                    .dailyWageApplied(new BigDecimal("900.00"))
                    .overtimeRate(new BigDecimal("112.50"))
                    .remarks("Full day climbing + 2h sorting")
                    .markedBy(admin)
                    .build());

            wageLedgerRepository.save(WageLedger.builder()
                    .worker(w1)
                    .transactionType(WageLedger.TransactionType.DAILY_WAGE)
                    .amount(new BigDecimal("900.00"))
                    .transactionDate(attDate)
                    .attendance(a1)
                    .field(field1)
                    .contract(c3)
                    .description("Daily wage for Suresh plot harvest")
                    .recordedBy(admin)
                    .build());

            wageLedgerRepository.save(WageLedger.builder()
                    .worker(w1)
                    .transactionType(WageLedger.TransactionType.OVERTIME)
                    .amount(new BigDecimal("225.00"))
                    .transactionDate(attDate)
                    .attendance(a1)
                    .field(field1)
                    .contract(c3)
                    .description("2 hours overtime climbing")
                    .recordedBy(admin)
                    .build());

            Attendance a2 = attendanceRepository.save(Attendance.builder()
                    .worker(w2)
                    .field(field1)
                    .contract(c3)
                    .attendanceDate(attDate)
                    .status(Attendance.AttendanceStatus.PRESENT)
                    .checkInTime(LocalTime.of(8, 0))
                    .checkOutTime(LocalTime.of(17, 0))
                    .workingHours(new BigDecimal("8.00"))
                    .overtimeHours(BigDecimal.ZERO)
                    .dailyWageApplied(new BigDecimal("850.00"))
                    .overtimeRate(BigDecimal.ZERO)
                    .remarks("Full day cutting")
                    .markedBy(admin)
                    .build());

            wageLedgerRepository.save(WageLedger.builder()
                    .worker(w2)
                    .transactionType(WageLedger.TransactionType.DAILY_WAGE)
                    .amount(new BigDecimal("850.00"))
                    .transactionDate(attDate)
                    .attendance(a2)
                    .field(field1)
                    .contract(c3)
                    .description("Daily wage for cutting at Suresh plot")
                    .recordedBy(admin)
                    .build());
        }

        log.info("Demo data seeding complete! Login: admin / Password@123");
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
}
