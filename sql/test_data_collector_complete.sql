-- ============================================================================
-- COMPLETE TEST DATA FOR COLLECTOR WORKFLOW
-- Bao gồm collectors và tất cả các bảng liên quan
-- ============================================================================

-- ============================================================================
-- PREREQUISITE 1: USERS
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 101)
BEGIN
    SET IDENTITY_INSERT users ON;
    INSERT INTO users (id, username, password, email, role, status, created_at, updated_at)
    VALUES
    (101, 'collector1_user', '$2a$10$dummyHashedPassword1', 'collector1@example.com', 'COLLECTOR', 'ACTIVE', '2024-01-15 08:00:00', '2024-01-15 08:00:00'),
    (102, 'collector2_user', '$2a$10$dummyHashedPassword2', 'collector2@example.com', 'COLLECTOR', 'ACTIVE', '2024-03-20 09:00:00', '2024-03-20 09:00:00'),
    (103, 'collector3_user', '$2a$10$dummyHashedPassword3', 'collector3@example.com', 'COLLECTOR', 'INACTIVE', '2023-11-01 10:00:00', '2023-11-01 10:00:00'),
    (104, 'collector4_user', '$2a$10$dummyHashedPassword4', 'collector4@example.com', 'COLLECTOR', 'ACTIVE', '2024-06-10 09:00:00', '2024-06-10 09:00:00');
    SET IDENTITY_INSERT users OFF;
END
GO

-- ============================================================================
-- PREREQUISITE 2: ENTERPRISES
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM enterprises WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT enterprises ON;
    INSERT INTO enterprises (id, user_id, company_name, tax_code, email, phone, address, status, created_at)
    VALUES
    (1, 1, N'Công ty Thu Gom Rác ABC', '0123456789', 'enterprise1@example.com', '0901234567', N'123 Đường ABC, Quận 1, TP.HCM', 'active', '2023-01-01 10:00:00');
    SET IDENTITY_INSERT enterprises OFF;
END
GO

-- ============================================================================
-- PREREQUISITE 3: CITIZENS (để tạo waste_reports)
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM citizens WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT citizens ON;
    INSERT INTO citizens (id, user_id, full_name, phone, address, created_at, updated_at)
    VALUES
    (1, 2, N'Nguyễn Văn Dân', '0912345678', N'456 Đường XYZ, Quận 1, TP.HCM', '2024-01-01 10:00:00', '2024-01-01 10:00:00');
    SET IDENTITY_INSERT citizens OFF;
END
GO

-- ============================================================================
-- PREREQUISITE 4: WASTE_TYPES
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM waste_types WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT waste_types ON;
    INSERT INTO waste_types (id, code, name, description, point_per_kg, created_at, updated_at)
    VALUES
    (1, 'RECYCLABLE', N'Rác tái chế', N'Giấy, nhựa, kim loại có thể tái chế', 10.00, '2023-01-01 00:00:00', '2023-01-01 00:00:00'),
    (2, 'ORGANIC', N'Rác hữu cơ', N'Rác thực phẩm, lá cây', 5.00, '2023-01-01 00:00:00', '2023-01-01 00:00:00'),
    (3, 'ELECTRONIC', N'Rác điện tử', N'Thiết bị điện tử cũ', 15.00, '2023-01-01 00:00:00', '2023-01-01 00:00:00');
    SET IDENTITY_INSERT waste_types OFF;
END
GO

-- ============================================================================
-- PREREQUISITE 5: WASTE_REPORTS
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM waste_reports WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT waste_reports ON;
    INSERT INTO waste_reports (id, report_code, citizen_id, waste_type_id, latitude, longitude, address, description, images, status, created_at, updated_at)
    VALUES
    (1, 'WR-20260205-001', 1, 1, 10.77580000, 106.70550000, N'123 Nguyễn Huệ, Q1', N'Rác tái chế khoảng 10kg', '["https://example.com/waste1.jpg"]', 'ASSIGNED', '2026-02-05 05:50:00', '2026-02-05 06:00:00'),
    (2, 'WR-20260205-002', 1, 1, 10.77600000, 106.70600000, N'456 Lê Lợi, Q1', N'Rác giấy 8kg', '["https://example.com/waste2.jpg"]', 'ACCEPTED_COLLECTOR', '2026-02-05 06:20:00', '2026-02-05 06:45:00'),
    (3, 'WR-20260205-003', 1, 2, 10.77650000, 106.70650000, N'789 Trần Hưng Đạo, Q1', N'Rác hữu cơ 15kg', '["https://example.com/waste3.jpg"]', 'ON_THE_WAY', '2026-02-05 06:50:00', '2026-02-05 07:20:00'),
    (4, 'WR-20260204-001', 1, 2, 10.77700000, 106.70700000, N'321 Hai Bà Trưng, Q1', N'Rác sinh hoạt 15.5kg', '["https://example.com/waste4.jpg"]', 'COLLECTED', '2026-02-04 07:50:00', '2026-02-04 09:00:00'),
    (5, 'WR-20260203-001', 1, 1, 10.77800000, 106.70800000, N'654 Pasteur, Q1', N'Rác tái chế 22.3kg', '["https://example.com/waste5.jpg"]', 'COLLECTED', '2026-02-03 09:50:00', '2026-02-03 11:15:00'),
    (6, 'WR-20260205-004', 1, 1, 10.77900000, 106.70900000, N'987 Võ Văn Tần, Q3', N'Rác nhựa 5kg', '["https://example.com/waste6.jpg"]', 'ASSIGNED', '2026-02-05 06:55:00', '2026-02-05 07:00:00'),
    (7, 'WR-20260205-005', 1, 3, 10.78000000, 106.71000000, N'111 Cách Mạng Tháng 8, Q3', N'Rác điện tử 3kg', '["https://example.com/waste7.jpg"]', 'ACCEPTED_ENTERPRISE', '2026-02-05 07:10:00', '2026-02-05 07:15:00'),
    (8, 'WR-20260205-006', 1, 2, 10.78100000, 106.71100000, N'222 Điện Biên Phủ, Q3', N'Rác hữu cơ 12kg', '["https://example.com/waste8.jpg"]', 'PENDING', '2026-02-05 07:30:00', '2026-02-05 07:30:00');
    SET IDENTITY_INSERT waste_reports OFF;
END
GO

-- ============================================================================
-- 1. COLLECTORS TABLE
-- 4 collectors với 4 status: AVAILABLE, ACTIVE, INACTIVE, SUSPEND
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM collectors WHERE user_id = 101)
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, 
                           vehicle_type, vehicle_plate, status, 
                           last_location_update,
                           total_collections, successful_collections, total_weight_collected, created_at)
    VALUES
    (101, 1, 'collector1@example.com', N'Nguyễn Văn A', 'COL-001', 
     'TRUCK', '29A-12345', 'AVAILABLE',
     '2026-02-05 07:00:00',
     50, 45, 1250.50, '2024-01-15 08:00:00'),
    
    (102, 1, 'collector2@example.com', N'Trần Thị B', 'COL-002',
     'MOTORCYCLE', '29B-98765', 'ACTIVE',
     '2026-02-05 07:15:00',
     15, 12, 320.75, '2024-03-20 09:00:00'),
    
    (103, 1, 'collector3@example.com', N'Lê Văn C', 'COL-003',
     'TRUCK', '29C-54321', 'INACTIVE',
     '2026-02-04 18:00:00',
     100, 95, 2500.00, '2023-11-01 10:00:00'),
    
    (104, 1, 'collector4@example.com', N'Phạm Thị D', 'COL-004',
     'MOTORCYCLE', '29D-11111', 'SUSPEND',
     '2026-02-03 16:00:00',
     30, 25, 680.00, '2024-06-10 09:00:00');
END
GO

-- ============================================================================
-- 2. COLLECTION_REQUESTS TABLE
-- Liên kết waste_reports với collectors
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM collection_requests WHERE request_code = 'REQ-20260205-001')
BEGIN
    DECLARE @col1 INT = (SELECT id FROM collectors WHERE user_id = 101);
    DECLARE @col2 INT = (SELECT id FROM collectors WHERE user_id = 102);

    INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id,
                                     status, rejection_reason, 
                                     assigned_at, accepted_at, started_at, collected_at,
                                     actual_weight_kg, created_at, updated_at)
    VALUES
    -- Task 1: ASSIGNED - collector 1 chưa accept
    ('REQ-20260205-001', 1, 1, @col1, 'ASSIGNED', NULL,
     '2026-02-05 06:00:00', NULL, NULL, NULL, NULL,
     '2026-02-05 05:50:00', '2026-02-05 06:00:00'),

    -- Task 2: ACCEPTED_COLLECTOR - collector 1 đã accept
    ('REQ-20260205-002', 2, 1, @col1, 'ACCEPTED_COLLECTOR', NULL,
     '2026-02-05 06:30:00', '2026-02-05 06:45:00', NULL, NULL, NULL,
     '2026-02-05 06:20:00', '2026-02-05 06:45:00'),

    -- Task 3: ON_THE_WAY - collector 1 đang đi
    ('REQ-20260205-003', 3, 1, @col1, 'ON_THE_WAY', NULL,
     '2026-02-05 07:00:00', '2026-02-05 07:05:00', '2026-02-05 07:20:00', NULL, NULL,
     '2026-02-05 06:50:00', '2026-02-05 07:20:00'),

    -- Task 4: COLLECTED - collector 1 đã hoàn thành
    ('REQ-20260204-001', 4, 1, @col1, 'COLLECTED', NULL,
     '2026-02-04 08:00:00', '2026-02-04 08:15:00', '2026-02-04 08:30:00', '2026-02-04 09:00:00', 15.50,
     '2026-02-04 07:50:00', '2026-02-04 09:00:00'),

    -- Task 5: COLLECTED - collector 1 (ngày trước)
    ('REQ-20260203-001', 5, 1, @col1, 'COLLECTED', NULL,
     '2026-02-03 10:00:00', '2026-02-03 10:10:00', '2026-02-03 10:30:00', '2026-02-03 11:15:00', 22.30,
     '2026-02-03 09:50:00', '2026-02-03 11:15:00'),

    -- Task 6: ASSIGNED - collector 2
    ('REQ-20260205-004', 6, 1, @col2, 'ASSIGNED', NULL,
     '2026-02-05 07:00:00', NULL, NULL, NULL, NULL,
     '2026-02-05 06:55:00', '2026-02-05 07:00:00'),

    -- Task 7: ACCEPTED_ENTERPRISE - chưa gán collector
    ('REQ-20260205-005', 7, 1, NULL, 'ACCEPTED_ENTERPRISE', NULL,
     NULL, NULL, NULL, NULL, NULL,
     '2026-02-05 07:10:00', '2026-02-05 07:15:00'),

    -- Task 8: PENDING
    ('REQ-20260205-006', 8, 1, NULL, 'PENDING', NULL,
     NULL, NULL, NULL, NULL, NULL,
     '2026-02-05 07:30:00', '2026-02-05 07:30:00');
END
GO

-- ============================================================================
-- 3. COLLECTION_TRACKING TABLE
-- Audit logs cho mọi action của collector
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM collection_tracking WHERE collection_request_id = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260205-001'))
BEGIN
    DECLARE @req1 INT = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260205-001');
    DECLARE @req2 INT = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260205-002');
    DECLARE @req3 INT = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260205-003');
    DECLARE @req4 INT = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260204-001');
    DECLARE @req5 INT = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260203-001');
    DECLARE @col1 INT = (SELECT id FROM collectors WHERE user_id = 101);

    INSERT INTO collection_tracking (collection_request_id, collector_id, action, latitude, longitude, note, images, created_at)
    VALUES
    -- Task 1: Assigned
    (@req1, @col1, 'assigned', 10.77580000, 106.70550000, 'Enterprise assigned task', NULL, '2026-02-05 06:00:00'),
    
    -- Task 2: Assigned -> Accepted
    (@req2, @col1, 'assigned', 10.77580000, 106.70550000, 'Enterprise assigned task', NULL, '2026-02-05 06:30:00'),
    (@req2, @col1, 'accepted', 10.77580000, 106.70550000, 'Collector accepted task', NULL, '2026-02-05 06:45:00'),
    
    -- Task 3: Full workflow on_the_way
    (@req3, @col1, 'assigned', 10.77580000, 106.70550000, 'Enterprise assigned task', NULL, '2026-02-05 07:00:00'),
    (@req3, @col1, 'accepted', 10.77580000, 106.70550000, 'Collector accepted task', NULL, '2026-02-05 07:05:00'),
    (@req3, @col1, 'started', 10.77600000, 106.70600000, 'Collector started moving', NULL, '2026-02-05 07:20:00'),
    
    -- Task 4: Completed
    (@req4, @col1, 'assigned', 10.77580000, 106.70550000, 'Enterprise assigned task', NULL, '2026-02-04 08:00:00'),
    (@req4, @col1, 'accepted', 10.77580000, 106.70550000, 'Collector accepted task', NULL, '2026-02-04 08:15:00'),
    (@req4, @col1, 'started', 10.77600000, 106.70600000, 'Collector started moving', NULL, '2026-02-04 08:30:00'),
    (@req4, @col1, 'collected', 10.77800000, 106.70800000, 'Task completed', '["https://example.com/image1.jpg","https://example.com/image2.jpg"]', '2026-02-04 09:00:00'),
    
    -- Task 5: Completed (previous day)
    (@req5, @col1, 'assigned', 10.77580000, 106.70550000, 'Enterprise assigned task', NULL, '2026-02-03 10:00:00'),
    (@req5, @col1, 'accepted', 10.77580000, 106.70550000, 'Collector accepted task', NULL, '2026-02-03 10:10:00'),
    (@req5, @col1, 'started', 10.77600000, 106.70600000, 'Collector started moving', NULL, '2026-02-03 10:30:00'),
    (@req5, @col1, 'collected', 10.77900000, 106.70900000, 'Task completed', '["https://example.com/image3.jpg"]', '2026-02-03 11:15:00');
END
GO

-- ============================================================================
-- 4. COLLECTOR_REPORTS TABLE
-- Reports khi collector hoàn thành task (với hình ảnh minh chứng)
-- Status: COMPLETED hoặc FAILED
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM collector_reports WHERE collection_request_id = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260204-001'))
BEGIN
    DECLARE @req4_report INT = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260204-001');
    DECLARE @req5_report INT = (SELECT id FROM collection_requests WHERE request_code = 'REQ-20260203-001');
    DECLARE @col1_report INT = (SELECT id FROM collectors WHERE user_id = 101);

    INSERT INTO collector_reports (collection_request_id, collector_id, status,
                                   collector_note, collected_at, latitude, longitude, created_at)
    VALUES
    (@req4_report, @col1_report, 'COMPLETED',
     N'Đã thu gom thành công 15.5kg rác tái chế. Khu vực sạch sẽ.',
     '2026-02-04 09:00:00', 10.77800000, 106.70800000, '2026-02-04 09:00:00'),

    (@req5_report, @col1_report, 'COMPLETED',
     N'Thu gom 22.3kg rác sinh hoạt. Không có vấn đề gì.',
     '2026-02-03 11:15:00', 10.77900000, 106.70900000, '2026-02-03 11:15:00');
END
GO

-- ============================================================================
-- 5. COLLECTOR_REPORT_IMAGES TABLE
-- Hình ảnh minh chứng cho reports
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM collector_report_images WHERE collector_report_id = (SELECT TOP 1 id FROM collector_reports ORDER BY id))
BEGIN
    DECLARE @report1 INT = (SELECT TOP 1 id FROM collector_reports ORDER BY id);
    DECLARE @report2 INT = (SELECT TOP 1 id FROM collector_reports ORDER BY id DESC);

    INSERT INTO collector_report_images (collector_report_id, image_url, image_public_id, created_at)
    VALUES
    -- Report 1: Before & After
    (@report1, 'https://res.cloudinary.com/demo/collector/report1_before.jpg', 'collector/report1_before', '2026-02-04 08:55:00'),
    (@report1, 'https://res.cloudinary.com/demo/collector/report1_after.jpg', 'collector/report1_after', '2026-02-04 09:00:00'),

    -- Report 2: Before, After & Waste
    (@report2, 'https://res.cloudinary.com/demo/collector/report2_before.jpg', 'collector/report2_before', '2026-02-03 11:10:00'),
    (@report2, 'https://res.cloudinary.com/demo/collector/report2_after.jpg', 'collector/report2_after', '2026-02-03 11:15:00'),
    (@report2, 'https://res.cloudinary.com/demo/collector/report2_waste.jpg', 'collector/report2_waste', '2026-02-03 11:15:00');
END
GO

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

PRINT '';
PRINT '=== 1. COLLECTORS SUMMARY ===';
SELECT 
    c.employee_code,
    c.full_name,
    c.status,
    c.vehicle_type,
    c.total_collections,
    c.successful_collections,
    c.total_weight_collected,
    CASE c.status
        WHEN 'AVAILABLE' THEN N'✅ Sẵn sàng'
        WHEN 'ACTIVE' THEN N'🔵 Đang làm'
        WHEN 'INACTIVE' THEN N'⏸️ Nghỉ'
        WHEN 'SUSPEND' THEN N'❌ Tạm ngừng'
    END AS status_display
FROM collectors c
WHERE c.enterprise_id = 1
ORDER BY c.id;

PRINT '';
PRINT '=== 2. COLLECTOR 1 TASKS ===';
SELECT 
    cr.request_code,
    cr.status,
    wr.address,
    wt.name AS waste_type,
    cr.assigned_at,
    cr.collected_at,
    cr.actual_weight_kg
FROM collection_requests cr
JOIN waste_reports wr ON cr.report_id = wr.id
JOIN waste_types wt ON wr.waste_type_id = wt.id
WHERE cr.collector_id = (SELECT id FROM collectors WHERE user_id = 101)
ORDER BY cr.created_at DESC;

PRINT '';
PRINT '=== 3. COLLECTION TRACKING AUDIT LOG ===';
SELECT 
    cr.request_code,
    ct.action,
    ct.note,
    ct.created_at
FROM collection_tracking ct
JOIN collection_requests cr ON ct.collection_request_id = cr.id
WHERE ct.collector_id = (SELECT id FROM collectors WHERE user_id = 101)
ORDER BY ct.created_at DESC;

PRINT '';
PRINT '=== 4. COLLECTOR REPORTS ===';
SELECT 
    cr.request_code,
    crep.status,
    crep.collector_note,
    crep.collected_at,
    COUNT(cri.id) AS image_count
FROM collector_reports crep
JOIN collection_requests cr ON crep.collection_request_id = cr.id
LEFT JOIN collector_report_images cri ON crep.id = cri.collector_report_id
WHERE crep.collector_id = (SELECT id FROM collectors WHERE user_id = 101)
GROUP BY cr.request_code, crep.status, crep.collector_note, crep.collected_at
ORDER BY crep.collected_at DESC;

PRINT '';
PRINT '=== 5. ACTIVE TASKS PER COLLECTOR ===';
SELECT 
    c.employee_code,
    c.full_name,
    COUNT(CASE WHEN cr.status IN ('ASSIGNED', 'ACCEPTED_COLLECTOR', 'ON_THE_WAY') THEN 1 END) AS active_tasks,
    COUNT(CASE WHEN cr.status = 'COLLECTED' THEN 1 END) AS completed_tasks
FROM collectors c
LEFT JOIN collection_requests cr ON c.id = cr.collector_id
WHERE c.enterprise_id = 1
GROUP BY c.employee_code, c.full_name
ORDER BY c.employee_code;

-- ============================================================================
-- DATA SUMMARY
-- ============================================================================
PRINT '';
PRINT '=== DATA SUMMARY ===';
PRINT 'Collectors: 4 (AVAILABLE: 1, ACTIVE: 1, INACTIVE: 1, SUSPEND: 1)';
PRINT 'Collection Requests: 8';
PRINT 'Collection Tracking Logs: 14';
PRINT 'Collector Reports: 2';
PRINT 'Report Images: 5';
PRINT '';
PRINT '✅ All collector-related data inserted successfully!';
