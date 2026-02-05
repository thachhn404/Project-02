-- =============================================
-- CLEANUP EXISTING DATA
-- =============================================

-- Disable all foreign key constraints to allow deleting data in any order
EXEC sp_msforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all"
GO

-- Delete data from tables
DELETE FROM leaderboard;
DELETE FROM feedbacks;
DELETE FROM point_transactions;
DELETE FROM collector_report_images;
DELETE FROM collector_reports;
DELETE FROM collection_tracking;
DELETE FROM collection_request_images;
DELETE FROM collection_requests;
DELETE FROM report_images;
DELETE FROM waste_reports;
DELETE FROM system_settings;
DELETE FROM point_rules;
DELETE FROM collectors;
DELETE FROM citizens;
DELETE FROM users;
DELETE FROM enterprise;
DELETE FROM waste_types;
DELETE FROM role_permissions;
DELETE FROM permissions;
DELETE FROM roles;
GO

-- =============================================
-- INSERT DATA
-- =============================================

-- 1. Roles
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (id, role_code, role_name, description, is_active, created_at) VALUES 
(1, 'ADMIN', 'Administrator', N'Quản trị viên hệ thống', 1, GETDATE()),
(2, 'CITIZEN', 'Citizen', N'Người dân tham gia tái chế', 1, GETDATE()),
(3, 'COLLECTOR', 'Collector', N'Người thu gom rác', 1, GETDATE()),
(4, 'ENTERPRISE_MANAGER', 'Enterprise Manager', N'Quản lý doanh nghiệp xử lý rác', 1, GETDATE());
SET IDENTITY_INSERT roles OFF;

-- 2. Permissions
SET IDENTITY_INSERT permissions ON;
INSERT INTO permissions (id, permission_code, permission_name, module, description) VALUES 
(1, 'USER_READ', 'Read Users', 'USER', N'Xem danh sách người dùng'),
(2, 'USER_WRITE', 'Write Users', 'USER', N'Thêm/Sửa người dùng'),
(3, 'REPORT_READ', 'Read Reports', 'REPORT', N'Xem báo cáo rác thải'),
(4, 'REPORT_CREATE', 'Create Report', 'REPORT', N'Tạo báo cáo rác thải'),
(5, 'COLLECTION_ASSIGN', 'Assign Collection', 'COLLECTION', N'Phân công thu gom');
SET IDENTITY_INSERT permissions OFF;

-- 3. Role Permissions
SET IDENTITY_INSERT role_permissions ON;
INSERT INTO role_permissions (id, role_id, permission_id) VALUES 
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), -- Admin has all
(6, 2, 4), (7, 2, 3), -- Citizen can create/read reports
(8, 3, 3); -- Collector can read reports
SET IDENTITY_INSERT role_permissions OFF;

-- 4. Waste Types
SET IDENTITY_INSERT waste_types ON;
INSERT INTO waste_types (id, code, name, description, category, base_points, is_recyclable, handling_instructions, icon_url, created_at) VALUES 
(1, 'PLASTIC', N'Nhựa', N'Chai nhựa, hộp nhựa', 'RECYCLABLE', 10, 1, N'Rửa sạch và làm khô', 'https://example.com/icons/plastic.png', GETDATE()),
(2, 'PAPER', N'Giấy', N'Giấy báo, bìa carton', 'RECYCLABLE', 5, 1, N'Giữ khô ráo, xếp gọn', 'https://example.com/icons/paper.png', GETDATE()),
(3, 'METAL', N'Kim loại', N'Vỏ lon, đồ kim loại hỏng', 'RECYCLABLE', 15, 1, N'Làm sạch thức ăn thừa', 'https://example.com/icons/metal.png', GETDATE()),
(4, 'GLASS', N'Thủy tinh', N'Chai lọ thủy tinh', 'RECYCLABLE', 8, 1, N'Cẩn thận tránh vỡ, bọc kỹ', 'https://example.com/icons/glass.png', GETDATE()),
(5, 'ORGANIC', N'Hữu cơ', N'Thức ăn thừa, vỏ rau củ', 'ORGANIC', 0, 0, N'Để trong túi phân hủy sinh học', 'https://example.com/icons/organic.png', GETDATE()),
(6, 'ELECTRONIC', N'Điện tử', N'Pin, thiết bị điện tử hỏng', 'HAZARDOUS', 50, 1, N'Để riêng, không đập vỡ', 'https://example.com/icons/electronic.png', GETDATE());
SET IDENTITY_INSERT waste_types OFF;

-- 5. Enterprise
SET IDENTITY_INSERT enterprise ON;
INSERT INTO enterprise (id, name, address, ward, city, phone, email, license_number, tax_code, capacity_kg_per_day, supported_waste_type_codes, service_wards, service_cities, status, total_collected_weight, created_at, updated_at) VALUES 
(1, N'Công ty Môi trường Xanh', N'123 Đường Lê Lợi', N'Phường Bến Nghé', N'TP. Hồ Chí Minh', '02838290001', 'contact@moitruongxanh.vn', 'LIC-2023-001', '0300123456', 5000.00, 'PLASTIC,PAPER,METAL', N'Phường Bến Nghé, Phường Đa Kao', N'TP. Hồ Chí Minh', 'ACTIVE', 12500.50, GETDATE(), GETDATE());
-- (2, N'Tái chế Đô Thị', N'456 Đường Nguyễn Huệ', N'Phường Bến Nghé', N'TP. Hồ Chí Minh', '02838290002', 'info@taichedothi.com', 'LIC-2023-002', '0300654321', 3000.00, 'GLASS,ELECTRONIC', N'Phường Bến Nghé', N'TP. Hồ Chí Minh', 'ACTIVE', 8000.00, GETDATE(), GETDATE());
SET IDENTITY_INSERT enterprise OFF;

-- 6. Users
SET IDENTITY_INSERT users ON;
INSERT INTO users (id, email, password_hash, full_name, phone, avatar_url, role_id, enterprise_id, status, last_login, created_at, updated_at) VALUES 
-- Password hash for 'password123' (BCrypt example, placeholder)
(1, 'admin@system.com', '$2a$10$w...hash...', N'System Admin', '0901234567', NULL, 1, NULL, 'active', GETDATE(), GETDATE(), GETDATE()),
(2, 'citizen1@email.com', '$2a$10$w...hash...', N'Nguyễn Văn A', '0912345678', NULL, 2, NULL, 'active', GETDATE(), GETDATE(), GETDATE()),
(3, 'citizen2@email.com', '$2a$10$w...hash...', N'Trần Thị B', '0923456789', NULL, 2, NULL, 'active', GETDATE(), GETDATE(), GETDATE()),
(4, 'collector1@email.com', '$2a$10$w...hash...', N'Lê Văn C', '0934567890', NULL, 3, 1, 'active', GETDATE(), GETDATE(), GETDATE());
-- (5, 'collector2@email.com', '$2a$10$w...hash...', N'Phạm Thị D', '0945678901', NULL, 3, 2, 'active', GETDATE(), GETDATE(), GETDATE());
SET IDENTITY_INSERT users OFF;

-- 7. Citizens
SET IDENTITY_INSERT citizens ON;
INSERT INTO citizens (id, user_id, email, full_name, password_hash, address, phone, ward, city, total_points, total_reports, valid_reports) VALUES 
(1, 2, 'citizen1@email.com', N'Nguyễn Văn A', '$2a$10$w...hash...', N'10 Đường số 1', '0912345678', N'Phường Bến Nghé', N'TP. Hồ Chí Minh', 150, 5, 4),
(2, 3, 'citizen2@email.com', N'Trần Thị B', '$2a$10$w...hash...', N'20 Đường số 2', '0923456789', N'Phường Đa Kao', N'TP. Hồ Chí Minh', 80, 3, 3);
SET IDENTITY_INSERT citizens OFF;

-- 8. Collectors
SET IDENTITY_INSERT collectors ON;
INSERT INTO collectors (id, user_id, enterprise_id, email, full_name, employee_code, vehicle_type, vehicle_plate, status, current_latitude, current_longitude, last_location_update, total_collections, successful_collections, total_weight_collected, created_at) VALUES 
(1, 4, 1, 'collector1@email.com', N'Lê Văn C', 'EMP001', N'Xe tải nhỏ', '59C-12345', 'AVAILABLE', 10.7769, 106.7009, GETDATE(), 20, 19, 500.50, GETDATE());
-- (2, 5, 2, 'collector2@email.com', N'Phạm Thị D', 'EMP002', N'Xe ba gác', '59C-67890', 'BUSY', 10.7800, 106.6980, GETDATE(), 15, 15, 300.00, GETDATE());
SET IDENTITY_INSERT collectors OFF;

-- 9. Point Rules
SET IDENTITY_INSERT point_rules ON;
INSERT INTO point_rules (id, enterprise_id, rule_name, rule_type, waste_type_id, min_weight_kg, max_weight_kg, min_quality_rating, max_processing_hours, base_points, multiplier, is_active, valid_from, valid_to, priority, created_at, updated_at) VALUES 
(1, 1, N'Thưởng nhựa sạch', 'WEIGHT_BASED', 1, 1.00, 100.00, NULL, NULL, 10, 1.50, 1, '2023-01-01', '2025-12-31', 1, GETDATE(), GETDATE()),
(2, 1, N'Thưởng giấy báo', 'WEIGHT_BASED', 2, 2.00, 50.00, NULL, NULL, 5, 1.20, 1, '2023-01-01', '2025-12-31', 1, GETDATE(), GETDATE());
SET IDENTITY_INSERT point_rules OFF;

-- 10. System Settings
SET IDENTITY_INSERT system_settings ON;
INSERT INTO system_settings (id, setting_key, setting_value, data_type, category, description, updated_by, updated_at) VALUES 
(1, 'min_withdrawal_points', '1000', 'INTEGER', 'FINANCE', N'Điểm tối thiểu để đổi quà', 1, GETDATE()),
(2, 'default_search_radius_km', '5', 'DOUBLE', 'MAP', N'Bán kính tìm kiếm mặc định (km)', 1, GETDATE());
SET IDENTITY_INSERT system_settings OFF;

-- 11. Waste Reports
SET IDENTITY_INSERT waste_reports ON;
INSERT INTO waste_reports (id, report_code, citizen_id, waste_type_id, description, latitude, longitude, address, images, status, created_at, updated_at) VALUES 
(1, 'RPT-20231001-001', 1, 1, N'Một túi lớn chai nhựa đã rửa sạch', 10.7770, 106.7010, N'10 Đường số 1, P. Bến Nghé, Q.1', '["https://res.cloudinary.com/demo/image/upload/v1/waste/plastic1.jpg"]', 'VERIFIED', DATEADD(day, -2, GETDATE()), DATEADD(day, -2, GETDATE())),
(2, 'RPT-20231002-002', 1, 2, N'Chồng báo cũ khoảng 5kg', 10.7770, 106.7010, N'10 Đường số 1, P. Bến Nghé, Q.1', '["https://res.cloudinary.com/demo/image/upload/v1/waste/paper1.jpg"]', 'COLLECTED', DATEADD(day, -1, GETDATE()), GETDATE()),
(3, 'RPT-20231003-003', 2, 6, N'Pin cũ và điện thoại hỏng', 10.7810, 106.6990, N'20 Đường số 2, P. Đa Kao, Q.1', '["https://res.cloudinary.com/demo/image/upload/v1/waste/electronic1.jpg"]', 'PENDING', GETDATE(), GETDATE());
SET IDENTITY_INSERT waste_reports OFF;

-- 12. Report Images
SET IDENTITY_INSERT report_images ON;
INSERT INTO report_images (id, report_id, image_url, image_type, uploaded_at) VALUES 
(1, 1, 'https://res.cloudinary.com/demo/image/upload/v1/waste/plastic1.jpg', 'report', DATEADD(day, -2, GETDATE())),
(2, 2, 'https://res.cloudinary.com/demo/image/upload/v1/waste/paper1.jpg', 'report', DATEADD(day, -1, GETDATE())),
(3, 3, 'https://res.cloudinary.com/demo/image/upload/v1/waste/electronic1.jpg', 'report', GETDATE());
SET IDENTITY_INSERT report_images OFF;

-- 13. Collection Requests
SET IDENTITY_INSERT collection_requests ON;
INSERT INTO collection_requests (id, request_code, report_id, enterprise_id, collector_id, status, assigned_at, accepted_at, started_at, actual_weight_kg, collected_at, created_at, updated_at) VALUES 
(1, 'REQ-001', 2, 1, 1, 'COMPLETED', DATEADD(hour, -20, GETDATE()), DATEADD(hour, -19, GETDATE()), DATEADD(hour, -18, GETDATE()), 5.5, DATEADD(hour, -17, GETDATE()), DATEADD(day, -1, GETDATE()), GETDATE());
SET IDENTITY_INSERT collection_requests OFF;

-- 14. Collection Request Images
SET IDENTITY_INSERT collection_request_images ON;
INSERT INTO collection_request_images (id, collection_request_id, cloudinary_public_id, image_url, image_role, uploaded_at) VALUES 
(1, 1, 'req_img_001', 'https://res.cloudinary.com/demo/image/upload/v1/waste/confirm1.jpg', 'confirmation', DATEADD(hour, -17, GETDATE()));
SET IDENTITY_INSERT collection_request_images OFF;

-- 15. Collection Tracking
SET IDENTITY_INSERT collection_tracking ON;
INSERT INTO collection_tracking (id, collection_request_id, collector_id, action, latitude, longitude, note, images, created_at) VALUES 
(1, 1, 1, 'PICKUP', 10.7770, 106.7010, N'Đã nhận rác từ người dân', NULL, DATEADD(hour, -18, GETDATE()));
SET IDENTITY_INSERT collection_tracking OFF;

-- 16. Collector Reports
SET IDENTITY_INSERT collector_reports ON;
INSERT INTO collector_reports (id, collection_request_id, collector_id, status, collector_note, actual_weight, collected_at, latitude, longitude, created_at) VALUES 
(1, 1, 1, 'APPROVED', N'Rác đúng loại, cân đúng ký', 5.5, DATEADD(hour, -17, GETDATE()), 10.7770, 106.7010, DATEADD(hour, -16, GETDATE()));
SET IDENTITY_INSERT collector_reports OFF;

-- 17. Collector Report Images
SET IDENTITY_INSERT collector_report_images ON;
INSERT INTO collector_report_images (id, collector_report_id, image_url, image_public_id, created_at) VALUES 
(1, 1, 'https://res.cloudinary.com/demo/image/upload/v1/waste/collected1.jpg', 'col_rpt_001', DATEADD(hour, -16, GETDATE()));
SET IDENTITY_INSERT collector_report_images OFF;

-- 18. Point Transactions
SET IDENTITY_INSERT point_transactions ON;
INSERT INTO point_transactions (id, citizen_id, report_id, collection_request_id, rule_id, points, transaction_type, description, balance_after, created_by, created_at) VALUES 
(1, 1, 2, 1, 2, 30, 'EARN', N'Cộng điểm từ yêu cầu thu gom REQ-001', 150, 1, DATEADD(hour, -16, GETDATE()));
SET IDENTITY_INSERT point_transactions OFF;

-- 19. Feedbacks
SET IDENTITY_INSERT feedbacks ON;
INSERT INTO feedbacks (id, feedback_code, citizen_id, collection_request_id, feedback_type, subject, content, images, severity, status, created_at, updated_at) VALUES 
(1, 'FB-001', 1, 1, 'COMPLIMENT', N'Thu gom nhanh', N'Nhân viên thân thiện, thu gom đúng giờ', NULL, 'LOW', 'NEW', GETDATE(), GETDATE());
SET IDENTITY_INSERT feedbacks OFF;

-- 20. Leaderboard
SET IDENTITY_INSERT leaderboard ON;
INSERT INTO leaderboard (id, citizen_id, ward, city, period_type, period_start, period_end, total_points, total_reports, valid_reports, total_weight_kg, rank_position, updated_at) VALUES 
(1, 1, N'Phường Bến Nghé', N'TP. Hồ Chí Minh', 'MONTHLY', '2023-10-01', '2023-10-31', 150, 5, 4, 25.5, 1, GETDATE()),
(2, 2, N'Phường Đa Kao', N'TP. Hồ Chí Minh', 'MONTHLY', '2023-10-01', '2023-10-31', 80, 3, 3, 15.0, 2, GETDATE());
SET IDENTITY_INSERT leaderboard OFF;

-- Re-enable constraints
EXEC sp_msforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all"
GO
