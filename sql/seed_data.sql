SET NOCOUNT ON;

DECLARE @now DATETIME2 = SYSDATETIME();

DECLARE @roleAdminId INT;
DECLARE @roleCitizenId INT;
DECLARE @roleCollectorId INT;
DECLARE @roleEnterpriseId INT;
DECLARE @roleEnterpriseAdminId INT;

SELECT @roleAdminId = id FROM roles WHERE role_code = 'ADMIN';
IF @roleAdminId IS NULL
BEGIN
    INSERT INTO roles (role_code, role_name, description, is_active, created_at)
    VALUES ('ADMIN', 'Administrator', N'System Administrator', 1, @now);
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

SELECT @roleEnterpriseAdminId = id FROM roles WHERE role_code = 'ENTERPRISE_ADMIN';
IF @roleEnterpriseAdminId IS NULL
BEGIN
    INSERT INTO roles (role_code, role_name, description, is_active, created_at)
    VALUES ('ENTERPRISE_ADMIN', 'Enterprise Administrator', N'Enterprise Administrator', 1, @now);
    SET @roleEnterpriseAdminId = SCOPE_IDENTITY();
END

DECLARE @permCreateReportId INT;
DECLARE @permViewOwnReportsId INT;
DECLARE @permViewAreaReportsId INT;
DECLARE @permAssignCollectorId INT;
DECLARE @permViewAssignedTasksId INT;
DECLARE @permUpdateTaskStatusId INT;

SELECT @permCreateReportId = id FROM permissions WHERE permission_code = 'CREATE_REPORT';
IF @permCreateReportId IS NULL
BEGIN
    INSERT INTO permissions (permission_code, permission_name, module, description)
    VALUES ('CREATE_REPORT', N'Create waste report', 'CITIZEN', N'Create a new waste report');
    SET @permCreateReportId = SCOPE_IDENTITY();
END

SELECT @permViewOwnReportsId = id FROM permissions WHERE permission_code = 'VIEW_OWN_REPORTS';
IF @permViewOwnReportsId IS NULL
BEGIN
    INSERT INTO permissions (permission_code, permission_name, module, description)
    VALUES ('VIEW_OWN_REPORTS', N'View own waste reports', 'CITIZEN', N'View waste reports created by the citizen');
    SET @permViewOwnReportsId = SCOPE_IDENTITY();
END

SELECT @permViewAreaReportsId = id FROM permissions WHERE permission_code = 'VIEW_AREA_REPORTS';
IF @permViewAreaReportsId IS NULL
BEGIN
    INSERT INTO permissions (permission_code, permission_name, module, description)
    VALUES ('VIEW_AREA_REPORTS', N'View reports in assigned area', 'ENTERPRISE', N'View waste reports in enterprise coverage area');
    SET @permViewAreaReportsId = SCOPE_IDENTITY();
END

SELECT @permAssignCollectorId = id FROM permissions WHERE permission_code = 'ASSIGN_COLLECTOR';
IF @permAssignCollectorId IS NULL
BEGIN
    INSERT INTO permissions (permission_code, permission_name, module, description)
    VALUES ('ASSIGN_COLLECTOR', N'Assign collector to report', 'ENTERPRISE', N'Assign a collector to a collection request');
    SET @permAssignCollectorId = SCOPE_IDENTITY();
END

SELECT @permViewAssignedTasksId = id FROM permissions WHERE permission_code = 'VIEW_ASSIGNED_TASKS';
IF @permViewAssignedTasksId IS NULL
BEGIN
    INSERT INTO permissions (permission_code, permission_name, module, description)
    VALUES ('VIEW_ASSIGNED_TASKS', N'View assigned collection tasks', 'COLLECTOR', N'View tasks assigned to the collector');
    SET @permViewAssignedTasksId = SCOPE_IDENTITY();
END

SELECT @permUpdateTaskStatusId = id FROM permissions WHERE permission_code = 'UPDATE_TASK_STATUS';
IF @permUpdateTaskStatusId IS NULL
BEGIN
    INSERT INTO permissions (permission_code, permission_name, module, description)
    VALUES ('UPDATE_TASK_STATUS', N'Update task collection status', 'COLLECTOR', N'Update collection request status by collector');
    SET @permUpdateTaskStatusId = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = @roleCitizenId AND permission_id = @permCreateReportId)
    INSERT INTO role_permissions (role_id, permission_id) VALUES (@roleCitizenId, @permCreateReportId);
IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = @roleCitizenId AND permission_id = @permViewOwnReportsId)
    INSERT INTO role_permissions (role_id, permission_id) VALUES (@roleCitizenId, @permViewOwnReportsId);
IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = @roleEnterpriseId AND permission_id = @permViewAreaReportsId)
    INSERT INTO role_permissions (role_id, permission_id) VALUES (@roleEnterpriseId, @permViewAreaReportsId);
IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = @roleEnterpriseAdminId AND permission_id = @permViewAreaReportsId)
    INSERT INTO role_permissions (role_id, permission_id) VALUES (@roleEnterpriseAdminId, @permViewAreaReportsId);
IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = @roleEnterpriseAdminId AND permission_id = @permAssignCollectorId)
    INSERT INTO role_permissions (role_id, permission_id) VALUES (@roleEnterpriseAdminId, @permAssignCollectorId);
IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = @roleCollectorId AND permission_id = @permViewAssignedTasksId)
    INSERT INTO role_permissions (role_id, permission_id) VALUES (@roleCollectorId, @permViewAssignedTasksId);
IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = @roleCollectorId AND permission_id = @permUpdateTaskStatusId)
    INSERT INTO role_permissions (role_id, permission_id) VALUES (@roleCollectorId, @permUpdateTaskStatusId);

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
    VALUES ('enterprise@demo.com', @bcrypt, N'Enterprise Demo', '0908000000', NULL, @roleEnterpriseAdminId, @enterpriseId, 'active', NULL, @now, @now);
    SET @enterpriseUserId = SCOPE_IDENTITY();
END
ELSE
BEGIN
    UPDATE users
    SET enterprise_id = @enterpriseId, role_id = @roleEnterpriseAdminId
    WHERE id = @enterpriseUserId AND (enterprise_id IS NULL OR enterprise_id <> @enterpriseId OR role_id <> @roleEnterpriseAdminId);
END

DECLARE @collectorUserId1 INT, @collectorUserId2 INT;
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

DECLARE @citizenUserId1 INT, @citizenUserId2 INT;
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

DECLARE @citizenId1 INT, @citizenId2 INT;
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

DECLARE @collectorId1 INT, @collectorId2 INT;
SELECT @collectorId1 = id FROM collectors WHERE user_id = @collectorUserId1;
IF @collectorId1 IS NULL
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
    VALUES (@collectorUserId1, @enterpriseId, 'collector1@demo.com', N'Collector 01', 'EMP-001', N'Xe tải nhỏ', '59C-10001', 'AVAILABLE', @now, 0, 0, 0.00, @now);
    SET @collectorId1 = SCOPE_IDENTITY();
END

SELECT @collectorId2 = id FROM collectors WHERE user_id = @collectorUserId2;
IF @collectorId2 IS NULL
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
    VALUES (@collectorUserId2, @enterpriseId, 'collector2@demo.com', N'Collector 02', 'EMP-002', N'Xe ba gác', '59C-10002', 'AVAILABLE', @now, 0, 0, 0.00, @now);
    SET @collectorId2 = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Giấy')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Giấy', NULL, 'KG', 2250.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Báo')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Báo', NULL, 'KG', 3600.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Giấy, hồ sơ')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Giấy, hồ sơ', NULL, 'KG', 3150.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Giấy tập')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Giấy tập', NULL, 'KG', 3600.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Lon bia')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Lon bia', NULL, 'CAN', 180.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Sắt')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Sắt', NULL, 'KG', 3600.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Sắt lon')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Sắt lon', NULL, 'KG', 1440.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Inox')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Inox', NULL, 'KG', 5400.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Đồng')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Đồng', NULL, 'KG', 67500.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Nhôm')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Nhôm', NULL, 'KG', 16200.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Chai thủy tinh')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Chai thủy tinh', NULL, 'BOTTLE', 450.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Bao bì, hỗn hợp')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Bao bì, hỗn hợp', NULL, 'KG', 1600.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Meca')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Meca', NULL, 'KG', 450.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Mủ')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Mủ', NULL, 'KG', 3600.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Mủ bình')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Mủ bình', NULL, 'KG', 4500.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Mủ tôn')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Mủ tôn', NULL, 'KG', 1800.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Mủ đen')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Mủ đen', NULL, 'KG', 150.0000, @now, @now);

IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Lon nhôm')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Lon nhôm', NULL, 'CAN', 180.0000, @now, @now);
IF NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = N'Chai nhựa PET')
    INSERT INTO waste_categories (name, description, unit, point_per_unit, created_at, updated_at)
    VALUES (N'Chai nhựa PET', NULL, 'BOTTLE', 120.0000, @now, @now);

DECLARE @catPaperId INT, @catCanId INT, @catPetId INT, @catCopperId INT;
SELECT @catPaperId = id FROM waste_categories WHERE name = N'Giấy';
SELECT @catCanId = id FROM waste_categories WHERE name = N'Lon nhôm';
SELECT @catPetId = id FROM waste_categories WHERE name = N'Chai nhựa PET';
SELECT @catCopperId = id FROM waste_categories WHERE name = N'Đồng';

DECLARE @wr1 INT, @wr2 INT;
IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-001')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, description, waste_type, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-001', @citizenId1, N'Báo cáo rác tái chế #001', 'RECYCLABLE', 1.20, 10.77653000, 106.70098000, N'Quận 1 - Điểm 001', N'https://example.com/wr/1.jpg', NULL, 'PENDING', DATEADD(HOUR, -6, @now), DATEADD(HOUR, -6, @now));
    SET @wr1 = SCOPE_IDENTITY();
END
ELSE SELECT @wr1 = id FROM waste_reports WHERE report_code = 'WR-DEMO-001';

IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE report_code = 'WR-DEMO-002')
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, description, waste_type, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-DEMO-002', @citizenId2, N'Báo cáo rác tái chế #002', 'RECYCLABLE', 2.50, 10.77664000, 106.70108000, N'Quận 1 - Điểm 002', N'https://example.com/wr/2.jpg', NULL, 'ASSIGNED', DATEADD(HOUR, -10, @now), DATEADD(HOUR, -8, @now));
    SET @wr2 = SCOPE_IDENTITY();
END
ELSE SELECT @wr2 = id FROM waste_reports WHERE report_code = 'WR-DEMO-002';

IF @wr1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM waste_report_items WHERE report_id = @wr1)
BEGIN
    INSERT INTO waste_report_items (report_id, waste_category_id, quantity, unit_snapshot, created_at)
    VALUES
    (@wr1, @catPaperId, 0.70, 'KG', DATEADD(HOUR, -6, @now)),
    (@wr1, @catCanId, 2, 'CAN', DATEADD(HOUR, -6, @now));
END

IF @wr2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM waste_report_items WHERE report_id = @wr2)
BEGIN
    INSERT INTO waste_report_items (report_id, waste_category_id, quantity, unit_snapshot, created_at)
    VALUES
    (@wr2, @catPetId, 4, 'BOTTLE', DATEADD(HOUR, -10, @now)),
    (@wr2, @catCopperId, 0.25, 'KG', DATEADD(HOUR, -10, @now));
END

IF NOT EXISTS (SELECT 1 FROM report_images WHERE report_id = @wr1 AND image_url = 'https://example.com/wr/1.jpg')
    INSERT INTO report_images (report_id, image_url, image_type, uploaded_at) VALUES (@wr1, 'https://example.com/wr/1.jpg', 'BEFORE', DATEADD(HOUR, -6, @now));
IF NOT EXISTS (SELECT 1 FROM report_images WHERE report_id = @wr2 AND image_url = 'https://example.com/wr/2.jpg')
    INSERT INTO report_images (report_id, image_url, image_type, uploaded_at) VALUES (@wr2, 'https://example.com/wr/2.jpg', 'BEFORE', DATEADD(HOUR, -10, @now));

DECLARE @cr1 INT, @cr2 INT;
SELECT @cr1 = id FROM collection_requests WHERE request_code = 'CR-DEMO-001';
IF @cr1 IS NULL
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-001', @wr1, @enterpriseId, NULL, 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -6, @now), DATEADD(HOUR, -6, @now));
    SET @cr1 = SCOPE_IDENTITY();
END

SELECT @cr2 = id FROM collection_requests WHERE request_code = 'CR-DEMO-002';
IF @cr2 IS NULL
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-DEMO-002', @wr2, @enterpriseId, @collectorId1, 'ASSIGNED', NULL, DATEADD(HOUR, -9, @now), NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -10, @now), DATEADD(HOUR, -9, @now));
    SET @cr2 = SCOPE_IDENTITY();
END

IF @cr2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM collection_tracking WHERE collection_request_id = @cr2 AND action = 'assigned')
BEGIN
    INSERT INTO collection_tracking (collection_request_id, collector_id, action, latitude, longitude, note, images, created_at)
    VALUES (@cr2, @collectorId1, 'assigned', 10.77664000, 106.70108000, N'Demo assigned', NULL, DATEADD(HOUR, -9, @now));
END

DECLARE @collectorReportId INT;
SELECT @collectorReportId = id FROM collector_reports WHERE report_code = 'CRPT-DEMO-001';
IF @collectorReportId IS NULL AND @cr2 IS NOT NULL
BEGIN
    INSERT INTO collector_reports (report_code, collection_request_id, collector_id, status, collector_note, total_point, actual_weight_recyclable, collected_at, latitude, longitude, created_at)
    VALUES ('CRPT-DEMO-001', @cr2, @collectorId1, 'COMPLETED', N'Hoàn tất thu gom demo', 5000, 0.95, DATEADD(HOUR, -1, @now), 10.77664000, 106.70108000, DATEADD(HOUR, -1, @now));
    SET @collectorReportId = SCOPE_IDENTITY();
END

IF @collectorReportId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM collector_report_items WHERE collector_report_id = @collectorReportId)
BEGIN
    INSERT INTO collector_report_items (collector_report_id, waste_category_id, quantity, unit_snapshot, point_per_unit_snapshot, total_point, created_at)
    VALUES
    (@collectorReportId, @catPaperId, 0.70, 'KG', 2250.0000, 1575, DATEADD(HOUR, -1, @now)),
    (@collectorReportId, @catCanId, 2, 'CAN', 180.0000, 360, DATEADD(HOUR, -1, @now));
END

IF @cr2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_transactions WHERE citizen_id = @citizenId2 AND collection_request_id = @cr2 AND transaction_type = 'EARN')
BEGIN
    INSERT INTO point_transactions (citizen_id, report_id, collection_request_id, points, transaction_type, description, balance_after, created_by, created_at)
    VALUES (@citizenId2, @wr2, @cr2, 5000, 'EARN', N'Điểm thưởng thu gom demo', 5000, @adminUserId, DATEADD(HOUR, -1, @now));
END

IF NOT EXISTS (SELECT 1 FROM feedbacks WHERE feedback_code = 'FB-DEMO-001')
BEGIN
    INSERT INTO feedbacks (feedback_code, citizen_id, collection_request_id, feedback_type, subject, content, images, severity, status, assigned_to, assigned_at, resolution, resolved_by, resolved_at, responses, created_at, updated_at)
    VALUES ('FB-DEMO-001', @citizenId1, @cr2, 'SERVICE', N'Chậm trễ thu gom', N'Yêu cầu đã được gán nhưng chưa thấy cập nhật', NULL, 'MEDIUM', 'OPEN', @enterpriseUserId, DATEADD(HOUR, -2, @now), NULL, NULL, NULL, NULL, DATEADD(HOUR, -2, @now), DATEADD(HOUR, -2, @now));
END

IF NOT EXISTS (SELECT 1 FROM leaderboard WHERE citizen_id = @citizenId2 AND period_type = 'MONTHLY' AND period_start = CONVERT(date, DATEADD(DAY, 1 - DAY(@now), @now)))
BEGIN
    INSERT INTO leaderboard (citizen_id, ward, city, period_type, period_start, period_end, total_points, total_reports, valid_reports, total_weight_kg, rank_position, updated_at)
    VALUES
    (@citizenId2, N'Bến Nghé', N'HCM', 'MONTHLY', CONVERT(date, DATEADD(DAY, 1 - DAY(@now), @now)), CONVERT(date, EOMONTH(@now)), 5000, 1, 1, 0.95, 1, @now);
END

IF NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'APP_NAME')
BEGIN
    INSERT INTO system_settings (setting_key, setting_value, data_type, category, description, updated_by, updated_at)
    VALUES ('APP_NAME', N'Crowdsourced Waste Collection & Recycling System', 'TEXT', 'GENERAL', N'Application display name', @adminUserId, @now);
END

IF NOT EXISTS (SELECT 1 FROM invalidated_tokens WHERE id = 'demo-token-1')
BEGIN
    INSERT INTO invalidated_tokens (id, expiry_time) VALUES ('demo-token-1', DATEADD(DAY, 1, @now));
END

SELECT
    @enterpriseId AS enterpriseId,
    @adminUserId AS adminUserId,
    @enterpriseUserId AS enterpriseUserId,
    @collectorId1 AS collectorId1,
    @citizenId1 AS citizenId1,
    @citizenId2 AS citizenId2,
    @wr1 AS reportId1,
    @wr2 AS reportId2;
