-- =============================================================================
-- V12: Development / Demo Seed Data
-- =============================================================================
-- ⚠️  THIS IS DEMO DATA ONLY. DO NOT use in production reports.
--     All passwords are BCrypt of "Password@123"
-- =============================================================================

-- ── Users ────────────────────────────────────────────────────────────────────
INSERT INTO users (username, email, password_hash, full_name, phone, active) VALUES
    ('admin',       'admin@arecaharvest.com',      '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Rajesh Kumar (Admin)',    '9876543210', TRUE),
    ('supervisor1', 'sup1@arecaharvest.com',        '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mohan Das (Supervisor)',  '9876543211', TRUE),
    ('worker_raju',  'raju@arecaharvest.com',       '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Raju Gowda (Worker)',     '9876543212', TRUE);

-- User-Role assignments
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1),   -- admin → ROLE_ADMIN
    (2, 2),   -- supervisor1 → ROLE_SUPERVISOR
    (3, 3);   -- worker_raju → ROLE_WORKER

-- ── Owners ────────────────────────────────────────────────────────────────────
INSERT INTO owners (name, phone, alternate_phone, village, district, state, payment_preference, active) VALUES
    ('Suresh Gowda',      '9845001001', '9845001002', 'Sakleshpur',  'Hassan',    'Karnataka', 'CASH',         TRUE),
    ('Laxmamma Hegde',    '9845002001', NULL,          'Mudigere',    'Chikkamagaluru', 'Karnataka', 'UPI',   TRUE),
    ('Narayana Poojary',  '9845003001', '9845003002', 'Sullia',       'Dakshina Kannada', 'Karnataka', 'CASH', TRUE),
    ('Kavitha Shetty',    '9845004001', NULL,          'Belthangady', 'Dakshina Kannada', 'Karnataka', 'BANK_TRANSFER', TRUE),
    ('Manjunath Nayak',   '9845005001', '9845005002', 'Chikkamagaluru', 'Chikkamagaluru', 'Karnataka', 'CASH', TRUE);

-- ── Fields ────────────────────────────────────────────────────────────────────
INSERT INTO fields (owner_id, field_name, village, district, acreage, tree_count, expected_yield_kg, status) VALUES
    (1, 'Suresh North Plot',    'Sakleshpur',       'Hassan',              2.50, 120, 5000.000, 'ACTIVE'),
    (1, 'Suresh South Plot',    'Sakleshpur',       'Hassan',              1.75,  85, 3500.000, 'ACTIVE'),
    (2, 'Laxmamma Main Field',  'Mudigere',         'Chikkamagaluru',      3.00, 160, 6500.000, 'HARVESTING'),
    (3, 'Poojary Field A',      'Sullia',           'Dakshina Kannada',    1.50,  70, 2800.000, 'ACTIVE'),
    (3, 'Poojary Field B',      'Sullia',           'Dakshina Kannada',    2.00,  95, 4000.000, 'PENDING'),
    (4, 'Kavitha Estate',       'Belthangady',      'Dakshina Kannada',    4.00, 200, 8000.000, 'HARVESTING'),
    (5, 'Manjunath Farm 1',     'Chikkamagaluru',   'Chikkamagaluru',      1.25,  60, 2500.000, 'ACTIVE'),
    (5, 'Manjunath Farm 2',     'Chikkamagaluru',   'Chikkamagaluru',      2.75, 130, 5500.000, 'ACTIVE'),
    (1, 'Suresh Hill Plot',     'Sakleshpur',       'Hassan',              1.00,  45, 1800.000, 'COMPLETED'),
    (2, 'Laxmamma Back Field',  'Mudigere',         'Chikkamagaluru',      1.80,  90, 3600.000, 'ACTIVE');

-- ── Workers ───────────────────────────────────────────────────────────────────
INSERT INTO workers (user_id, name, phone, village, skill_type, experience_years, daily_wage, joining_date, status) VALUES
    (3,    'Raju Gowda',       '9876543212', 'Sakleshpur',     'CLIMBER',   8.0,  900.00, '2020-01-15', 'ACTIVE'),
    (NULL, 'Shiva Kumar',      '9876500001', 'Mudigere',       'CUTTER',    5.0,  850.00, '2021-03-10', 'ACTIVE'),
    (NULL, 'Prasad M',         '9876500002', 'Sullia',         'CLIMBER',   12.0, 950.00, '2018-06-01', 'ACTIVE'),
    (NULL, 'Geetha Bai',       '9876500003', 'Belthangady',    'COLLECTOR', 3.0,  750.00, '2023-01-05', 'ACTIVE'),
    (NULL, 'Ramesh Naik',      '9876500004', 'Chikkamagaluru', 'LOADER',    6.0,  800.00, '2020-07-20', 'ACTIVE'),
    (NULL, 'Venu Gopal',       '9876500005', 'Sakleshpur',     'CUTTER',    4.0,  825.00, '2022-02-14', 'ACTIVE'),
    (NULL, 'Anand Shetty',     '9876500006', 'Sullia',         'CLIMBER',   9.0,  900.00, '2019-11-30', 'ACTIVE'),
    (NULL, 'Suma Rani',        '9876500007', 'Belthangady',    'COLLECTOR', 2.5,  700.00, '2024-01-10', 'ACTIVE'),
    (NULL, 'Nagesha G',        '9876500008', 'Mudigere',       'HELPER',    1.0,  650.00, '2025-03-15', 'ACTIVE'),
    (NULL, 'Krishnamurthy R',  '9876500009', 'Hassan',         'DRIVER',    7.0,  875.00, '2020-09-01', 'ACTIVE'),
    (NULL, 'Deepa Kumari',     '9876500010', 'Chikkamagaluru', 'COLLECTOR', 4.0,  750.00, '2021-08-20', 'ACTIVE'),
    (NULL, 'Mahesha B',        '9876500011', 'Sakleshpur',     'CLIMBER',   11.0, 925.00, '2018-12-10', 'ACTIVE'),
    (NULL, 'Santhosh K',       '9876500012', 'Sullia',         'CUTTER',    6.5,  840.00, '2021-05-05', 'ACTIVE'),
    (NULL, 'Vijaya M',         '9876500013', 'Mudigere',       'HELPER',    2.0,  650.00, '2024-06-01', 'ACTIVE'),
    (NULL, 'Lokesh Gowda',     '9876500014', 'Hassan',         'LOADER',    8.0,  820.00, '2019-04-15', 'ACTIVE');

-- ── Buyers ────────────────────────────────────────────────────────────────────
INSERT INTO buyers (name, phone, company_name, village, district, payment_terms, default_rate_per_kg, status) VALUES
    ('Mangalore Areca Traders',   '9844100001', 'Mangalore Areca Traders Pvt Ltd', 'Mangalore',     'Dakshina Kannada', 'Net 7 days',  58.50, 'ACTIVE'),
    ('Hassan Nut Buyers',         '9844100002', NULL,                               'Hassan',        'Hassan',           'Immediate',   57.00, 'ACTIVE'),
    ('Udupi Fresh Produce',       '9844100003', 'Udupi Fresh Pvt Ltd',              'Udupi',         'Udupi',            'Net 15 days', 59.00, 'ACTIVE');

-- ── Contracts ─────────────────────────────────────────────────────────────────
INSERT INTO harvesting_contracts
    (owner_id, field_id, contract_number, contract_type, rate, expected_quantity_kg, start_date, expected_end_date, status, supervisor_id)
VALUES
    (2, 3, 'CTR-2026-001', 'PER_KG',      4.50,  6500.000, '2026-08-01', '2026-09-30', 'ACTIVE',    2),
    (4, 6, 'CTR-2026-002', 'PER_TREE',  120.00,  8000.000, '2026-07-15', '2026-09-15', 'ACTIVE',    2),
    (1, 1, 'CTR-2026-003', 'PER_KG',      4.25,  5000.000, '2026-09-10', '2026-10-31', 'ACTIVE',    2),
    (3, 4, 'CTR-2026-004', 'FIXED_AMOUNT', 0.00,  2800.000, '2026-09-01', '2026-10-15', 'DRAFT',    NULL),
    (1, 9, 'CTR-2025-001', 'PER_KG',      4.00,  1800.000, '2025-08-01', '2025-09-30', 'COMPLETED', 2);

UPDATE harvesting_contracts SET fixed_amount = 28000.00 WHERE contract_number = 'CTR-2026-004';

-- ── Vehicles ──────────────────────────────────────────────────────────────────
INSERT INTO vehicles (registration_number, vehicle_type, make, model, year, driver_id, fuel_type, status) VALUES
    ('KA-13-AB-1234', 'TRUCK',  'TATA',   'Ace',      2022, 10, 'DIESEL', 'ACTIVE'),
    ('KA-13-CD-5678', 'PICKUP', 'Mahindra','Bolero',  2020, NULL, 'DIESEL', 'ACTIVE'),
    ('KA-13-EF-9012', 'BIKE',   'Hero',   'Splendor', 2023, NULL, 'PETROL', 'ACTIVE');

-- ── Equipment ─────────────────────────────────────────────────────────────────
INSERT INTO equipment (name, category, quantity, condition_status, purchase_date, purchase_cost, status) VALUES
    ('Bamboo Ladders 15ft',    'LADDER',           10, 'GOOD',  '2024-01-01', 1500.00,  'AVAILABLE'),
    ('Climbing Ropes 20m',     'ROPE',             15, 'GOOD',  '2024-01-01',  450.00,  'AVAILABLE'),
    ('Areca Cutters',          'CUTTER',            8, 'FAIR',  '2023-06-15',  850.00,  'IN_USE'),
    ('Jute Collection Bags',   'BAGS',            200, 'GOOD',  '2026-01-01',   35.00,  'IN_USE'),
    ('Safety Helmets',         'SAFETY_EQUIPMENT', 20, 'GOOD',  '2024-03-01',  250.00,  'AVAILABLE'),
    ('Tarpaulin Sheets 20x20', 'TARPAULIN',         5, 'FAIR',  '2023-09-01',  600.00,  'AVAILABLE');
