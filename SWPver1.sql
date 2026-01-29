
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
-- 2. USERS
-- ============================================

CREATE TABLE users (
    id INT PRIMARY KEY IDENTITY(1,1),
    email NVARCHAR(255) UNIQUE NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20),
    avatar_url NVARCHAR(500),
    role_id INT NOT NULL FOREIGN KEY REFERENCES roles(id),
    status NVARCHAR(20) DEFAULT 'active',
    last_login DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 3. CITIZENS
-- ============================================

CREATE TABLE citizens (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT UNIQUE NOT NULL FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE,
    address NVARCHAR(500),
    ward NVARCHAR(100),
    district NVARCHAR(100),
    city NVARCHAR(100),
    total_points INT DEFAULT 0,
    total_reports INT DEFAULT 0,
    valid_reports INT DEFAULT 0
);

-- ============================================
-- 4. RECYCLING ENTERPRISES
-- ============================================

CREATE TABLE recycling_enterprises (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL,
    address NVARCHAR(500),
    phone NVARCHAR(20),
    email NVARCHAR(255),
    license_number NVARCHAR(100),
    tax_code NVARCHAR(50),
    status NVARCHAR(20) DEFAULT 'active',
    total_collected_weight DECIMAL(12,2) DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE enterprise_admins (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT UNIQUE NOT NULL FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE,
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES recycling_enterprises(id),
    position NVARCHAR(100),
    is_owner BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE()
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
-- 6. ENTERPRISE CAPACITY & SERVICE AREAS
-- ============================================

CREATE TABLE enterprise_waste_capacity (
    id INT PRIMARY KEY IDENTITY(1,1),
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES recycling_enterprises(id),
    waste_type_id INT NOT NULL FOREIGN KEY REFERENCES waste_types(id),
    daily_capacity_kg DECIMAL(10,2) NOT NULL,
    current_load_kg DECIMAL(10,2) DEFAULT 0,
    price_per_kg DECIMAL(10,2),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT uq_enterprise_waste UNIQUE (enterprise_id, waste_type_id)
);

CREATE TABLE enterprise_service_areas (
    id INT PRIMARY KEY IDENTITY(1,1),
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES recycling_enterprises(id),
    city NVARCHAR(100) NOT NULL,
    district NVARCHAR(100) NOT NULL,
    ward NVARCHAR(100),
    priority_score INT DEFAULT 5,
    max_distance_km DECIMAL(5,2) DEFAULT 10,
    is_active BIT DEFAULT 1,
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
    district NVARCHAR(100),
    city NVARCHAR(100),
    images NVARCHAR(MAX),
    ai_suggested_type_id INT FOREIGN KEY REFERENCES waste_types(id),
    ai_confidence DECIMAL(5,2),
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

CREATE TABLE report_images (
    id INT PRIMARY KEY IDENTITY(1,1),
    report_id INT NOT NULL FOREIGN KEY REFERENCES waste_reports(id) ON DELETE CASCADE,
    image_url NVARCHAR(500) NOT NULL,
    image_type NVARCHAR(20) DEFAULT 'report',
    uploaded_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 8. COLLECTORS
-- ============================================

CREATE TABLE collectors (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT UNIQUE NOT NULL FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE,
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES recycling_enterprises(id),
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
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES recycling_enterprises(id),
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
    enterprise_id INT NOT NULL FOREIGN KEY REFERENCES recycling_enterprises(id),
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

-- ============================================
-- 13. LEADERBOARD
-- ============================================

CREATE TABLE leaderboard (
    id INT PRIMARY KEY IDENTITY(1,1),
    citizen_id INT NOT NULL FOREIGN KEY REFERENCES citizens(id),
    ward NVARCHAR(100),
    district NVARCHAR(100),
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
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE feedback_responses (
    id INT PRIMARY KEY IDENTITY(1,1),
    feedback_id INT NOT NULL FOREIGN KEY REFERENCES feedbacks(id) ON DELETE CASCADE,
    responder_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    response NVARCHAR(MAX) NOT NULL,
    is_internal BIT DEFAULT 0,
    attachments NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE()
);

-- ============================================
-- 15. COLLECTION STATISTICS
-- ============================================

CREATE TABLE collection_statistics (
    id INT PRIMARY KEY IDENTITY(1,1),
    stat_date DATE NOT NULL,
    enterprise_id INT FOREIGN KEY REFERENCES recycling_enterprises(id),
    waste_type_id INT NOT NULL FOREIGN KEY REFERENCES waste_types(id),
    district NVARCHAR(100),
    city NVARCHAR(100),
    total_reports INT DEFAULT 0,
    total_collections INT DEFAULT 0,
    total_weight_kg DECIMAL(12,2) DEFAULT 0,
    total_points_awarded INT DEFAULT 0,
    avg_collection_time_hours DECIMAL(5,2),
    success_rate DECIMAL(5,2),
    created_at DATETIME2 DEFAULT GETDATE()
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

-- 2. PERMISSIONS
INSERT INTO permissions (permission_code, permission_name, module, description) VALUES
-- Citizen
('report.create', N'Tạo báo cáo rác', 'report', N'Báo cáo rác với ảnh + GPS + mô tả'),
('report.view_own', N'Xem báo cáo của mình', 'report', N'Theo dõi trạng thái báo cáo'),
('report.classify_waste', N'Phân loại rác', 'report', N'Chọn loại rác khi tạo báo cáo'),
('points.view_own', N'Xem điểm thưởng', 'points', N'Xem lịch sử điểm thưởng'),
('leaderboard.view', N'Xem bảng xếp hạng', 'points', N'Xem xếp hạng theo khu vực'),
('feedback.create', N'Gửi phản hồi/khiếu nại', 'feedback', N'Khiếu nại khi thu gom không đúng'),
-- Enterprise
('enterprise.manage_capacity', N'Quản lý năng lực', 'enterprise', N'Đăng ký loại rác/công suất/khu vực'),
('enterprise.view_requests', N'Xem yêu cầu thu gom', 'collection', N'Xem yêu cầu trong phạm vi hoạt động'),
('enterprise.accept_reject', N'Chấp nhận/Từ chối', 'collection', N'Quyết định tiếp nhận yêu cầu'),
('enterprise.assign_collector', N'Phân công collector', 'collection', N'Gán yêu cầu cho nhân viên'),
('enterprise.track_realtime', N'Theo dõi thời gian thực', 'collection', N'Xem tiến độ và trạng thái'),
('enterprise.view_statistics', N'Xem báo cáo thống kê', 'statistics', N'Báo cáo khối lượng theo loại/khu vực/thời gian'),
('enterprise.manage_point_rules', N'Cấu hình quy tắc điểm', 'points', N'Tạo quy tắc tính điểm thưởng'),
-- Collector
('collection.view_assigned', N'Xem yêu cầu được gán', 'collection', N'Nhận yêu cầu từ Enterprise'),
('collection.update_status', N'Cập nhật trạng thái', 'collection', N'Assigned/On the way/Collected'),
('collection.confirm_complete', N'Xác nhận hoàn thành', 'collection', N'Upload ảnh xác nhận'),
('collection.view_history', N'Xem lịch sử công việc', 'collection', N'Xem số lượng đã hoàn thành'),
-- Admin
('admin.manage_users', N'Quản lý tài khoản', 'admin', N'Quản lý tài khoản và phân quyền'),
('admin.monitor_system', N'Giám sát hệ thống', 'admin', N'Giám sát hoạt động tổng thể'),
('admin.resolve_disputes', N'Giải quyết tranh chấp', 'feedback', N'Xử lý khiếu nại'),
('admin.view_all', N'Xem toàn bộ dữ liệu', 'admin', N'Truy cập tất cả báo cáo/thống kê');

-- 3. ROLE_PERMISSIONS
-- Citizen (role_id = 1)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6);
-- Collector (role_id = 2)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 14), (2, 15), (2, 16), (2, 17);
-- Enterprise (role_id = 3)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12), (3, 13);
-- Admin (role_id = 4) - All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions;

-- 4. WASTE TYPES
INSERT INTO waste_types (code, name, description, category, base_points, is_recyclable) VALUES
('PLASTIC', N'Nhựa tái chế', N'Chai nhựa, hộp nhựa PET, HDPE, PP', 'plastic', 10, 1),
('PAPER', N'Giấy & Carton', N'Giấy báo, hộp giấy, bìa carton', 'paper', 8, 1),
('METAL', N'Kim loại', N'Lon nhôm, sắt vụn, đồng, inox', 'metal', 15, 1),
('GLASS', N'Thủy tinh', N'Chai lọ thủy tinh các loại', 'glass', 12, 1),
('ELECTRONIC', N'Rác điện tử', N'Thiết bị điện tử cũ, pin, bóng đèn', 'electronic', 20, 1),
('ORGANIC', N'Hữu cơ', N'Rác thực phẩm, lá cây, rau quả', 'organic', 5, 0),
('HAZARDOUS', N'Nguy hại', N'Hóa chất, thuốc trừ sâu, pin lithium', 'hazardous', 0, 0);

-- 5. RECYCLING ENTERPRISES
INSERT INTO recycling_enterprises (name, address, phone, email, license_number, tax_code) VALUES
(N'Công ty TNHH Tái chế Xanh', N'123 Nguyễn Văn Linh, Q.7, TP.HCM', '028-1234-5678', 'info@taichexanh.vn', 'GP-2024-001', '0312345678'),
(N'Công ty CP Môi trường Sạch', N'456 Lê Văn Việt, Q.9, TP.HCM', '028-8765-4321', 'contact@moitruongsach.vn', 'GP-2024-002', '0387654321'),
(N'Doanh nghiệp Tái chế Phương Nam', N'789 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '028-5555-6666', 'phuongnam@recycle.vn', 'GP-2024-003', '0355556666');

-- 6. USERS
-- Password hash = 'password123' (bcrypt)
INSERT INTO users (email, password_hash, full_name, phone, role_id, status) VALUES
-- Admin
('admin@wastemanagement.vn', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Nguyễn Văn Admin', '0901234567', 4, 'active'),
-- Enterprise Admins
('manager1@taichexanh.vn', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Trần Thị Lan', '0902345678', 3, 'active'),
('manager2@moitruongsach.vn', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Lê Văn Hùng', '0903456789', 3, 'active'),
-- Collectors
('collector1@taichexanh.vn', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Phạm Văn Minh', '0904567890', 2, 'active'),
('collector2@taichexanh.vn', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Ngô Thị Hoa', '0905678901', 2, 'active'),
('collector3@moitruongsach.vn', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Hoàng Văn Nam', '0906789012', 2, 'active'),
-- Citizens
('citizen1@gmail.com', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Võ Thị Mai', '0907890123', 1, 'active'),
('citizen2@gmail.com', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Đặng Văn Tùng', '0908901234', 1, 'active'),
('citizen3@gmail.com', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Bùi Thị Hương', '0909012345', 1, 'active'),
('citizen4@gmail.com', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Lý Văn Đức', '0900123456', 1, 'active'),
('citizen5@gmail.com', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', N'Trịnh Thị Nga', '0911234567', 1, 'active');

-- 7. ENTERPRISE ADMINS
INSERT INTO enterprise_admins (user_id, enterprise_id, position, is_owner) VALUES
(2, 1, N'Giám đốc điều hành', 1),
(3, 2, N'Quản lý vận hành', 1);

-- 8. COLLECTORS
INSERT INTO collectors (user_id, enterprise_id, employee_code, vehicle_type, vehicle_plate, status) VALUES
(4, 1, 'COL-001', N'Xe tải nhỏ', '59C-12345', 'available'),
(5, 1, 'COL-002', N'Xe ba gác', '59B-67890', 'available'),
(6, 2, 'COL-003', N'Xe máy', '59P1-11111', 'available');

-- 9. CITIZENS
INSERT INTO citizens (user_id, address, ward, district, city, total_points, total_reports, valid_reports) VALUES
(7, N'12 Nguyễn Huệ', N'Phường Bến Nghé', N'Quận 1', N'TP.HCM', 150, 12, 10),
(8, N'45 Lê Lợi', N'Phường Bến Thành', N'Quận 1', N'TP.HCM', 280, 25, 22),
(9, N'78 Hai Bà Trưng', N'Phường Tân Định', N'Quận 1', N'TP.HCM', 95, 8, 7),
(10, N'100 Điện Biên Phủ', N'Phường 15', N'Quận Bình Thạnh', N'TP.HCM', 320, 30, 28),
(11, N'200 Nguyễn Thị Minh Khai', N'Phường 6', N'Quận 3', N'TP.HCM', 180, 15, 14);

-- 10. ENTERPRISE WASTE CAPACITY
INSERT INTO enterprise_waste_capacity (enterprise_id, waste_type_id, daily_capacity_kg, current_load_kg, price_per_kg, is_active) VALUES
-- Công ty Tái chế Xanh
(1, 1, 5000, 1200, 5000, 1),   -- Nhựa
(1, 2, 3000, 800, 3000, 1),    -- Giấy
(1, 3, 2000, 500, 15000, 1),   -- Kim loại
(1, 4, 1000, 200, 2000, 1),    -- Thủy tinh
-- Công ty Môi trường Sạch
(2, 1, 4000, 900, 4800, 1),    -- Nhựa
(2, 2, 2500, 600, 2800, 1),    -- Giấy
(2, 5, 500, 100, 50000, 1),    -- Điện tử
-- Doanh nghiệp Phương Nam
(3, 3, 3000, 700, 14000, 1),   -- Kim loại
(3, 4, 2000, 400, 1800, 1);    -- Thủy tinh

-- 11. ENTERPRISE SERVICE AREAS
INSERT INTO enterprise_service_areas (enterprise_id, city, district, ward, priority_score, max_distance_km, is_active) VALUES
-- Công ty Tái chế Xanh - Phục vụ Q.1, Q.3, Q.7
(1, N'TP.HCM', N'Quận 1', NULL, 10, 15, 1),
(1, N'TP.HCM', N'Quận 3', NULL, 8, 12, 1),
(1, N'TP.HCM', N'Quận 7', NULL, 10, 10, 1),
-- Công ty Môi trường Sạch - Phục vụ Q.9, Q.Thủ Đức, Q.Bình Thạnh
(2, N'TP.HCM', N'Quận 9', NULL, 10, 15, 1),
(2, N'TP.HCM', N'Quận Thủ Đức', NULL, 9, 12, 1),
(2, N'TP.HCM', N'Quận Bình Thạnh', NULL, 8, 10, 1),
-- Doanh nghiệp Phương Nam - Phục vụ Q.Bình Thạnh, Q.Phú Nhuận
(3, N'TP.HCM', N'Quận Bình Thạnh', NULL, 10, 10, 1),
(3, N'TP.HCM', N'Quận Phú Nhuận', NULL, 9, 8, 1);

-- 12. WASTE REPORTS
INSERT INTO waste_reports (report_code, citizen_id, waste_type_id, description, estimated_weight_kg, latitude, longitude, address, ward, district, city, images, ai_suggested_type_id, ai_confidence, status, is_valid, points_awarded, quality_rating, created_at) VALUES
('WR-20250115-001', 1, 1, N'Nhiều chai nhựa PET đã rửa sạch', 5.5, 10.7769, 106.7009, N'12 Nguyễn Huệ, Q.1', N'Phường Bến Nghé', N'Quận 1', N'TP.HCM', '["https://storage.example.com/img1.jpg"]', 1, 95.5, 'collected', 1, 55, 5, '2025-01-15 08:30:00'),
('WR-20250115-002', 2, 2, N'Thùng carton và giấy báo cũ', 8.0, 10.7731, 106.6989, N'45 Lê Lợi, Q.1', N'Phường Bến Thành', N'Quận 1', N'TP.HCM', '["https://storage.example.com/img2.jpg"]', 2, 92.0, 'collected', 1, 64, 4, '2025-01-15 09:15:00'),
('WR-20250115-003', 3, 3, N'Lon nhôm và sắt vụn', 3.2, 10.7856, 106.6912, N'78 Hai Bà Trưng, Q.1', N'Phường Tân Định', N'Quận 1', N'TP.HCM', '["https://storage.example.com/img3.jpg"]', 3, 88.5, 'assigned', 1, 0, NULL, '2025-01-15 10:00:00'),
('WR-20250115-004', 4, 1, N'Chai nhựa HDPE từ nước giặt', 4.0, 10.8012, 106.7102, N'100 Điện Biên Phủ, Bình Thạnh', N'Phường 15', N'Quận Bình Thạnh', N'TP.HCM', '["https://storage.example.com/img4.jpg"]', 1, 90.0, 'accepted', 1, 0, NULL, '2025-01-15 11:30:00'),
('WR-20250115-005', 5, 4, N'Chai lọ thủy tinh các loại', 6.5, 10.7823, 106.6845, N'200 Nguyễn Thị Minh Khai, Q.3', N'Phường 6', N'Quận 3', N'TP.HCM', '["https://storage.example.com/img5.jpg"]', 4, 94.2, 'pending', NULL, 0, NULL, '2025-01-15 14:00:00'),
('WR-20250116-001', 1, 5, N'Điện thoại cũ và pin', 1.2, 10.7769, 106.7009, N'12 Nguyễn Huệ, Q.1', N'Phường Bến Nghé', N'Quận 1', N'TP.HCM', '["https://storage.example.com/img6.jpg"]', 5, 85.0, 'pending', NULL, 0, NULL, '2025-01-16 09:00:00'),
('WR-20250116-002', 2, 1, N'Chai nhựa PP từ hộp sữa', 3.5, 10.7731, 106.6989, N'45 Lê Lợi, Q.1', N'Phường Bến Thành', N'Quận 1', N'TP.HCM', '["https://storage.example.com/img7.jpg"]', 1, 91.5, 'pending', NULL, 0, NULL, '2025-01-16 10:30:00');

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

-- 17. LEADERBOARD
INSERT INTO leaderboard (citizen_id, ward, district, city, period_type, period_start, period_end, total_points, total_reports, valid_reports, total_weight_kg, rank_position) VALUES
(4, N'Phường 15', N'Quận Bình Thạnh', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 320, 30, 28, 125.5, 1),
(2, N'Phường Bến Thành', N'Quận 1', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 280, 25, 22, 98.2, 2),
(5, N'Phường 6', N'Quận 3', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 180, 15, 14, 65.0, 3),
(1, N'Phường Bến Nghé', N'Quận 1', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 150, 12, 10, 52.3, 4),
(3, N'Phường Tân Định', N'Quận 1', N'TP.HCM', 'monthly', '2025-01-01', '2025-01-31', 95, 8, 7, 35.8, 5);

-- 18. FEEDBACKS
INSERT INTO feedbacks (feedback_code, citizen_id, collection_request_id, feedback_type, subject, content, severity, status, assigned_to, created_at) VALUES
('FB-20250115-001', 1, 1, 'feedback', N'Dịch vụ tốt', N'Nhân viên thu gom rất nhiệt tình và đúng giờ. Cảm ơn!', 'low', 'resolved', 1, '2025-01-15 11:00:00'),
('FB-20250115-002', 3, NULL, 'complaint', N'Chờ lâu', N'Báo cáo đã 2 ngày nhưng chưa có ai đến thu gom', 'high', 'reviewing', 1, '2025-01-15 15:00:00'),
('FB-20250116-001', 2, 2, 'suggestion', N'Đề xuất thêm điểm', N'Đề xuất tăng điểm thưởng cho rác điện tử vì khó thu gom', 'normal', 'pending', NULL, '2025-01-16 09:30:00');

-- 19. FEEDBACK RESPONSES
INSERT INTO feedback_responses (feedback_id, responder_id, response, is_internal, created_at) VALUES
(1, 1, N'Cảm ơn bạn đã phản hồi tích cực! Chúng tôi sẽ tiếp tục cải thiện dịch vụ.', 0, '2025-01-15 14:00:00'),
(2, 1, N'Đang kiểm tra với đơn vị thu gom. Sẽ phản hồi trong 24h.', 0, '2025-01-15 16:00:00'),
(2, 1, N'Lưu ý: Khu vực Q.1 đang quá tải, cần điều phối thêm collector.', 1, '2025-01-15 16:05:00');

-- 20. COLLECTION STATISTICS
INSERT INTO collection_statistics (stat_date, enterprise_id, waste_type_id, district, city, total_reports, total_collections, total_weight_kg, total_points_awarded, avg_collection_time_hours, success_rate) VALUES
('2025-01-15', 1, 1, N'Quận 1', N'TP.HCM', 5, 4, 25.5, 180, 2.5, 95.00),
('2025-01-15', 1, 2, N'Quận 1', N'TP.HCM', 3, 3, 18.2, 120, 2.0, 100.00),
('2025-01-15', 1, 3, N'Quận 1', N'TP.HCM', 2, 1, 8.5, 75, 3.0, 85.00),
('2025-01-15', 2, 1, N'Quận Bình Thạnh', N'TP.HCM', 4, 3, 15.8, 95, 2.8, 90.00),
('2025-01-14', 1, 1, N'Quận 1', N'TP.HCM', 6, 5, 30.2, 210, 2.3, 92.00),
('2025-01-14', 1, 2, N'Quận 1', N'TP.HCM', 4, 4, 22.0, 145, 1.8, 100.00);


-- 22. SYSTEM SETTINGS
INSERT INTO system_settings (setting_key, setting_value, data_type, category, description) VALUES
('max_pending_hours', '72', 'number', 'report', N'Thời gian tối đa báo cáo ở trạng thái pending (giờ)'),
('collection_timeout_hours', '24', 'number', 'collection', N'Timeout cho yêu cầu thu gom (giờ)'),
('default_point_multiplier', '1.0', 'number', 'points', N'Hệ số nhân điểm mặc định'),
('max_distance_km', '10', 'number', 'collection', N'Khoảng cách tối đa cho thu gom (km)'),
('enable_ai_classification', 'true', 'boolean', 'ai', N'Bật AI hỗ trợ phân loại rác'),
('ai_min_confidence', '0.75', 'number', 'ai', N'Độ tin cậy tối thiểu của AI (0-1)'),
('leaderboard_min_reports', '5', 'number', 'points', N'Số báo cáo tối thiểu để lên bảng xếp hạng');


GO