-- ============================================================================
-- GENERATED TEST DATA FOR COLLECTOR WORKFLOW
-- Corrected to match Entity definitions exactly (SQL Server / T-SQL)
-- ============================================================================

-- ============================================================================
-- 1. ROLES
-- Table: roles
-- Entity: Role
-- ============================================================================
SET IDENTITY_INSERT roles ON;
IF NOT EXISTS (SELECT 1 FROM roles WHERE id = 1) INSERT INTO roles (id, role_code, role_name, description, is_active, created_at) VALUES (1, 'ADMIN', 'Administrator', 'System Administrator', 1, GETDATE());
IF NOT EXISTS (SELECT 1 FROM roles WHERE id = 2) INSERT INTO roles (id, role_code, role_name, description, is_active, created_at) VALUES (2, 'ENTERPRISE', 'Enterprise', 'Waste Collection Enterprise', 1, GETDATE());
IF NOT EXISTS (SELECT 1 FROM roles WHERE id = 3) INSERT INTO roles (id, role_code, role_name, description, is_active, created_at) VALUES (3, 'COLLECTOR', 'Collector', 'Waste Collector', 1, GETDATE());
IF NOT EXISTS (SELECT 1 FROM roles WHERE id = 4) INSERT INTO roles (id, role_code, role_name, description, is_active, created_at) VALUES (4, 'CITIZEN', 'Citizen', 'Resident/User', 1, GETDATE());
SET IDENTITY_INSERT roles OFF;

-- ============================================================================
-- 2. USERS
-- Table: users
-- Entity: User
-- ============================================================================
SET IDENTITY_INSERT users ON;

-- Enterprise User
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 201)
INSERT INTO users (id, email, password_hash, full_name, phone, role_id, status, created_at, updated_at)
VALUES (201, 'enterprise@example.com', '$2a$10$dummyHash', 'Green Earth Enterprise', '0901111111', 2, 'active', GETDATE(), GETDATE());

-- Collector User
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 301)
INSERT INTO users (id, email, password_hash, full_name, phone, role_id, status, created_at, updated_at)
VALUES (301, 'collector@example.com', '$2a$10$dummyHash', 'Nguyen Van Collector', '0902222222', 3, 'active', GETDATE(), GETDATE());

-- Citizen User
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 401)
INSERT INTO users (id, email, password_hash, full_name, phone, role_id, status, created_at, updated_at)
VALUES (401, 'citizen@example.com', '$2a$10$dummyHash', 'Le Thi Citizen', '0903333333', 4, 'active', GETDATE(), GETDATE());

SET IDENTITY_INSERT users OFF;

-- ============================================================================
-- 3. ENTERPRISES
-- Table: enterprise (Singular name in Entity)
-- Entity: Enterprise
-- ============================================================================
SET IDENTITY_INSERT enterprise ON;

IF NOT EXISTS (SELECT 1 FROM enterprise WHERE id = 1)
INSERT INTO enterprise (id, name, address, ward, city, phone, email, license_number, tax_code, capacity_kg_per_day, status, created_at, updated_at)
VALUES (1, N'Green Earth Enterprise', N'123 Green St', N'Ward 1', N'HCM City', '0901111111', 'enterprise@example.com', 'LIC-001', 'TAX-001', 5000.00, 'active', GETDATE(), GETDATE());

SET IDENTITY_INSERT enterprise OFF;

-- Update User link
UPDATE users SET enterprise_id = 1 WHERE id = 201;

-- ============================================================================
-- 4. CITIZENS
-- Table: citizens
-- Entity: Citizen
-- ============================================================================
SET IDENTITY_INSERT citizens ON;

IF NOT EXISTS (SELECT 1 FROM citizens WHERE id = 1)
INSERT INTO citizens (id, user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
VALUES (1, 401, 'citizen@example.com', N'Le Thi Citizen', '$2a$10$dummyHash', N'456 Citizen Rd', '0903333333', N'Ward 2', N'HCM City', 100, 5, 5);

SET IDENTITY_INSERT citizens OFF;

-- ============================================================================
-- 5. COLLECTORS
-- Table: collectors
-- Entity: Collector
-- ============================================================================
SET IDENTITY_INSERT collectors ON;

IF NOT EXISTS (SELECT 1 FROM collectors WHERE id = 1)
INSERT INTO collectors (id, user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, current_latitude, current_longitude, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
VALUES (1, 301, 1, 'collector@example.com', N'Nguyen Van Collector', 'COL-001', 'TRUCK', '59C-12345', 'AVAILABLE', 10.762622, 106.660172, GETDATE(), 10, 10, 500.50, GETDATE());

SET IDENTITY_INSERT collectors OFF;

-- ============================================================================
-- 6. WASTE TYPES
-- Table: waste_types
-- Entity: WasteType
-- ============================================================================
SET IDENTITY_INSERT waste_types ON;

IF NOT EXISTS (SELECT 1 FROM waste_types WHERE id = 1)
INSERT INTO waste_types (id, code, name, category, base_points, is_recyclable, created_at)
VALUES (1, 'PLASTIC', N'Plastic Waste', 'RECYCLABLE', 10, 1, GETDATE());

SET IDENTITY_INSERT waste_types OFF;

-- ============================================================================
-- 7. WASTE REPORTS
-- Table: waste_reports
-- Entity: WasteReport
-- ============================================================================
SET IDENTITY_INSERT waste_reports ON;

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE id = 1)
INSERT INTO waste_reports (id, report_code, citizen_id, waste_type_id, description, latitude, longitude, address, status, images, created_at, updated_at)
VALUES (1, 'WR-20260205-001', 1, 1, N'Pile of plastic bottles', 10.762622, 106.660172, N'456 Citizen Rd', 'PENDING', '["https://res.cloudinary.com/demo/image/upload/sample.jpg"]', GETDATE(), GETDATE());

SET IDENTITY_INSERT waste_reports OFF;

-- ============================================================================
-- 8. COLLECTION REQUESTS
-- Table: collection_requests
-- Entity: CollectionRequest
-- ============================================================================
SET IDENTITY_INSERT collection_requests ON;

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE id = 1)
INSERT INTO collection_requests (id, request_code, report_id, enterprise_id, collector_id, status, assigned_at, accepted_at, started_at, collected_at, actual_weight_kg, created_at, updated_at)
VALUES (1, 'REQ-20260205-001', 1, 1, 1, 'COLLECTED', 
        DATEADD(hour, -2, GETDATE()), 
        DATEADD(hour, -1, GETDATE()), 
        DATEADD(minute, -30, GETDATE()), 
        GETDATE(), 
        15.50, -- actual_weight_kg from Entity
        DATEADD(hour, -2, GETDATE()), 
        GETDATE());

SET IDENTITY_INSERT collection_requests OFF;

-- ============================================================================
-- 9. COLLECTOR REPORTS
-- Table: collector_reports
-- Entity: CollectorReport
-- ============================================================================
SET IDENTITY_INSERT collector_reports ON;

IF NOT EXISTS (SELECT 1 FROM collector_reports WHERE id = 1)
INSERT INTO collector_reports (id, collection_request_id, collector_id, status, collector_note, actual_weight, collected_at, latitude, longitude, created_at)
VALUES (1, 1, 1, 'COMPLETED', 
        N'Verified weight on site. All clear.', 
        15.50, -- actual_weight from Entity (NEW FIELD)
        GETDATE(), 
        10.762622, 
        106.660172, 
        GETDATE());

SET IDENTITY_INSERT collector_reports OFF;

-- ============================================================================
-- 10. COLLECTOR REPORT IMAGES
-- Table: collector_report_images
-- Entity: CollectorReportImage
-- ============================================================================
SET IDENTITY_INSERT collector_report_images ON;

IF NOT EXISTS (SELECT 1 FROM collector_report_images WHERE id = 1)
INSERT INTO collector_report_images (id, collector_report_id, image_url, image_public_id, created_at)
VALUES (1, 1, 'https://res.cloudinary.com/demo/image/upload/report_sample.jpg', 'report_sample_id', GETDATE());

SET IDENTITY_INSERT collector_report_images OFF;

-- ============================================================================
-- VERIFICATION
-- ============================================================================
PRINT 'Data Generation Complete.';
PRINT '--- Collector Reports ---';
SELECT id, status, actual_weight, collector_note FROM collector_reports;
