
USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'WasteManagementDB')
BEGIN
    ALTER DATABASE WasteManagementDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE WasteManagementDB;
END
GO

CREATE DATABASE WasteManagementDB;
GO

USE WasteManagementDB;
GO

-- ============================================
-- 1. ROLES & PERMISSIONS
-- ============================================

CREATE TABLE roles (
    id INT PRIMARY KEY IDENTITY(1,1),
    role_code NVARCHAR(20) UNIQUE NOT NULL,
    role_name NVARCHAR(50) NOT NULL,
    description NVARCHAR(500),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE permissions (
    id INT PRIMARY KEY IDENTITY(1,1),
    permission_code NVARCHAR(100) UNIQUE NOT NULL,
    permission_name NVARCHAR(255) NOT NULL,
    module NVARCHAR(50) NOT NULL,
    description NVARCHAR(500)
);

CREATE TABLE role_permissions (
    id INT PRIMARY KEY IDENTITY(1,1),
    role_id INT NOT NULL FOREIGN KEY REFERENCES roles(id),
    permission_id INT NOT NULL FOREIGN KEY REFERENCES permissions(id),
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id)
);

-- ============================================
-- 2. ENTERPRISE
-- ============================================

CREATE TABLE enterprise (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL,
    address NVARCHAR(500),
    ward NVARCHAR(100),
    city NVARCHAR(100),
    phone NVARCHAR(20),
    email NVARCHAR(255),
    license_number NVARCHAR(100),
    tax_code NVARCHAR(50),
    capacity_kg_per_day DECIMAL(12,2),
    supported_waste_type_codes NVARCHAR(MAX),
    service_wards NVARCHAR(MAX),
    service_cities NVARCHAR(MAX),
    status NVARCHAR(20) DEFAULT 'active',
    total_collected_weight DECIMAL(12,2) DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 3. USERS
-- ============================================

CREATE TABLE users (
    id INT PRIMARY KEY IDENTITY(1,1),
    email NVARCHAR(255) UNIQUE NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20),
    avatar_url NVARCHAR(500),
    role_id INT NOT NULL FOREIGN KEY REFERENCES roles(id),
    enterprise_id INT NULL UNIQUE FOREIGN KEY REFERENCES enterprise(id),
    status NVARCHAR(20) DEFAULT 'active',
    last_login DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 4. CITIZENS
-- ============================================

CREATE TABLE citizens (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT UNIQUE NOT NULL FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE,
    email NVARCHAR(255),
    full_name NVARCHAR(255),
    password_hash NVARCHAR(255),
    address NVARCHAR(500),
    phone NVARCHAR(20),
    ward NVARCHAR(100),
    city NVARCHAR(100),
    total_points INT DEFAULT 0,
    total_reports INT DEFAULT 0,
    valid_reports INT DEFAULT 0
);

-- ============================================
-- 5. WASTE TYPES
-- ============================================

CREATE TABLE waste_types (
    id INT PRIMARY KEY IDENTITY(1,1),
    code NVARCHAR(20) UNIQUE NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    category NVARCHAR(50) NOT NULL,
    base_points INT DEFAULT 0,
    is_recyclable BIT DEFAULT 1,
    handling_instructions NVARCHAR(1000),
    icon_url NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 7. WASTE REPORTS
-- ============================================

CREATE TABLE waste_reports (
    id INT PRIMARY KEY IDENTITY(1,1),
    report_code NVARCHAR(20) UNIQUE NOT NULL,
    citizen_id INT NOT NULL FOREIGN KEY REFERENCES citizens(id),
    waste_type_id INT NOT NULL FOREIGN KEY REFERENCES waste_types(id),
    description NVARCHAR(1000),
    estimated_weight_kg DECIMAL(10,2),
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    address NVARCHAR(500),
    ward NVARCHAR(100),
    city NVARCHAR(100),
    images NVARCHAR(MAX),
    status NVARCHAR(20) DEFAULT 'pending',
    is_valid BIT NULL,
    validation_note NVARCHAR(500),
    validated_by INT FOREIGN KEY REFERENCES users(id),
    validated_at DATETIME2,
    points_awarded INT DEFAULT 0,
    quality_rating INT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 8. COLLECTORS
-- ============================================

CREATE TABLE collectors (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT UNIQUE NOT NULL FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE,
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES enterprise(id),
    employee_code NVARCHAR(50),
    vehicle_type NVARCHAR(50),
    vehicle_plate NVARCHAR(20),
    status NVARCHAR(20) DEFAULT 'available',
    current_latitude DECIMAL(10,8),
    current_longitude DECIMAL(11,8),
    last_location_update DATETIME2,
    total_collections INT DEFAULT 0,
    successful_collections INT DEFAULT 0,
    total_weight_collected DECIMAL(12,2) DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 9. COLLECTION REQUESTS
-- ============================================

CREATE TABLE collection_requests (
    id INT PRIMARY KEY IDENTITY(1,1),
    request_code NVARCHAR(20) UNIQUE NOT NULL,
    report_id INT NOT NULL FOREIGN KEY REFERENCES waste_reports(id),
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES enterprise(id),
    collector_id INT FOREIGN KEY REFERENCES collectors(id),
    status NVARCHAR(20) DEFAULT 'pending',
    priority NVARCHAR(20) DEFAULT 'normal',
    rejection_reason NVARCHAR(500),
    assigned_at DATETIME2,
    estimated_arrival DATETIME2,
    actual_weight_kg DECIMAL(10,2),
    collection_images NVARCHAR(MAX),
    collected_at DATETIME2,
    collection_note NVARCHAR(500),
    distance_km DECIMAL(10,2),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 10. COLLECTION TRACKING
-- ============================================

CREATE TABLE collection_tracking (
    id INT PRIMARY KEY IDENTITY(1,1),
    collection_request_id INT NOT NULL FOREIGN KEY REFERENCES collection_requests(id),
    collector_id INT NOT NULL FOREIGN KEY REFERENCES collectors(id),
    action NVARCHAR(50) NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    note NVARCHAR(500),
    images NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 11. POINT RULES
-- ============================================

CREATE TABLE point_rules (
    id INT PRIMARY KEY IDENTITY(1,1),
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES enterprise(id),
    rule_name NVARCHAR(255) NOT NULL,
    rule_type NVARCHAR(30) NOT NULL,
    waste_type_id INT FOREIGN KEY REFERENCES waste_types(id),
    min_weight_kg DECIMAL(10,2),
    max_weight_kg DECIMAL(10,2),
    min_quality_rating INT,
    max_processing_hours INT,
    base_points INT NOT NULL,
    multiplier DECIMAL(3,2) DEFAULT 1.00,
    is_active BIT DEFAULT 1,
    valid_from DATETIME2,
    valid_to DATETIME2,
    priority INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 12. POINT TRANSACTIONS
-- ============================================

CREATE TABLE point_transactions (
    id INT PRIMARY KEY IDENTITY(1,1),
    citizen_id INT NOT NULL FOREIGN KEY REFERENCES citizens(id),
    report_id INT FOREIGN KEY REFERENCES waste_reports(id),
    collection_request_id INT FOREIGN KEY REFERENCES collection_requests(id),
    rule_id INT FOREIGN KEY REFERENCES point_rules(id),
    points INT NOT NULL,
    transaction_type NVARCHAR(30) NOT NULL,
    description NVARCHAR(500),
    balance_after INT NOT NULL,
    created_by INT FOREIGN KEY REFERENCES users(id),
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE leaderboard (
    id INT PRIMARY KEY IDENTITY(1,1),
    citizen_id INT NOT NULL FOREIGN KEY REFERENCES citizens(id),
    ward NVARCHAR(100),
    city NVARCHAR(100),
    period_type NVARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_points INT NOT NULL DEFAULT 0,
    total_reports INT NOT NULL DEFAULT 0,
    valid_reports INT NOT NULL DEFAULT 0,
    total_weight_kg DECIMAL(10,2) DEFAULT 0,
    rank_position INT,
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 14. FEEDBACKS
-- ============================================

CREATE TABLE feedbacks (
    id INT PRIMARY KEY IDENTITY(1,1),
    feedback_code NVARCHAR(20) UNIQUE NOT NULL,
    citizen_id INT NOT NULL FOREIGN KEY REFERENCES citizens(id),
    collection_request_id INT FOREIGN KEY REFERENCES collection_requests(id),
    feedback_type NVARCHAR(20) NOT NULL,
    subject NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    images NVARCHAR(MAX),
    severity NVARCHAR(20) DEFAULT 'normal',
    status NVARCHAR(20) DEFAULT 'pending',
    assigned_to INT FOREIGN KEY REFERENCES users(id),
    assigned_at DATETIME2,
    resolution NVARCHAR(MAX),
    resolved_by INT FOREIGN KEY REFERENCES users(id),
    resolved_at DATETIME2,
    responses NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);



-- ============================================
-- 17. SYSTEM SETTINGS
-- ============================================

CREATE TABLE system_settings (
    id INT PRIMARY KEY IDENTITY(1,1),
    setting_key NVARCHAR(100) UNIQUE NOT NULL,
    setting_value NVARCHAR(MAX),
    data_type NVARCHAR(20),
    category NVARCHAR(50),
    description NVARCHAR(500),
    updated_by INT FOREIGN KEY REFERENCES users(id),
    updated_at DATETIME2 DEFAULT GETDATE()
);



-- 1. ROLES
INSERT INTO roles (role_code, role_name, description) VALUES
('citizen', N'Công dân', N'Người dân báo cáo rác và nhận điểm thưởng'),
('collector', N'Nhân viên thu gom', N'Nhân viên thực hiện thu gom rác'),
('enterprise', N'Doanh nghiệp tái chế', N'Doanh nghiệp quản lý và điều phối thu gom'),
('admin', N'Quản trị viên', N'Quản trị hệ thống và giải quyết tranh chấp');

INSERT INTO permissions (permission_code, permission_name, module, description) VALUES
('report.create', N'Tạo báo cáo rác', 'report', N'Báo cáo rác với ảnh + GPS + mô tả'),
('report.view_own', N'Xem báo cáo của mình', 'report', N'Theo dõi trạng thái báo cáo'),
('report.classify_waste', N'Phân loại rác', 'report', N'Chọn loại rác khi tạo báo cáo'),
('points.view_own', N'Xem điểm thưởng', 'points', N'Xem lịch sử điểm thưởng'),
('leaderboard.view', N'Xem bảng xếp hạng', 'points', N'Xem xếp hạng theo khu vực'),
('feedback.create', N'Gửi phản hồi/khiếu nại', 'feedback', N'Khiếu nại khi thu gom không đúng'),
('enterprise.manage_capacity', N'Quản lý năng lực', 'enterprise', N'Đăng ký loại rác/công suất/khu vực'),
('enterprise.view_requests', N'Xem yêu cầu thu gom', 'collection', N'Xem yêu cầu trong phạm vi hoạt động'),
('enterprise.accept_reject', N'Chấp nhận/Từ chối', 'collection', N'Quyết định tiếp nhận yêu cầu'),
('enterprise.assign_collector', N'Phân công collector', 'collection', N'Gán yêu cầu cho nhân viên'),
('enterprise.track_realtime', N'Theo dõi thời gian thực', 'collection', N'Xem tiến độ và trạng thái'),
('enterprise.view_statistics', N'Xem báo cáo thống kê', 'statistics', N'Báo cáo khối lượng theo loại/khu vực/thời gian'),
('enterprise.manage_point_rules', N'Cấu hình quy tắc điểm', 'points', N'Tạo quy tắc tính điểm thưởng'),
('collection.view_assigned', N'Xem yêu cầu được gán', 'collection', N'Nhận yêu cầu từ Enterprise'),
('collection.update_status', N'Cập nhật trạng thái', 'collection', N'Assigned/On the way/Collected'),
('collection.confirm_complete', N'Xác nhận hoàn thành', 'collection', N'Upload ảnh xác nhận'),
('collection.view_history', N'Xem lịch sử công việc', 'collection', N'Xem số lượng đã hoàn thành'),
('admin.manage_users', N'Quản lý tài khoản', 'admin', N'Quản lý tài khoản và phân quyền'),
('admin.monitor_system', N'Giám sát hệ thống', 'admin', N'Giám sát hoạt động tổng thể'),
('admin.resolve_disputes', N'Giải quyết tranh chấp', 'feedback', N'Xử lý khiếu nại'),
('admin.view_all', N'Xem toàn bộ dữ liệu', 'admin', N'Truy cập tất cả báo cáo/thống kê');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM (VALUES
    ('citizen','report.create'),
    ('citizen','report.view_own'),
    ('citizen','report.classify_waste'),
    ('citizen','points.view_own'),
    ('citizen','leaderboard.view'),
    ('citizen','feedback.create'),
    ('collector','collection.view_assigned'),
    ('collector','collection.update_status'),
    ('collector','collection.confirm_complete'),
    ('collector','collection.view_history'),
    ('enterprise','enterprise.manage_capacity'),
    ('enterprise','enterprise.view_requests'),
    ('enterprise','enterprise.accept_reject'),
    ('enterprise','enterprise.assign_collector'),
    ('enterprise','enterprise.track_realtime'),
    ('enterprise','enterprise.view_statistics'),
    ('enterprise','enterprise.manage_point_rules')
) v(role_code, permission_code)
JOIN roles r ON r.role_code = v.role_code
JOIN permissions p ON p.permission_code = v.permission_code;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_code = 'admin';

-- 4. WASTE TYPES
INSERT INTO waste_types (code, name, description, category, base_points, is_recyclable) VALUES
('PLASTIC', N'Nhựa tái chế', N'Chai nhựa, hộp nhựa PET, HDPE, PP', 'plastic', 10, 1),
('PAPER', N'Giấy & Carton', N'Giấy báo, hộp giấy, bìa carton', 'paper', 8, 1),
('METAL', N'Kim loại', N'Lon nhôm, sắt vụn, đồng, inox', 'metal', 15, 1),
('GLASS', N'Thủy tinh', N'Chai lọ thủy tinh các loại', 'glass', 12, 1),
('ELECTRONIC', N'Rác điện tử', N'Thiết bị điện tử cũ, pin, bóng đèn', 'electronic', 20, 1),
('ORGANIC', N'Hữu cơ', N'Rác thực phẩm, lá cây, rau quả', 'organic', 5, 0),
('HAZARDOUS', N'Nguy hại', N'Hóa chất, thuốc trừ sâu, pin lithium', 'hazardous', 0, 0);

-- 5. ENTERPRISE
INSERT INTO enterprise (name, address, ward, city, phone, email, license_number, tax_code, capacity_kg_per_day, supported_waste_type_codes, service_wards, service_cities) VALUES
(N'Công ty TNHH Tái chế Xanh', N'123 Nguyễn Văn Linh, Q.7, TP.HCM', N'Phường Tân Phong', N'TP.HCM', '028-1234-5678', 'info@taichexanh.vn', 'GP-2024-001', '0312345678', 2000, '["PLASTIC","PAPER","METAL"]', '["Phường Bến Nghé","Phường Bến Thành","Phường Tân Định"]', '["TP.HCM"]'),
(N'Công ty CP Môi trường Sạch', N'456 Lê Văn Việt, Q.9, TP.HCM', N'Phường Hiệp Phú', N'TP.HCM', '028-8765-4321', 'contact@moitruongsach.vn', 'GP-2024-002', '0387654321', 1500, '["PLASTIC","ELECTRONIC","GLASS"]', '["Phường 15","Phường 6"]', '["TP.HCM"]'),
(N'Doanh nghiệp Tái chế Phương Nam', N'789 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', N'Phường 21', N'TP.HCM', '028-5555-6666', 'phuongnam@recycle.vn', 'GP-2024-003', '0355556666', 1000, '["PAPER","ORGANIC"]', '["Phường 6","Phường 15"]', '["TP.HCM"]');

-- 6. USERS
-- Password hash = 'password123' (bcrypt)
INSERT INTO users (email, password_hash, full_name, phone, role_id, enterprise_id, status) VALUES
-- Admin
('admin@wastemanagement.vn', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Nguyễn Văn Admin', '0901234567', 4, NULL, 'active'),
-- Enterprise Admins
('manager1@taichexanh.vn', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Trần Thị Lan', '0902345678', 3, 1, 'active'),
('manager2@moitruongsach.vn', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Lê Văn Hùng', '0903456789', 3, 2, 'active'),
-- Collectors
('collector1@taichexanh.vn', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Phạm Văn Minh', '0904567890', 2, 1, 'active'),
('collector2@taichexanh.vn', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Ngô Thị Hoa', '0905678901', 2, 1, 'active'),
('collector3@moitruongsach.vn', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Hoàng Văn Nam', '0906789012', 2, 2, 'active'),
-- Citizens
('citizen1@gmail.com', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Võ Thị Mai', '0907890123', 1, NULL, 'active'),
('citizen2@gmail.com', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Đặng Văn Tùng', '0908901234', 1, NULL, 'active'),
('citizen3@gmail.com', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Bùi Thị Hương', '0909012345', 1, NULL, 'active'),
('citizen4@gmail.com', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Lý Văn Đức', '0900123456', 1, NULL, 'active'),
('citizen5@gmail.com', '$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu', N'Trịnh Thị Nga', '0911234567', 1, NULL, 'active');

-- 8. COLLECTORS
INSERT INTO collectors (user_id, enterprise_id, employee_code, vehicle_type, vehicle_plate, status) VALUES
(4, 1, 'COL-001', N'Xe tải nhỏ', '59C-12345', 'available'),
(5, 1, 'COL-002', N'Xe ba gác', '59B-67890', 'available'),
(6, 2, 'COL-003', N'Xe máy', '59P1-11111', 'available');

-- 9. CITIZENS
INSERT INTO citizens (user_id, address, ward, city, total_points, total_reports, valid_reports) VALUES
(7, N'12 Nguyễn Huệ', N'Phường Bến Nghé', N'TP.HCM', 150, 12, 10),
(8, N'45 Lê Lợi', N'Phường Bến Thành', N'TP.HCM', 280, 25, 22),
(9, N'78 Hai Bà Trưng', N'Phường Tân Định', N'TP.HCM', 95, 8, 7),
(10, N'100 Điện Biên Phủ', N'Phường 15', N'TP.HCM', 320, 30, 28),
(11, N'200 Nguyễn Thị Minh Khai', N'Phường 6', N'TP.HCM', 180, 15, 14);

UPDATE c
SET
    c.email = u.email,
    c.full_name = u.full_name,
    c.password_hash = u.password_hash,
    c.phone = COALESCE(c.phone, u.phone)
FROM citizens c
JOIN users u ON u.id = c.user_id;

-- 12. WASTE REPORTS
INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight_kg, latitude, longitude, address, ward, city, images, status, is_valid, points_awarded, quality_rating, created_at) VALUES
('WR-20250115-001', 1, 1, N'Nhiều chai nhựa PET đã rửa sạch', 5.5, 10.7769, 106.7009, N'12 Nguyễn Huệ, Q.1', N'Phường Bến Nghé', N'TP.HCM', '["https://storage.example.com/img1.jpg"]', 'collected', 1, 55, 5, '2025-01-15 08:30:00'),
('WR-20250115-002', 2, 2, N'Thùng carton và giấy báo cũ', 8.0, 10.7731, 106.6989, N'45 Lê Lợi, Q.1', N'Phường Bến Thành', N'TP.HCM', '["https://storage.example.com/img2.jpg"]', 'collected', 1, 64, 4, '2025-01-15 09:15:00'),
('WR-20250115-003', 3, 3, N'Lon nhôm và sắt vụn', 3.2, 10.7856, 106.6912, N'78 Hai Bà Trưng, Q.1', N'Phường Tân Định', N'TP.HCM', '["https://storage.example.com/img3.jpg"]', 'assigned', 1, 0, NULL, '2025-01-15 10:00:00'),
('WR-20250115-004', 4, 1, N'Chai nhựa HDPE từ nước giặt', 4.0, 10.8012, 106.7102, N'100 Điện Biên Phủ, Bình Thạnh', N'Phường 15', N'TP.HCM', '["https://storage.example.com/img4.jpg"]', 'accepted', 1, 0, NULL, '2025-01-15 11:30:00'),
('WR-20250115-005', 5, 4, N'Chai lọ thủy tinh các loại', 6.5, 10.7823, 106.6845, N'200 Nguyễn Thị Minh Khai, Q.3', N'Phường 6', N'TP.HCM', '["https://storage.example.com/img5.jpg"]', 'pending', NULL, 0, NULL, '2025-01-15 14:00:00'),
('WR-20250116-001', 1, 5, N'Điện thoại cũ và pin', 1.2, 10.7769, 106.7009, N'12 Nguyễn Huệ, Q.1', N'Phường Bến Nghé', N'TP.HCM', '["https://storage.example.com/img6.jpg"]', 'pending', NULL, 0, NULL, '2025-01-16 09:00:00'),
('WR-20250116-002', 2, 1, N'Chai nhựa PP từ hộp sữa', 3.5, 10.7731, 106.6989, N'45 Lê Lợi, Q.1', N'Phường Bến Thành', N'TP.HCM', '["https://storage.example.com/img7.jpg"]', 'pending', NULL, 0, NULL, '2025-01-16 10:30:00');

-- 13. COLLECTION REQUESTS
INSERT INTO collection_requests (request_code, report_id, enterprise_id, collector_id, status, priority, assigned_at, estimated_arrival, actual_weight_kg, collected_at, distance_km, created_at) VALUES
('CR-20250115-001', 1, 1, 1, 'collected', 'normal', '2025-01-15 09:00:00', '2025-01-15 10:00:00', 5.8, '2025-01-15 10:15:00', 2.5, '2025-01-15 08:45:00'),
('CR-20250115-002', 2, 1, 1, 'collected', 'normal', '2025-01-15 10:30:00', '2025-01-15 11:30:00', 8.2, '2025-01-15 11:45:00', 1.8, '2025-01-15 09:30:00'),
('CR-20250115-003', 3, 1, 2, 'assigned', 'high', '2025-01-15 11:00:00', '2025-01-15 12:00:00', NULL, NULL, 3.2, '2025-01-15 10:15:00'),
('CR-20250115-004', 4, 2, NULL, 'accepted', 'normal', NULL, NULL, NULL, NULL, 4.5, '2025-01-15 12:00:00');

-- 14. COLLECTION TRACKING
INSERT INTO collection_tracking (collection_request_id, collector_id, action, latitude, longitude, note, created_at) VALUES
-- Request 1 - Hoàn thành
(1, 1, 'assigned', 10.7720, 106.7050, N'Đã nhận yêu cầu', '2025-01-15 09:00:00'),
(1, 1, 'on_the_way', 10.7745, 106.7030, N'Đang di chuyển', '2025-01-15 09:30:00'),
(1, 1, 'arrived', 10.7769, 106.7009, N'Đã đến nơi', '2025-01-15 10:00:00'),
(1, 1, 'collected', 10.7769, 106.7009, N'Đã thu gom thành công', '2025-01-15 10:15:00'),
-- Request 2 - Hoàn thành
(2, 1, 'assigned', 10.7769, 106.7009, N'Đã nhận yêu cầu', '2025-01-15 10:30:00'),
(2, 1, 'on_the_way', 10.7750, 106.6995, N'Đang di chuyển', '2025-01-15 11:00:00'),
(2, 1, 'collected', 10.7731, 106.6989, N'Thu gom xong', '2025-01-15 11:45:00'),
-- Request 3 - Đang xử lý
(3, 2, 'assigned', 10.7800, 106.6950, N'Đã nhận yêu cầu', '2025-01-15 11:00:00');

-- 15. POINT RULES
INSERT INTO point_rules (enterprise_id, rule_name, rule_type, waste_type_id, min_weight_kg, max_weight_kg, min_quality_rating, max_processing_hours, base_points, multiplier, is_active) VALUES
-- Công ty Tái chế Xanh
(1, N'Điểm cơ bản - Nhựa', 'waste_type', 1, 0, 100, NULL, NULL, 10, 1.00, 1),
(1, N'Điểm cơ bản - Giấy', 'waste_type', 2, 0, 100, NULL, NULL, 8, 1.00, 1),
(1, N'Điểm cơ bản - Kim loại', 'waste_type', 3, 0, 100, NULL, NULL, 15, 1.00, 1),
(1, N'Bonus chất lượng cao', 'quality', NULL, NULL, NULL, 5, NULL, 20, 1.50, 1),
(1, N'Bonus số lượng lớn', 'quantity', NULL, 10, 50, NULL, NULL, 50, 1.20, 1),
(1, N'Bonus xử lý nhanh', 'speed', NULL, NULL, NULL, NULL, 24, 15, 1.00, 1),
-- Công ty Môi trường Sạch
(2, N'Điểm cơ bản - Nhựa', 'waste_type', 1, 0, 100, NULL, NULL, 10, 1.00, 1),
(2, N'Điểm cơ bản - Điện tử', 'waste_type', 5, 0, 50, NULL, NULL, 20, 1.00, 1),
(2, N'Bonus tuần xanh', 'bonus', NULL, NULL, NULL, NULL, NULL, 30, 2.00, 1);

-- 16. POINT TRANSACTIONS
INSERT INTO point_transactions (citizen_id, report_id, collection_request_id, rule_id, points, transaction_type, description, balance_after, created_at) VALUES
(1, 1, 1, 1, 55, 'earned', N'Điểm thu gom nhựa - 5.5kg', 150, '2025-01-15 10:20:00'),
(2, 2, 2, 2, 64, 'earned', N'Điểm thu gom giấy - 8kg', 280, '2025-01-15 11:50:00'),
(2, 2, 2, 4, 20, 'bonus', N'Bonus chất lượng cao', 300, '2025-01-15 11:50:00'),
(4, NULL, NULL, NULL, 50, 'bonus', N'Bonus người dùng mới', 320, '2025-01-10 10:00:00');

INSERT INTO leaderboard (citizen_id, ward, city, period_type, period_start, period_end, total_points, total_reports, valid_reports, total_weight_kg, rank_position) VALUES
(4, N'Phường 15', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 320, 30, 28, 125.5, 1),
(2, N'Phường Bến Thành', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 280, 25, 22, 98.2, 2),
(5, N'Phường 6', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 180, 15, 14, 65.0, 3),
(1, N'Phường Bến Nghé', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 150, 12, 10, 52.3, 4),
(3, N'Phường Tân Định', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 95, 8, 7, 35.8, 5);

-- 18. FEEDBACKS
INSERT INTO feedbacks (feedback_code, citizen_id, collection_request_id, feedback_type, subject, content, severity, status, assigned_to, created_at) VALUES
('FB-20250115-001', 1, 1, 'feedback', N'Dịch vụ tốt', N'Nhân viên thu gom rất nhiệt tình và đúng giờ. Cảm ơn!', 'low', 'resolved', 1, '2025-01-15 11:00:00'),
('FB-20250115-002', 3, NULL, 'complaint', N'Chờ lâu', N'Báo cáo đã 2 ngày nhưng chưa có ai đến thu gom', 'high', 'reviewing', 1, '2025-01-15 15:00:00'),
('FB-20250116-001', 2, 2, 'suggestion', N'Đề xuất thêm điểm', N'Đề xuất tăng điểm thưởng cho rác điện tử vì khó thu gom', 'normal', 'pending', NULL, '2025-01-16 09:30:00');

-- 19. FEEDBACK RESPONSES
UPDATE feedbacks
SET responses = CASE id
    WHEN 1 THEN N'[{"responder_id":1,"response":"Cảm ơn bạn đã phản hồi tích cực! Chúng tôi sẽ tiếp tục cải thiện dịch vụ.","is_internal":false,"created_at":"2025-01-15 14:00:00"}]'
    WHEN 2 THEN N'[{"responder_id":1,"response":"Đang kiểm tra với đơn vị thu gom. Sẽ phản hồi trong 24h.","is_internal":false,"created_at":"2025-01-15 16:00:00"},{"responder_id":1,"response":"Lưu ý: Khu vực Q.1 đang quá tải, cần điều phối thêm collector.","is_internal":true,"created_at":"2025-01-15 16:05:00"}]'
    ELSE responses
END;


-- 22. SYSTEM SETTINGS
INSERT INTO system_settings (setting_key, setting_value, data_type, category, description) VALUES
('max_pending_hours', '72', 'number', 'report', N'Thời gian tối đa báo cáo ở trạng thái pending (giờ)'),
('collection_timeout_hours', '24', 'number', 'collection', N'Timeout cho yêu cầu thu gom (giờ)'),
('default_point_multiplier', '1.0', 'number', 'points', N'Hệ số nhân điểm mặc định'),
('max_distance_km', '10', 'number', 'collection', N'Khoảng cách tối đa cho thu gom (km)'),
('leaderboard_min_reports', '5', 'number', 'points', N'Số báo cáo tối thiểu để lên bảng xếp hạng');


GO
