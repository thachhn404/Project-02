USE WasteManagementDB;
GO

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
    VALUES ('CITIZEN', 'Citizen User', N'Citizen User', 1, @now);
    SET @roleCitizenId = SCOPE_IDENTITY();
END

SELECT @roleCollectorId = id FROM roles WHERE role_code = 'COLLECTOR';
IF @roleCollectorId IS NULL
BEGIN
    INSERT INTO roles (role_code, role_name, description, is_active, created_at)
    VALUES ('COLLECTOR', 'Waste Collector', N'Waste Collector', 1, @now);
    SET @roleCollectorId = SCOPE_IDENTITY();
END

SELECT @roleEnterpriseId = id FROM roles WHERE role_code = 'ENTERPRISE';
IF @roleEnterpriseId IS NULL
BEGIN
    INSERT INTO roles (role_code, role_name, description, is_active, created_at)
    VALUES ('ENTERPRISE', 'Recycling Enterprise', N'Recycling Enterprise', 1, @now);
    SET @roleEnterpriseId = SCOPE_IDENTITY();
END

DECLARE @enterpriseId INT;
SELECT @enterpriseId = id FROM enterprise WHERE email = 'enterprise@demo.com';
IF @enterpriseId IS NULL
BEGIN
    INSERT INTO enterprise (name, address, ward, city, phone, email, license_number, tax_code, capacity_kg_per_day, supported_waste_type_codes, service_wards, service_cities, status, total_collected_weight, created_at, updated_at)
    VALUES (N'Demo Recycling Enterprise', N'12 Nguyễn Huệ', N'Bến Nghé', N'HCM', '0900000000', 'enterprise@demo.com', 'LIC-DEMO-001', 'TAX-DEMO-001', 8000.00, N'RECYCLABLE', N'Bến Nghé;Đa Kao', N'HCM', 'active', 0.00, @now, @now);
    SET @enterpriseId = SCOPE_IDENTITY();
END

DECLARE @bcrypt NVARCHAR(255) = '$2a$10$7EqJtq98hPqEX7fNZaFWoO5rEo/pZ5lHppZArYrusS4x2ma/p3d.';

DECLARE @adminUserId INT;
SELECT @adminUserId = id FROM users WHERE email = 'admin@demo.com';
IF @adminUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('admin@demo.com', @bcrypt, N'Admin Demo', '0909000000', NULL, @roleAdminId, NULL, 'active', NULL, @now, @now);
    SET @adminUserId = SCOPE_IDENTITY();
END

DECLARE @enterpriseUserId INT;
SELECT @enterpriseUserId = id FROM users WHERE email = 'enterprise@demo.com';
IF @enterpriseUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('enterprise@demo.com', @bcrypt, N'Enterprise Demo', '0908000000', NULL, @roleEnterpriseId, @enterpriseId, 'active', NULL, @now, @now);
    SET @enterpriseUserId = SCOPE_IDENTITY();
END

DECLARE @collectorUserId1 INT, @collectorUserId2 INT, @collectorUserId3 INT, @collectorUserId4 INT;
SELECT @collectorUserId1 = id FROM users WHERE email = 'collector1@demo.com';
IF @collectorUserId1 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('collector1@demo.com', @bcrypt, N'Collector 01', '0901111111', NULL, @roleCollectorId, NULL, 'active', NULL, @now, @now);
    SET @collectorUserId1 = SCOPE_IDENTITY();
END

SELECT @collectorUserId2 = id FROM users WHERE email = 'collector2@demo.com';
IF @collectorUserId2 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('collector2@demo.com', @bcrypt, N'Collector 02', '0901111112', NULL, @roleCollectorId, NULL, 'active', NULL, @now, @now);
    SET @collectorUserId2 = SCOPE_IDENTITY();
END

SELECT @collectorUserId3 = id FROM users WHERE email = 'collector3@demo.com';
IF @collectorUserId3 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('collector3@demo.com', @bcrypt, N'Collector 03', '0901111113', NULL, @roleCollectorId, NULL, 'active', NULL, @now, @now);
    SET @collectorUserId3 = SCOPE_IDENTITY();
END

SELECT @collectorUserId4 = id FROM users WHERE email = 'collector4@demo.com';
IF @collectorUserId4 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('collector4@demo.com', @bcrypt, N'Collector 04', '0901111114', NULL, @roleCollectorId, NULL, 'active', NULL, @now, @now);
    SET @collectorUserId4 = SCOPE_IDENTITY();
END

DECLARE @citizenUserId1 INT, @citizenUserId2 INT, @citizenUserId3 INT, @citizenUserId4 INT, @citizenUserId5 INT, @citizenUserId6 INT;
SELECT @citizenUserId1 = id FROM users WHERE email = 'citizen1@demo.com';
IF @citizenUserId1 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('citizen1@demo.com', @bcrypt, N'Citizen 01', '0902222201', NULL, @roleCitizenId, NULL, 'active', NULL, @now, @now);
    SET @citizenUserId1 = SCOPE_IDENTITY();
END

SELECT @citizenUserId2 = id FROM users WHERE email = 'citizen2@demo.com';
IF @citizenUserId2 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('citizen2@demo.com', @bcrypt, N'Citizen 02', '0902222202', NULL, @roleCitizenId, NULL, 'active', NULL, @now, @now);
    SET @citizenUserId2 = SCOPE_IDENTITY();
END

SELECT @citizenUserId3 = id FROM users WHERE email = 'citizen3@demo.com';
IF @citizenUserId3 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('citizen3@demo.com', @bcrypt, N'Citizen 03', '0902222203', NULL, @roleCitizenId, NULL, 'active', NULL, @now, @now);
    SET @citizenUserId3 = SCOPE_IDENTITY();
END

SELECT @citizenUserId4 = id FROM users WHERE email = 'citizen4@demo.com';
IF @citizenUserId4 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('citizen4@demo.com', @bcrypt, N'Citizen 04', '0902222204', NULL, @roleCitizenId, NULL, 'active', NULL, @now, @now);
    SET @citizenUserId4 = SCOPE_IDENTITY();
END

SELECT @citizenUserId5 = id FROM users WHERE email = 'citizen5@demo.com';
IF @citizenUserId5 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('citizen5@demo.com', @bcrypt, N'Citizen 05', '0902222205', NULL, @roleCitizenId, NULL, 'active', NULL, @now, @now);
    SET @citizenUserId5 = SCOPE_IDENTITY();
END

SELECT @citizenUserId6 = id FROM users WHERE email = 'citizen6@demo.com';
IF @citizenUserId6 IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES ('citizen6@demo.com', @bcrypt, N'Citizen 06', '0902222206', NULL, @roleCitizenId, NULL, 'active', NULL, @now, @now);
    SET @citizenUserId6 = SCOPE_IDENTITY();
END

DECLARE @citizenId1 INT, @citizenId2 INT, @citizenId3 INT, @citizenId4 INT, @citizenId5 INT, @citizenId6 INT;
SELECT @citizenId1 = id FROM citizens WHERE user_id = @citizenUserId1;
IF @citizenId1 IS NULL
BEGIN
    INSERT INTO citizens (user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
    VALUES (@citizenUserId1, 'citizen1@demo.com', N'Citizen 01', NULL, N'1 Demo Address', '0902222201', N'Bến Nghé', N'HCM', 0, 0, 0);
    SET @citizenId1 = SCOPE_IDENTITY();
END

SELECT @citizenId2 = id FROM citizens WHERE user_id = @citizenUserId2;
IF @citizenId2 IS NULL
BEGIN
    INSERT INTO citizens (user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
    VALUES (@citizenUserId2, 'citizen2@demo.com', N'Citizen 02', NULL, N'2 Demo Address', '0902222202', N'Bến Nghé', N'HCM', 0, 0, 0);
    SET @citizenId2 = SCOPE_IDENTITY();
END

SELECT @citizenId3 = id FROM citizens WHERE user_id = @citizenUserId3;
IF @citizenId3 IS NULL
BEGIN
    INSERT INTO citizens (user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
    VALUES (@citizenUserId3, 'citizen3@demo.com', N'Citizen 03', NULL, N'3 Demo Address', '0902222203', N'Bến Nghé', N'HCM', 0, 0, 0);
    SET @citizenId3 = SCOPE_IDENTITY();
END

SELECT @citizenId4 = id FROM citizens WHERE user_id = @citizenUserId4;
IF @citizenId4 IS NULL
BEGIN
    INSERT INTO citizens (user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
    VALUES (@citizenUserId4, 'citizen4@demo.com', N'Citizen 04', NULL, N'4 Demo Address', '0902222204', N'Đa Kao', N'HCM', 0, 0, 0);
    SET @citizenId4 = SCOPE_IDENTITY();
END

SELECT @citizenId5 = id FROM citizens WHERE user_id = @citizenUserId5;
IF @citizenId5 IS NULL
BEGIN
    INSERT INTO citizens (user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
    VALUES (@citizenUserId5, 'citizen5@demo.com', N'Citizen 05', NULL, N'5 Demo Address', '0902222205', N'Đa Kao', N'HCM', 0, 0, 0);
    SET @citizenId5 = SCOPE_IDENTITY();
END

SELECT @citizenId6 = id FROM citizens WHERE user_id = @citizenUserId6;
IF @citizenId6 IS NULL
BEGIN
    INSERT INTO citizens (user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
    VALUES (@citizenUserId6, 'citizen6@demo.com', N'Citizen 06', NULL, N'6 Demo Address', '0902222206', N'Đa Kao', N'HCM', 0, 0, 0);
    SET @citizenId6 = SCOPE_IDENTITY();
END

DECLARE @collectorId1 INT, @collectorId2 INT, @collectorId3 INT, @collectorId4 INT;
SELECT @collectorId1 = id FROM collectors WHERE user_id = @collectorUserId1;
IF @collectorId1 IS NULL
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, current_latitude, current_longitude, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
    VALUES (@collectorUserId1, @enterpriseId, 'collector1@demo.com', N'Collector 01', 'EMP-001', N'Xe tải nhỏ', '59C-10001', 'AVAILABLE', 10.77690000, 106.70090000, @now, 0, 0, 0.00, @now);
    SET @collectorId1 = SCOPE_IDENTITY();
END

SELECT @collectorId2 = id FROM collectors WHERE user_id = @collectorUserId2;
IF @collectorId2 IS NULL
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, current_latitude, current_longitude, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
    VALUES (@collectorUserId2, @enterpriseId, 'collector2@demo.com', N'Collector 02', 'EMP-002', N'Xe ba gác', '59C-10002', 'AVAILABLE', 10.77710000, 106.70110000, @now, 0, 0, 0.00, @now);
    SET @collectorId2 = SCOPE_IDENTITY();
END

SELECT @collectorId3 = id FROM collectors WHERE user_id = @collectorUserId3;
IF @collectorId3 IS NULL
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, current_latitude, current_longitude, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
    VALUES (@collectorUserId3, @enterpriseId, 'collector3@demo.com', N'Collector 03', 'EMP-003', N'Xe tải nhỏ', '59C-10003', 'AVAILABLE', 10.77730000, 106.70130000, @now, 0, 0, 0.00, @now);
    SET @collectorId3 = SCOPE_IDENTITY();
END

SELECT @collectorId4 = id FROM collectors WHERE user_id = @collectorUserId4;
IF @collectorId4 IS NULL
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, current_latitude, current_longitude, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
    VALUES (@collectorUserId4, @enterpriseId, 'collector4@demo.com', N'Collector 04', 'EMP-004', N'Xe máy', '59C-10004', 'AVAILABLE', 10.77750000, 106.70150000, @now, 0, 0, 0.00, @now);
    SET @collectorId4 = SCOPE_IDENTITY();
END

DECLARE @wtRecyclableId INT, @wtHouseholdId INT, @wtHazardousId INT;
SELECT @wtRecyclableId = id FROM waste_types WHERE code = 'RECYCLABLE';
IF @wtRecyclableId IS NULL
BEGIN
    INSERT INTO waste_types (code, name, base_points, sla_hours, is_recyclable, created_at)
    VALUES ('RECYCLABLE', N'Rác tái chế', 20, 24, 1, @now);
    SET @wtRecyclableId = SCOPE_IDENTITY();
END

SELECT @wtHouseholdId = id FROM waste_types WHERE code = 'HOUSEHOLD';
IF @wtHouseholdId IS NULL
BEGIN
    INSERT INTO waste_types (code, name, base_points, sla_hours, is_recyclable, created_at)
    VALUES ('HOUSEHOLD', N'Rác sinh hoạt', 5, 24, 0, @now);
    SET @wtHouseholdId = SCOPE_IDENTITY();
END

SELECT @wtHazardousId = id FROM waste_types WHERE code = 'HAZARDOUS';
IF @wtHazardousId IS NULL
BEGIN
    INSERT INTO waste_types (code, name, base_points, sla_hours, is_recyclable, created_at)
    VALUES ('HAZARDOUS', N'Rác nguy hại', 50, 12, 0, @now);
    SET @wtHazardousId = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Giấy')
BEGIN
    INSERT INTO waste_categories (name, description, unit, point_per_unit, waste_type_id, created_at, updated_at)
    VALUES
    (N'Giấy', N'Giấy vụn, giấy trắng, giấy in', 'KG', 2250.0000, @wtRecyclableId, @now, @now),
    (N'Bìa carton', N'Thùng carton, bìa cứng', 'KG', 2000.0000, @wtRecyclableId, @now, @now),
    (N'Hộp sữa giấy', N'Hộp sữa/Tetra Pak', 'KG', 1800.0000, @wtRecyclableId, @now, @now),
    (N'Báo - tạp chí', N'Báo giấy, tạp chí cũ', 'KG', 1600.0000, @wtRecyclableId, @now, @now),
    (N'Chai nhựa PET', N'Chai nước suối, nước ngọt (PET)', 'BOTTLE', 120.0000, @wtRecyclableId, @now, @now),
    (N'Chai nhựa HDPE', N'Can/chai nhựa cứng (HDPE)', 'BOTTLE', 150.0000, @wtRecyclableId, @now, @now),
    (N'Nhựa PP cứng', N'Nhựa cứng PP (đồ gia dụng)', 'KG', 2500.0000, @wtRecyclableId, @now, @now),
    (N'Nhựa mềm', N'Nhựa dẻo, màng bọc', 'KG', 1000.0000, @wtRecyclableId, @now, @now),
    (N'Túi nilon', N'Túi nylon, bao bì mỏng', 'KG', 800.0000, @wtRecyclableId, @now, @now),
    (N'Thủy tinh vỡ', N'Mảnh thủy tinh', 'KG', 500.0000, @wtRecyclableId, @now, @now),
    (N'Chai thủy tinh', N'Chai thủy tinh còn nguyên', 'BOTTLE', 200.0000, @wtRecyclableId, @now, @now),
    (N'Lon nhôm', N'Lon nước ngọt/lon bia', 'CAN', 180.0000, @wtRecyclableId, @now, @now),
    (N'Lon thiếc', N'Lon đồ hộp/thiếc', 'CAN', 120.0000, @wtRecyclableId, @now, @now),
    (N'Sắt vụn', N'Sắt thép phế liệu', 'KG', 3500.0000, @wtRecyclableId, @now, @now),
    (N'Inox', N'Inox phế liệu', 'KG', 8000.0000, @wtRecyclableId, @now, @now),
    (N'Đồng', N'Đồng phế liệu', 'KG', 67500.0000, @wtRecyclableId, @now, @now),
    (N'Nhôm', N'Nhôm phế liệu', 'KG', 9000.0000, @wtRecyclableId, @now, @now),
    (N'Dây điện', N'Dây điện/đồng bọc nhựa', 'KG', 12000.0000, @wtRecyclableId, @now, @now),
    (N'Thiết bị điện tử nhỏ', N'Đồ điện tử nhỏ hư hỏng', 'KG', 15000.0000, @wtRecyclableId, @now, @now),
    (N'Vải - quần áo cũ', N'Vải, quần áo cũ', 'KG', 500.0000, @wtRecyclableId, @now, @now);
END

DECLARE @wr1 INT, @wr2 INT, @wr3 INT, @wr4 INT, @wr5 INT, @wr6 INT, @wr7 INT, @wr8 INT, @wr9 INT, @wr10 INT;

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-001')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-001', @citizenId1, @wtRecyclableId, N'Báo cáo rác tái chế #001', 1.20, 10.77653000, 106.70098000, N'Quận 1 - Điểm 001', N'https://example.com/wr/1.jpg', NULL, 'PENDING', DATEADD(HOUR, -6, @now), DATEADD(HOUR, -6, @now));
    SET @wr1 = SCOPE_IDENTITY();
END
ELSE SELECT @wr1 = id FROM waste_reports WHERE report_code = 'WR-DEMO-001';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-002')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-002', @citizenId2, @wtRecyclableId, N'Báo cáo rác tái chế #002', 2.50, 10.77664000, 106.70108000, N'Quận 1 - Điểm 002', N'https://example.com/wr/2.jpg', NULL, 'ACCEPTED_ENTERPRISE', DATEADD(HOUR, -10, @now), DATEADD(HOUR, -8, @now));
    SET @wr2 = SCOPE_IDENTITY();
END
ELSE SELECT @wr2 = id FROM waste_reports WHERE report_code = 'WR-DEMO-002';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-003')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-003', @citizenId3, @wtRecyclableId, N'Báo cáo rác tái chế #003', 0.80, 10.77675000, 106.70118000, N'Quận 1 - Điểm 003', N'https://example.com/wr/3.jpg', NULL, 'ASSIGNED', DATEADD(HOUR, -5, @now), DATEADD(HOUR, -4, @now));
    SET @wr3 = SCOPE_IDENTITY();
END
ELSE SELECT @wr3 = id FROM waste_reports WHERE report_code = 'WR-DEMO-003';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-004')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-004', @citizenId4, @wtRecyclableId, N'Báo cáo rác tái chế #004', 3.10, 10.77686000, 106.70128000, N'Quận 1 - Điểm 004', N'https://example.com/wr/4.jpg', NULL, 'ON_THE_WAY', DATEADD(HOUR, -4, @now), DATEADD(HOUR, -3, @now));
    SET @wr4 = SCOPE_IDENTITY();
END
ELSE SELECT @wr4 = id FROM waste_reports WHERE report_code = 'WR-DEMO-004';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-005')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-005', @citizenId5, @wtRecyclableId, N'Báo cáo rác tái chế #005', 1.80, 10.77697000, 106.70138000, N'Quận 1 - Điểm 005', N'https://example.com/wr/5.jpg', NULL, 'COLLECTED', DATEADD(HOUR, -12, @now), DATEADD(HOUR, -2, @now));
    SET @wr5 = SCOPE_IDENTITY();
END
ELSE SELECT @wr5 = id FROM waste_reports WHERE report_code = 'WR-DEMO-005';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-006')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-006', @citizenId6, @wtRecyclableId, N'Báo cáo rác tái chế #006', 0.60, 10.77708000, 106.70148000, N'Quận 1 - Điểm 006', N'https://example.com/wr/6.jpg', NULL, 'COLLECTED', DATEADD(HOUR, -24, @now), DATEADD(HOUR, -1, @now));
    SET @wr6 = SCOPE_IDENTITY();
END
ELSE SELECT @wr6 = id FROM waste_reports WHERE report_code = 'WR-DEMO-006';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-007')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-007', @citizenId1, @wtRecyclableId, N'Báo cáo rác tái chế #007', 2.20, 10.77719000, 106.70158000, N'Quận 1 - Điểm 007', N'https://example.com/wr/7.jpg', NULL, 'PENDING', DATEADD(HOUR, -2, @now), DATEADD(HOUR, -2, @now));
    SET @wr7 = SCOPE_IDENTITY();
END
ELSE SELECT @wr7 = id FROM waste_reports WHERE report_code = 'WR-DEMO-007';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-008')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-008', @citizenId2, @wtRecyclableId, N'Báo cáo rác tái chế #008', 1.40, 10.77730000, 106.70168000, N'Quận 1 - Điểm 008', N'https://example.com/wr/8.jpg', NULL, 'ACCEPTED_ENTERPRISE', DATEADD(HOUR, -9, @now), DATEADD(HOUR, -8, @now));
    SET @wr8 = SCOPE_IDENTITY();
END
ELSE SELECT @wr8 = id FROM waste_reports WHERE report_code = 'WR-DEMO-008';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-009')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-009', @citizenId3, @wtRecyclableId, N'Báo cáo rác tái chế #009', 4.00, 10.77741000, 106.70178000, N'Quận 1 - Điểm 009', N'https://example.com/wr/9.jpg', NULL, 'ASSIGNED', DATEADD(HOUR, -7, @now), DATEADD(HOUR, -6, @now));
    SET @wr9 = SCOPE_IDENTITY();
END
ELSE SELECT @wr9 = id FROM waste_reports WHERE report_code = 'WR-DEMO-009';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-010')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-010', @citizenId4, @wtRecyclableId, N'Báo cáo rác tái chế #010', 1.00, 10.77752000, 106.70188000, N'Quận 1 - Điểm 010', N'https://example.com/wr/10.jpg', NULL, 'REJECTED', DATEADD(HOUR, -20, @now), DATEADD(HOUR, -19, @now));
    SET @wr10 = SCOPE_IDENTITY();
END
ELSE SELECT @wr10 = id FROM waste_reports WHERE report_code = 'WR-DEMO-010';

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-001')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-001', @wr1, @enterpriseId, NULL, 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -6, @now), DATEADD(HOUR, -6, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-002')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-002', @wr2, @enterpriseId, NULL, 'ACCEPTED_ENTERPRISE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -10, @now), DATEADD(HOUR, -8, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-003')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-003', @wr3, @enterpriseId, @collectorId1, 'ASSIGNED', NULL, DATEADD(HOUR, -4, @now), NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -5, @now), DATEADD(HOUR, -4, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-004')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-004', @wr4, @enterpriseId, @collectorId2, 'ON_THE_WAY', NULL, DATEADD(HOUR, -3, @now), DATEADD(HOUR, -3, @now), DATEADD(HOUR, -3, @now), NULL, NULL, NULL, DATEADD(HOUR, -4, @now), DATEADD(HOUR, -3, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-005')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-005', @wr5, @enterpriseId, @collectorId3, 'COLLECTED', NULL, DATEADD(HOUR, -3, @now), DATEADD(HOUR, -3, @now), DATEADD(HOUR, -3, @now), 1.75, DATEADD(HOUR, -2, @now), NULL, DATEADD(HOUR, -12, @now), DATEADD(HOUR, -2, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-006')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-006', @wr6, @enterpriseId, @collectorId4, 'COMPLETED', NULL, DATEADD(HOUR, -5, @now), DATEADD(HOUR, -5, @now), DATEADD(HOUR, -5, @now), 0.55, DATEADD(HOUR, -2, @now), DATEADD(HOUR, -1, @now), DATEADD(HOUR, -24, @now), DATEADD(HOUR, -1, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-007')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-007', @wr7, @enterpriseId, NULL, 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -2, @now), DATEADD(HOUR, -2, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-008')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-008', @wr8, @enterpriseId, NULL, 'ACCEPTED_ENTERPRISE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -9, @now), DATEADD(HOUR, -8, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-009')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-009', @wr9, @enterpriseId, @collectorId1, 'ACCEPTED_COLLECTOR', NULL, DATEADD(HOUR, -6, @now), DATEADD(HOUR, -6, @now), NULL, NULL, NULL, NULL, DATEADD(HOUR, -7, @now), DATEADD(HOUR, -6, @now));
END

IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'CR-DEMO-010')
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-010', @wr10, @enterpriseId, NULL, 'REJECTED', N'Ảnh không rõ hoặc sai loại rác', NULL, NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -20, @now), DATEADD(HOUR, -19, @now));
END

DECLARE @crCompletedId INT;
SELECT @crCompletedId = id FROM collection_requests WHERE request_code = 'CR-DEMO-006';

DECLARE @collectorReportId INT;
SELECT @collectorReportId = id FROM collector_reports WHERE report_code = 'CRPT-DEMO-001';
IF @collectorReportId IS NULL AND @crCompletedId IS NOT NULL
BEGIN
    INSERT INTO collector_reports (report_code, collection_request_id, collector_id, status, collector_note, total_point, actual_weight_recyclable, collected_at, latitude, longitude, created_at)
    VALUES ('CRPT-DEMO-001', @crCompletedId, @collectorId4, 'COMPLETED', N'Hoàn tất thu gom demo', 25000, 0.55, DATEADD(HOUR, -1, @now), 10.77752000, 106.70188000, DATEADD(HOUR, -1, @now));
    SET @collectorReportId = SCOPE_IDENTITY();
END

IF @collectorReportId IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM collector_report_images WHERE collector_report_id = @collectorReportId)
    BEGIN
        INSERT INTO collector_report_images (collector_report_id, image_url, image_public_id, created_at)
        VALUES
        (@collectorReportId, 'https://example.com/collector_reports/1.jpg', 'demo_img_1', DATEADD(HOUR, -1, @now)),
        (@collectorReportId, 'https://example.com/collector_reports/2.jpg', 'demo_img_2', DATEADD(HOUR, -1, @now));
    END

    DECLARE @catPaperId INT, @catCanId INT;
    SELECT @catPaperId = id FROM waste_categories WHERE name = N'Giấy';
    SELECT @catCanId = id FROM waste_categories WHERE name = N'Lon nhôm';

    IF @catPaperId IS NOT NULL AND @catCanId IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM collector_report_items WHERE collector_report_id = @collectorReportId)
        BEGIN
            INSERT INTO collector_report_items (collector_report_id, waste_category_id, quantity, unit_snapshot, point_per_unit_snapshot, total_point, created_at)
            VALUES
            (@collectorReportId, @catPaperId, 0.35, 'KG', 2250.0000, 788, DATEADD(HOUR, -1, @now)),
            (@collectorReportId, @catCanId, 5, 'CAN', 180.0000, 900, DATEADD(HOUR, -1, @now));
        END
    END
END

SELECT
    @enterpriseId AS enterpriseId,
    @collectorId1 AS collectorId1,
    @collectorId2 AS collectorId2,
    @collectorId3 AS collectorId3,
    @collectorId4 AS collectorId4;
GO
