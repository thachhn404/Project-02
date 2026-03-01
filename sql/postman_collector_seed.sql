DECLARE @now DATETIME2 = SYSDATETIME();

DECLARE @roleCitizenId INT;
DECLARE @roleCollectorId INT;
DECLARE @roleEnterpriseId INT;

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
SELECT @enterpriseId = id FROM enterprise WHERE email = 'enterprise@test.com';
IF @enterpriseId IS NULL
BEGIN
    INSERT INTO enterprise (name, address, ward, city, phone, email, license_number, tax_code, capacity_kg_per_day, supported_waste_type_codes, service_wards, service_cities, status, total_collected_weight, created_at, updated_at)
    VALUES (N'Test Enterprise', N'1 Test Street', N'Ward 1', N'HCM', '0900000000', 'enterprise@test.com', 'LIC-TEST-001', 'TAX-TEST-001', 5000.00, N'RECYCLABLE', N'Ward 1', N'HCM', 'active', 0.00, @now, @now);
    SET @enterpriseId = SCOPE_IDENTITY();
END

DECLARE @collectorUserId INT;
SELECT @collectorUserId = id FROM users WHERE email = 'collector@test.com';
IF @collectorUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES (
        'collector@test.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoO5rEo/pZ5lHppZArYrusS4x2ma/p3d.',
        N'Test Collector',
        '0901111111',
        NULL,
        @roleCollectorId,
        NULL,
        'active',
        NULL,
        @now,
        @now
    );
    SET @collectorUserId = SCOPE_IDENTITY();
END

DECLARE @citizenUserId INT;
SELECT @citizenUserId = id FROM users WHERE email = 'citizen@test.com';
IF @citizenUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES (
        'citizen@test.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoO5rEo/pZ5lHppZArYrusS4x2ma/p3d.',
        N'Test Citizen',
        '0902222222',
        NULL,
        @roleCitizenId,
        NULL,
        'active',
        NULL,
        @now,
        @now
    );
    SET @citizenUserId = SCOPE_IDENTITY();
END

DECLARE @enterpriseUserId INT;
SELECT @enterpriseUserId = id FROM users WHERE email = 'enterprise@test.com';
IF @enterpriseUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at)
    VALUES (
        'enterprise@test.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoO5rEo/pZ5lHppZArYrusS4x2ma/p3d.',
        N'Test Enterprise',
        '0903333333',
        NULL,
        @roleEnterpriseId,
        @enterpriseId,
        'active',
        NULL,
        @now,
        @now
    );
    SET @enterpriseUserId = SCOPE_IDENTITY();
END
ELSE
BEGIN
    UPDATE users
    SET enterprise_id = @enterpriseId
    WHERE id = @enterpriseUserId AND (enterprise_id IS NULL OR enterprise_id <> @enterpriseId);
END

DECLARE @citizenId INT;
SELECT @citizenId = id FROM citizens WHERE user_id = @citizenUserId;
IF @citizenId IS NULL
BEGIN
    INSERT INTO citizens (user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports)
    VALUES (@citizenUserId, 'citizen@test.com', N'Test Citizen', NULL, N'10 Test Address', '0902222222', N'Ward 1', N'HCM', 0, 0, 0);
    SET @citizenId = SCOPE_IDENTITY();
END

DECLARE @collectorId INT;
SELECT @collectorId = id FROM collectors WHERE user_id = @collectorUserId;
IF @collectorId IS NULL
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, last_location_update, total_collections, successful_collections, total_weight_collected, created_at)
    VALUES (@collectorUserId, @enterpriseId, 'collector@test.com', N'Test Collector', 'EMP-TEST-001', N'Xe tải nhỏ', '59C-00001', 'AVAILABLE', @now, 0, 0, 0.00, @now);
    SET @collectorId = SCOPE_IDENTITY();
END

DECLARE @wtRecyclableId INT;
SELECT @wtRecyclableId = id FROM waste_types WHERE code = 'RECYCLABLE';
IF @wtRecyclableId IS NULL
BEGIN
    INSERT INTO waste_types (code, name, base_points, sla_hours, is_recyclable, created_at)
    VALUES ('RECYCLABLE', N'Recyclable Waste', 20, 24, 1, @now);
    SET @wtRecyclableId = SCOPE_IDENTITY();
END

DECLARE @catPaperId INT;
SELECT @catPaperId = id FROM waste_categories WHERE name = N'Giấy';
IF @catPaperId IS NULL
BEGIN
    INSERT INTO waste_categories (name, description, unit, point_per_unit, waste_type_id, created_at, updated_at)
    VALUES (N'Giấy', N'Giấy vụn, bìa carton', 'KG', 2250.0000, @wtRecyclableId, @now, @now);
    SET @catPaperId = SCOPE_IDENTITY();
END

DECLARE @catCanId INT;
SELECT @catCanId = id FROM waste_categories WHERE name = N'Lon nhôm';
IF @catCanId IS NULL
BEGIN
    INSERT INTO waste_categories (name, description, unit, point_per_unit, waste_type_id, created_at, updated_at)
    VALUES (N'Lon nhôm', N'Lon nước ngọt/lon bia', 'CAN', 180.0000, @wtRecyclableId, @now, @now);
    SET @catCanId = SCOPE_IDENTITY();
END

DECLARE @reportAssignedId INT;
SELECT @reportAssignedId = id FROM waste_reports WHERE report_code = 'WR-PM-ASSIGNED';
IF @reportAssignedId IS NULL
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-PM-ASSIGNED', @citizenId, @wtRecyclableId, N'Postman seed report (assigned)', 1.00, 10.77653000, 106.70098000, N'PM address assigned', N'https://example.com/reports/WR-PM-ASSIGNED.jpg', NULL, 'PENDING', DATEADD(HOUR, -2, @now), DATEADD(HOUR, -2, @now));
    SET @reportAssignedId = SCOPE_IDENTITY();
END

DECLARE @reportAcceptedId INT;
SELECT @reportAcceptedId = id FROM waste_reports WHERE report_code = 'WR-PM-ACCEPTED';
IF @reportAcceptedId IS NULL
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-PM-ACCEPTED', @citizenId, @wtRecyclableId, N'Postman seed report (accepted_collector)', 1.10, 10.77653100, 106.70098100, N'PM address accepted', N'https://example.com/reports/WR-PM-ACCEPTED.jpg', NULL, 'PENDING', DATEADD(HOUR, -3, @now), DATEADD(HOUR, -3, @now));
    SET @reportAcceptedId = SCOPE_IDENTITY();
END

DECLARE @reportOnWayId INT;
SELECT @reportOnWayId = id FROM waste_reports WHERE report_code = 'WR-PM-ONWAY';
IF @reportOnWayId IS NULL
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-PM-ONWAY', @citizenId, @wtRecyclableId, N'Postman seed report (on_the_way)', 2.50, 10.77653200, 106.70098200, N'PM address onway', N'https://example.com/reports/WR-PM-ONWAY.jpg', NULL, 'PENDING', DATEADD(HOUR, -4, @now), DATEADD(HOUR, -4, @now));
    SET @reportOnWayId = SCOPE_IDENTITY();
END

DECLARE @reportCollectedId INT;
SELECT @reportCollectedId = id FROM waste_reports WHERE report_code = 'WR-PM-COLLECTED';
IF @reportCollectedId IS NULL
BEGIN
    INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight, latitude, longitude, address, images, cloudinary_public_id, status, created_at, updated_at)
    VALUES ('WR-PM-COLLECTED', @citizenId, @wtRecyclableId, N'Postman seed report (collected)', 3.00, 10.77653300, 106.70098300, N'PM address collected', N'https://example.com/reports/WR-PM-COLLECTED.jpg', NULL, 'PENDING', DATEADD(HOUR, -6, @now), DATEADD(HOUR, -6, @now));
    SET @reportCollectedId = SCOPE_IDENTITY();
END

DECLARE @crAssignedId INT;
SELECT @crAssignedId = id FROM collection_requests WHERE request_code = 'CR-PM-001';
IF @crAssignedId IS NULL
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-PM-001', @reportAssignedId, @enterpriseId, @collectorId, 'assigned', NULL, DATEADD(MINUTE, -30, @now), NULL, NULL, NULL, NULL, NULL, DATEADD(HOUR, -2, @now), DATEADD(MINUTE, -30, @now));
    SET @crAssignedId = SCOPE_IDENTITY();
END

DECLARE @crAcceptedId INT;
SELECT @crAcceptedId = id FROM collection_requests WHERE request_code = 'CR-PM-002';
IF @crAcceptedId IS NULL
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-PM-002', @reportAcceptedId, @enterpriseId, @collectorId, 'accepted_collector', NULL, DATEADD(HOUR, -3, @now), DATEADD(MINUTE, -20, @now), NULL, NULL, NULL, NULL, DATEADD(HOUR, -3, @now), DATEADD(MINUTE, -20, @now));
    SET @crAcceptedId = SCOPE_IDENTITY();
END

DECLARE @crOnWayId INT;
SELECT @crOnWayId = id FROM collection_requests WHERE request_code = 'CR-PM-003';
IF @crOnWayId IS NULL
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-PM-003', @reportOnWayId, @enterpriseId, @collectorId, 'on_the_way', NULL, DATEADD(HOUR, -4, @now), DATEADD(HOUR, -3, @now), DATEADD(MINUTE, -45, @now), NULL, NULL, NULL, DATEADD(HOUR, -4, @now), DATEADD(MINUTE, -45, @now));
    SET @crOnWayId = SCOPE_IDENTITY();
END

DECLARE @crCollectedId INT;
SELECT @crCollectedId = id FROM collection_requests WHERE request_code = 'CR-PM-004';
IF @crCollectedId IS NULL
BEGIN
    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, rejection_reason, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, completed_at, created_at, updated_at)
    VALUES ('CR-PM-004', @reportCollectedId, @enterpriseId, @collectorId, 'collected', NULL, DATEADD(HOUR, -6, @now), DATEADD(HOUR, -5, @now), DATEADD(HOUR, -4, @now), 4.25, DATEADD(MINUTE, -10, @now), NULL, DATEADD(HOUR, -6, @now), DATEADD(MINUTE, -10, @now));
    SET @crCollectedId = SCOPE_IDENTITY();
END

SELECT
    @collectorId AS collectorId,
    @enterpriseId AS enterpriseId,
    @citizenId AS citizenId,
    @catPaperId AS categoryPaperId,
    @catCanId AS categoryCanId,
    @crAssignedId AS requestAssignedId,
    @crAcceptedId AS requestAcceptedId,
    @crOnWayId AS requestOnTheWayId,
    @crCollectedId AS requestCollectedId;
