DECLARE @now DATETIME2 = SYSDATETIME();

DECLARE @roleAdminId INT;
DECLARE @roleCitizenId INT;
DECLARE @roleCollectorId INT;
DECLARE @roleEnterpriseId INT;

SELECT @roleAdminId = id FROM roles WHERE role_code = 'ADMIN';
IF @roleAdminId IS NULL
BEGIN
    INSERT INTO roles (role_code, role_name, description, is_active, created_at)
    VALUES ('ADMIN', 'Administrator', N'Administrator', 1, @now);
    SET @roleAdminId = SCOPE_IDENTITY();
END

SELECT @roleCitizenId = id FROM roles WHERE role_code = 'CITIZEN';
IF @roleCitizenId IS NULL
BEGIN
    INSERT INTO roles (role_code, role_name, description, is_active, created_at)
    VALUES ('CITIZEN', 'Citizen User', N'Citizen', 1, @now);
    SET @roleCitizenId = SCOPE_IDENTITY();
END

SELECT @roleCollectorId = id FROM roles WHERE role_code = 'COLLECTOR';
IF @roleCollectorId IS NULL
BEGIN
    INSERT INTO roles (role_code, role_name, description, is_active, created_at)
    VALUES ('COLLECTOR', 'Collector', N'Collector', 1, @now);
    SET @roleCollectorId = SCOPE_IDENTITY();
END

SELECT @roleEnterpriseId = id FROM roles WHERE role_code = 'ENTERPRISE';
IF @roleEnterpriseId IS NULL
BEGIN
    INSERT INTO roles (role_code, role_name, description, is_active, created_at)
    VALUES ('ENTERPRISE', 'Enterprise', N'Enterprise', 1, @now);
    SET @roleEnterpriseId = SCOPE_IDENTITY();
END

DECLARE @enterpriseId INT;
SELECT @enterpriseId = id FROM enterprise WHERE email = 'enterprise@seed.com';
IF @enterpriseId IS NULL
BEGIN
    INSERT INTO enterprise (name, address, ward, city, phone, email, license_number, tax_code, capacity_kg_per_day, supported_waste_type_codes, service_wards, service_cities, status, total_collected_weight, created_at, updated_at)
    VALUES (N'Seed Enterprise', N'12 Nguyễn Huệ', N'Bến Nghé', N'HCM', '0900000000', 'enterprise@seed.com', 'LIC-SEED-001', 'TAX-SEED-001', 8000.00, N'RECYCLABLE', N'Bến Nghé', N'HCM', 'active', 0.00, @now, @now);
    SET @enterpriseId = SCOPE_IDENTITY();
END

DECLARE @bcrypt NVARCHAR(255) = '$2a$10$7EqJtq98hPqEX7fNZaFWoO5rEo/pZ5lHppZArYrusS4x2ma/p3d.';

DECLARE @adminUserId INT;
SELECT @adminUserId = id FROM users WHERE email = 'admin@seed.com';
IF @adminUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('admin@seed.com', @bcrypt, N'Admin Seed', '0909000000', NULL, @roleAdminId, NULL, 'active', NULL, @now, @now);
    SET @adminUserId = SCOPE_IDENTITY();
END

DECLARE @enterpriseUserId INT;
SELECT @enterpriseUserId = id FROM users WHERE email = 'enterprise@seed.com';
IF @enterpriseUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('enterprise@seed.com', @bcrypt, N'Enterprise Seed', '0908000000', NULL, @roleEnterpriseId, @enterpriseId, 'active', NULL, @now, @now);
    SET @enterpriseUserId = SCOPE_IDENTITY();
END

DECLARE @collectorUserId INT;
SELECT @collectorUserId = id FROM users WHERE email = 'collector@seed.com';
IF @collectorUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('collector@seed.com', @bcrypt, N'Collector Seed', '0901111111', NULL, @roleCollectorId, NULL, 'active', NULL, @now, @now);
    SET @collectorUserId = SCOPE_IDENTITY();
END

DECLARE @citizenUserId INT;
SELECT @citizenUserId = id FROM users WHERE email = 'citizen@seed.com';
IF @citizenUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('citizen@seed.com', @bcrypt, N'Citizen Seed', '0902222222', NULL, @roleCitizenId, NULL, 'active', NULL, @now, @now);
    SET @citizenUserId = SCOPE_IDENTITY();
END

DECLARE @citizenId INT;
SELECT @citizenId = id FROM citizens WHERE user_id = @citizenUserId;
IF @citizenId IS NULL
BEGIN
    INSERT INTO citizens (user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
    VALUES (@citizenUserId, 'citizen@seed.com', N'Citizen Seed', NULL, N'1 Seed Address', '0902222222', N'Bến Nghé', N'HCM', 0, 0, 0);
    SET @citizenId = SCOPE_IDENTITY();
END

DECLARE @collectorId INT;
SELECT @collectorId = id FROM collectors WHERE user_id = @collectorUserId;
IF @collectorId IS NULL
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
    VALUES (@collectorUserId, @enterpriseId, 'collector@seed.com', N'Collector Seed', 'EMP-SEED-001', N'Xe tải nhỏ', '59C-10001', 'AVAILABLE', @now, 0, 0, 0.00, @now);
    SET @collectorId = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Giấy')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Giấy', NULL, 'KG', 2250.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Lon nhôm')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Lon nhôm', NULL, 'CAN', 180.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Chai nhựa PET')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Chai nhựa PET', NULL, 'BOTTLE', 120.0000, @now, @now);

DECLARE @catPaperId INT, @catCanId INT, @catPetId INT;
SELECT @catPaperId = id FROM waste_categories WHERE name = N'Giấy';
SELECT @catCanId = id FROM waste_categories WHERE name = N'Lon nhôm';
SELECT @catPetId = id FROM waste_categories WHERE name = N'Chai nhựa PET';

DECLARE @wr1 INT, @wr2 INT;
SELECT @wr1 = id FROM waste_reports WHERE report_code = 'WR-SEED-001';
IF @wr1 IS NULL
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, description, waste_type, estimated_weight, latitude, longitude, address, images, status, created_at, updated_at)
    VALUES ('WR-SEED-001', @citizenId, N'Báo cáo rác tái chế #SEED-001', 'RECYCLABLE', 1.20, 10.77653000, 106.70098000, N'Quận 1 - Điểm 001', N'https://example.com/seed/wr1.jpg', 'ASSIGNED', DATEADD(HOUR, -6, @now), DATEADD(HOUR, -6, @now));
    SET @wr1 = SCOPE_IDENTITY();
END

SELECT @wr2 = id FROM waste_reports WHERE report_code = 'WR-SEED-002';
IF @wr2 IS NULL
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, description, waste_type, estimated_weight, latitude, longitude, address, images, status, created_at, updated_at)
    VALUES ('WR-SEED-002', @citizenId, N'Báo cáo rác tái chế #SEED-002', 'RECYCLABLE', 2.50, 10.77664000, 106.70108000, N'Quận 1 - Điểm 002', N'https://example.com/seed/wr2.jpg', 'COLLECTED', DATEADD(HOUR, -10, @now), DATEADD(HOUR, -8, @now));
    SET @wr2 = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM waste_report_items WHERE report_id = @wr1)
BEGIN
    INSERT INTO waste_report_items (report_id, waste_category_id, quantity, unit_snapshot, created_at)
    VALUES
    (@wr1, @catPaperId, 0.70, 'KG', DATEADD(HOUR, -6, @now)),
    (@wr1, @catCanId, 2, 'CAN', DATEADD(HOUR, -6, @now));
END

IF NOT EXISTS (SELECT 1 FROM waste_report_items WHERE report_id = @wr2)
BEGIN
    INSERT INTO waste_report_items (report_id, waste_category_id, quantity, unit_snapshot, created_at)
    VALUES
    (@wr2, @catPetId, 4, 'BOTTLE', DATEADD(HOUR, -10, @now)),
    (@wr2, @catPaperId, 0.25, 'KG', DATEADD(HOUR, -10, @now));
END

IF NOT EXISTS (SELECT 1 FROM report_images WHERE report_id = @wr1)
    INSERT INTO report_images (report_id, image_url, image_type, uploaded_at) VALUES (@wr1, 'https://example.com/seed/wr1.jpg', 'BEFORE', DATEADD(HOUR, -6, @now));
IF NOT EXISTS (SELECT 1 FROM report_images WHERE report_id = @wr2)
    INSERT INTO report_images (report_id, image_url, image_type, uploaded_at) VALUES (@wr2, 'https://example.com/seed/wr2.jpg', 'BEFORE', DATEADD(HOUR, -10, @now));

DECLARE @cr1 INT, @cr2 INT;
SELECT @cr1 = id FROM collection_requests WHERE request_code = 'CR-SEED-001';
IF @cr1 IS NULL
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-SEED-001', @wr1, @enterpriseId, @collectorId, 'ASSIGNED', NULL, DATEADD(HOUR, -5, @now), NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -6, @now), DATEADD(HOUR, -5, @now));
    SET @cr1 = SCOPE_IDENTITY();
END

SELECT @cr2 = id FROM collection_requests WHERE request_code = 'CR-SEED-002';
IF @cr2 IS NULL
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-SEED-002', @wr2, @enterpriseId, @collectorId, 'COLLECTED', NULL, DATEADD(HOUR, -9, @now), DATEADD(HOUR, -9, @now), DATEADD(HOUR, -8, @now), 2.75, DATEADD(HOUR, -7, @now), NULL, DATEADD(HOUR, -10, @now), DATEADD(HOUR, -7, @now));
    SET @cr2 = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM collection_tracking WHERE collection_request_id = @cr1)
BEGIN
    INSERT INTO collection_tracking (collection_request_id, collector_id, action, latitude, longitude, note, images, created_at)
    VALUES (@cr1, @collectorId, 'assigned', 10.77653000, 106.70098000, N'Assigned', NULL, DATEADD(HOUR, -5, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_tracking WHERE collection_request_id = @cr2 AND action = 'assigned')
    INSERT INTO collection_tracking (collection_request_id, collector_id, action, latitude, longitude, note, images, created_at)
    VALUES (@cr2, @collectorId, 'assigned', 10.77664000, 106.70108000, N'Assigned', NULL, DATEADD(HOUR, -9, @now));
IF NOT EXISTS (SELECT 1 FROM collection_tracking WHERE collection_request_id = @cr2 AND action = 'accepted')
    INSERT INTO collection_tracking (collection_request_id, collector_id, action, latitude, longitude, note, images, created_at)
    VALUES (@cr2, @collectorId, 'accepted', 10.77664000, 106.70108000, N'Accepted', NULL, DATEADD(HOUR, -9, @now));
IF NOT EXISTS (SELECT 1 FROM collection_tracking WHERE collection_request_id = @cr2 AND action = 'started')
    INSERT INTO collection_tracking (collection_request_id, collector_id, action, latitude, longitude, note, images, created_at)
    VALUES (@cr2, @collectorId, 'started', 10.77664000, 106.70108000, N'Started', NULL, DATEADD(HOUR, -8, @now));
IF NOT EXISTS (SELECT 1 FROM collection_tracking WHERE collection_request_id = @cr2 AND action = 'collected')
    INSERT INTO collection_tracking (collection_request_id, collector_id, action, latitude, longitude, note, images, created_at)
    VALUES (@cr2, @collectorId, 'collected', 10.77664000, 106.70108000, N'Completed', N'["https://example.com/seed/rep2_before.jpg","https://example.com/seed/rep2_after.jpg"]', DATEADD(HOUR, -7, @now));

DECLARE @collectorReportId INT;
SELECT @collectorReportId = id FROM collector_reports WHERE report_code = 'CRPT-SEED-002';
IF @collectorReportId IS NULL
BEGIN
    INSERT INTO collector_reports (report_code, collection_request_id, collector_id, status, collector_note, total_point, actual_weight_recyclable, collected_at, latitude, longitude, created_at)
    VALUES ('CRPT-SEED-002', @cr2, @collectorId, 'COMPLETED', N'Hoàn tất thu gom seed', 5000, 2.75, DATEADD(HOUR, -7, @now), 10.77664000, 106.70108000, DATEADD(HOUR, -7, @now));
    SET @collectorReportId = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM collector_report_images WHERE collector_report_id = @collectorReportId)
BEGIN
    INSERT INTO collector_report_images (collector_report_id, image_url, image_public_id, created_at)
    VALUES
    (@collectorReportId, 'https://example.com/seed/collector2_before.jpg', 'seed_collector2_before', DATEADD(HOUR, -7, @now)),
    (@collectorReportId, 'https://example.com/seed/collector2_after.jpg', 'seed_collector2_after', DATEADD(HOUR, -7, @now));
END

IF NOT EXISTS (SELECT 1 FROM collector_report_items WHERE collector_report_id = @collectorReportId)
BEGIN
    INSERT INTO collector_report_items (collector_report_id, waste_category_id, quantity, unit_snapshot, point_per_unit_snapshot, total_point, created_at)
    VALUES
    (@collectorReportId, @catPaperId, 0.75, 'KG', 2250.0000, 1688, DATEADD(HOUR, -7, @now)),
    (@collectorReportId, @catCanId, 3, 'CAN', 180.0000, 540, DATEADD(HOUR, -7, @now));
END
