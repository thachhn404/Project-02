-- ============================================================================
-- TEST DATA FOR COLLECTOR WORKFLOW
-- Sử dụng CollectorStatus enum: ONLINE, OFFLINE, SUSPEND
-- ============================================================================

-- ============================================================================
-- PREREQUISITE 1: USERS (nếu chưa có user_id: 101, 102, 103, 104)
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 101)
BEGIN
    SET IDENTITY_INSERT users ON;
    INSERT INTO users (id, username, password, email, role, status, created_at, updated_at)
    VALUES
    (101, 'collector1_user', '$2a$10$dummyHashedPasswordForCollector1', 'collector1@example.com', 'COLLECTOR', 'ACTIVE', '2024-01-15 08:00:00', '2024-01-15 08:00:00'),
    (102, 'collector2_user', '$2a$10$dummyHashedPasswordForCollector2', 'collector2@example.com', 'COLLECTOR', 'ACTIVE', '2024-03-20 09:00:00', '2024-03-20 09:00:00'),
    (103, 'collector3_user', '$2a$10$dummyHashedPasswordForCollector3', 'collector3@example.com', 'COLLECTOR', 'INACTIVE', '2023-11-01 10:00:00', '2023-11-01 10:00:00'),
    (104, 'collector4_user', '$2a$10$dummyHashedPasswordForCollector4', 'collector4@example.com', 'COLLECTOR', 'ACTIVE', '2024-06-10 09:00:00', '2024-06-10 09:00:00');
    SET IDENTITY_INSERT users OFF;
END
GO

-- ============================================================================
-- PREREQUISITE 2: ENTERPRISES (nếu chưa có enterprise_id = 1)
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
-- 1. COLLECTORS TABLE
-- 4 Collectors với 4 trạng thái khác nhau: ONLINE, OFFLINE, SUSPEND
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM collectors WHERE user_id = 101)
BEGIN
    INSERT INTO collectors (user_id, enterprise_id, email, full_name, employee_code, 
                           vehicle_type, vehicle_plate, status, 
                           last_location_update,
                           total_collections, successful_collections, total_weight_collected, created_at)
    VALUES
    -- Collector 1: ONLINE - Sẵn sàng nhận task mới
    (101, 1, 'collector1@example.com', N'Nguyễn Văn A', 'COL-001', 
     'TRUCK', '29A-12345', 'ONLINE',
     '2026-02-05 07:00:00',
     50, 45, 1250.50, '2024-01-15 08:00:00'),
    
    -- Collector 2: ONLINE - Đang có nhiệm vụ (Vẫn tính là ONLINE)
    (102, 1, 'collector2@example.com', N'Trần Thị B', 'COL-002',
     'MOTORCYCLE', '29B-98765', 'ONLINE',
     '2026-02-05 07:15:00',
     15, 12, 320.75, '2024-03-20 09:00:00'),
    
    -- Collector 3: OFFLINE - Tạm nghỉ
    (103, 1, 'collector3@example.com', N'Lê Văn C', 'COL-003',
     'TRUCK', '29C-54321', 'OFFLINE',
     '2026-02-04 18:00:00',
     100, 95, 2500.00, '2023-11-01 10:00:00'),
    
    -- Collector 4: SUSPEND - Bị tạm ngừng (vi phạm)
    (104, 1, 'collector4@example.com', N'Phạm Thị D', 'COL-004',
     'MOTORCYCLE', '29D-11111', 'SUSPEND',
     '2026-02-03 16:00:00',
     30, 25, 680.00, '2024-06-10 09:00:00');
END
GO

-- ============================================================================
-- VERIFICATION: Kiểm tra collector status
-- ============================================================================
PRINT '=== COLLECTOR STATUSES ===';
SELECT 
    employee_code,
    full_name,
    status,
    total_collections,
    successful_collections,
    CASE 
        WHEN status = 'ONLINE' THEN N'✅ Đang online'
        WHEN status = 'OFFLINE' THEN N'⏸️ Đang offline'
        WHEN status = 'SUSPEND' THEN N'❌ Bị tạm ngừng'
    END AS status_desc
FROM collectors
WHERE enterprise_id = 1
ORDER BY 
    CASE status
        WHEN 'ONLINE' THEN 1
        WHEN 'OFFLINE' THEN 2
        WHEN 'SUSPEND' THEN 3
    END;

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- CollectorStatus Enum Values:
-- - ONLINE: Đang online, sẵn sàng nhận nhiệm vụ hoặc đang làm việc
-- - OFFLINE: Đang offline, không nhận task mới
-- - SUSPEND: Bị tạm ngừng (do vi phạm quy định)
--
-- Nghiệp vụ:
-- - Chỉ ONLINE mới có thể được gán task
-- - OFFLINE và SUSPEND không được gán task mới
-- - Khi login, status chuyển sang ONLINE
-- ============================================================================
