USE master;
GO

IF EXISTS (SELECT 1 FROM sys.databases WHERE name = N'Waste')
BEGIN
    ALTER DATABASE Waste SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE Waste;
END
GO

CREATE DATABASE Waste;
GO

USE Waste;
GO

-- Roles
CREATE TABLE roles (
    id INT IDENTITY(1,1) NOT NULL,
    role_code NVARCHAR(20) NOT NULL,
    role_name NVARCHAR(50) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_role_code UNIQUE (role_code)
);
GO

-- Permissions
CREATE TABLE permissions (
    id INT IDENTITY(1,1) NOT NULL,
    permission_code NVARCHAR(100) NOT NULL,
    permission_name NVARCHAR(255) NOT NULL,
    module NVARCHAR(50) NOT NULL,
    description NVARCHAR(500) NULL,
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uq_permissions_permission_code UNIQUE (permission_code)
);
GO

-- Role Permissions
CREATE TABLE role_permissions (
    id INT IDENTITY(1,1) NOT NULL,
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (id),
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);
GO

-- Enterprise
CREATE TABLE enterprise (
    id INT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(255) NOT NULL,
    address NVARCHAR(500) NULL,
    phone NVARCHAR(20) NULL,
    email NVARCHAR(255) NULL,
    service_wards NVARCHAR(MAX) NULL,
    service_cities NVARCHAR(MAX) NULL,
    status NVARCHAR(20) NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    CONSTRAINT pk_enterprise PRIMARY KEY (id)
);
GO

-- Users
CREATE TABLE users (
    id INT IDENTITY(1,1) NOT NULL,
    email NVARCHAR(255) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20) NULL,
    role_id INT NOT NULL,
    enterprise_id INT NULL,
    status NVARCHAR(20) NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_enterprise_id UNIQUE (enterprise_id),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_users_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise(id)
);
GO

-- Citizens
CREATE TABLE citizens (
    id INT IDENTITY(1,1) NOT NULL,
    user_id INT NOT NULL,
    email NVARCHAR(255) NULL,
    full_name NVARCHAR(255) NULL,
    password_hash NVARCHAR(255) NULL,
    address NVARCHAR(500) NULL,
    phone NVARCHAR(20) NULL,
    ward NVARCHAR(100) NULL,
    city NVARCHAR(100) NULL,
    total_points INT NULL,
    total_reports INT NULL,
    valid_reports INT NULL,
    CONSTRAINT pk_citizens PRIMARY KEY (id),
    CONSTRAINT uq_citizens_user_id UNIQUE (user_id),
    CONSTRAINT fk_citizens_user FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

-- Waste Categories
CREATE TABLE waste_categories (
    id INT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500) NULL,
    unit NVARCHAR(20) NULL,
    point_per_unit DECIMAL(19,4) NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    CONSTRAINT pk_waste_categories PRIMARY KEY (id),
    CONSTRAINT uq_waste_categories_name UNIQUE (name)
);
GO

-- Waste Reports
CREATE TABLE waste_reports (
    id INT IDENTITY(1,1) NOT NULL,
    report_code NVARCHAR(50) NOT NULL,
    citizen_id INT NOT NULL,
    description NVARCHAR(1000) NULL,
    waste_type NVARCHAR(20) NOT NULL,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    address NVARCHAR(500) NULL,
    images NVARCHAR(MAX) NULL,
    cloudinary_public_id NVARCHAR(255) NULL,
    status NVARCHAR(20) NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    accepted_at DATETIME2 NULL,
    rejection_reason NVARCHAR(500) NULL,
    CONSTRAINT pk_waste_reports PRIMARY KEY (id),
    CONSTRAINT uq_waste_reports_report_code UNIQUE (report_code),
    CONSTRAINT fk_waste_reports_citizen FOREIGN KEY (citizen_id) REFERENCES citizens(id)
);
GO

-- Waste Report Items
CREATE TABLE waste_report_items (
    id INT IDENTITY(1,1) NOT NULL,
    report_id INT NOT NULL,
    waste_category_id INT NOT NULL,
    quantity DECIMAL(19,4) NULL,
    unit_snapshot NVARCHAR(20) NULL,
    created_at DATETIME2 NULL,
    CONSTRAINT pk_waste_report_items PRIMARY KEY (id),
    CONSTRAINT uq_waste_report_items UNIQUE (report_id, waste_category_id),
    CONSTRAINT fk_waste_report_items_report FOREIGN KEY (report_id) REFERENCES waste_reports(id),
    CONSTRAINT fk_waste_report_items_category FOREIGN KEY (waste_category_id) REFERENCES waste_categories(id)
);
GO

-- Report Images
CREATE TABLE report_images (
    id INT IDENTITY(1,1) NOT NULL,
    report_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    image_type NVARCHAR(20) NULL,
    uploaded_at DATETIME2 NULL CONSTRAINT df_report_images_uploaded_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_report_images PRIMARY KEY (id),
    CONSTRAINT fk_report_images_report FOREIGN KEY (report_id) REFERENCES waste_reports(id)
);
GO

-- Collectors
CREATE TABLE collectors (
    id INT IDENTITY(1,1) NOT NULL,
    user_id INT NOT NULL,
    enterprise_id INT NOT NULL,
    email NVARCHAR(255) NULL,
    full_name NVARCHAR(255) NULL,
    employee_code NVARCHAR(50) NULL,
    vehicle_type NVARCHAR(50) NULL,
    vehicle_plate NVARCHAR(20) NULL,
    status NVARCHAR(20) NULL,
    current_latitude DECIMAL(10,8) NULL,
    current_longitude DECIMAL(11,8) NULL,
    last_location_update DATETIME2 NULL,
    violation_count INT NULL,
    created_at DATETIME2 NULL,
    CONSTRAINT pk_collectors PRIMARY KEY (id),
    CONSTRAINT uq_collectors_user_id UNIQUE (user_id),
    CONSTRAINT fk_collectors_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_collectors_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise(id)
);
GO

-- Collection Requests
CREATE TABLE collection_requests (
    id INT IDENTITY(1,1) NOT NULL,
    request_code NVARCHAR(20) NOT NULL,
    report_id INT NOT NULL,
    enterprise_id INT NOT NULL,
    collector_id INT NULL,
    status NVARCHAR(20) NULL,
    rejection_reason NVARCHAR(500) NULL,
    assigned_at DATETIME2 NULL,
    accepted_at DATETIME2 NULL,
    started_at DATETIME2 NULL,
    actual_weight_kg DECIMAL(10,2) NULL,
    collected_at DATETIME2 NULL,
    completed_at DATETIME2 NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    sla_violated BIT NULL,
    CONSTRAINT pk_collection_requests PRIMARY KEY (id),
    CONSTRAINT uq_collection_requests_request_code UNIQUE (request_code),
    CONSTRAINT fk_collection_requests_report FOREIGN KEY (report_id) REFERENCES waste_reports(id),
    CONSTRAINT fk_collection_requests_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise(id),
    CONSTRAINT fk_collection_requests_collector FOREIGN KEY (collector_id) REFERENCES collectors(id)
);
GO

-- Collection Tracking
CREATE TABLE collection_tracking (
    id INT IDENTITY(1,1) NOT NULL,
    collection_request_id INT NOT NULL,
    collector_id INT NOT NULL,
    action NVARCHAR(50) NOT NULL,
    latitude DECIMAL(10,8) NULL,
    longitude DECIMAL(11,8) NULL,
    note NVARCHAR(500) NULL,
    created_at DATETIME2 NULL,
    CONSTRAINT pk_collection_tracking PRIMARY KEY (id),
    CONSTRAINT fk_collection_tracking_request FOREIGN KEY (collection_request_id) REFERENCES collection_requests(id),
    CONSTRAINT fk_collection_tracking_collector FOREIGN KEY (collector_id) REFERENCES collectors(id)
);
GO

CREATE TABLE collector_reports (
    id INT IDENTITY(1,1) NOT NULL,
    report_code NVARCHAR(20) NULL,
    collection_request_id INT NOT NULL,
    collector_id INT NOT NULL,
    status NVARCHAR(20) NOT NULL,
    collector_note NVARCHAR(1000) NULL,
    total_point INT NULL,
    collected_at DATETIME2 NULL,
    latitude DECIMAL(10,8) NULL,
    longitude DECIMAL(11,8) NULL,
    created_at DATETIME2 NULL CONSTRAINT df_collector_reports_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_collector_reports PRIMARY KEY (id),
    CONSTRAINT uq_collector_reports_code UNIQUE (report_code),
    CONSTRAINT fk_collector_reports_request FOREIGN KEY (collection_request_id) REFERENCES collection_requests(id),
    CONSTRAINT fk_collector_reports_collector FOREIGN KEY (collector_id) REFERENCES collectors(id)
);
GO

CREATE TABLE collector_report_images (
    id INT IDENTITY(1,1) NOT NULL,
    collector_report_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    image_public_id NVARCHAR(255) NULL,
    created_at DATETIME2 NULL CONSTRAINT df_collector_report_images_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_collector_report_images PRIMARY KEY (id),
    CONSTRAINT fk_collector_report_images_report FOREIGN KEY (collector_report_id) REFERENCES collector_reports(id)
);
GO

-- Collector Report Items
CREATE TABLE collector_report_items (
    id INT IDENTITY(1,1) NOT NULL,
    collector_report_id INT NOT NULL,
    waste_category_id INT NOT NULL,
    quantity DECIMAL(19,4) NOT NULL,
    unit_snapshot NVARCHAR(20) NOT NULL,
    point_per_unit_snapshot DECIMAL(19,4) NOT NULL,
    total_point INT NOT NULL,
    created_at DATETIME2 NULL,
    CONSTRAINT pk_collector_report_items PRIMARY KEY (id),
    CONSTRAINT fk_collector_report_items_report FOREIGN KEY (collector_report_id) REFERENCES collector_reports(id),
    CONSTRAINT fk_collector_report_items_category FOREIGN KEY (waste_category_id) REFERENCES waste_categories(id)
);
GO

-- Feedbacks
CREATE TABLE feedbacks (
    id INT IDENTITY(1,1) NOT NULL,
    feedback_code NVARCHAR(20) NOT NULL,
    citizen_id INT NOT NULL,
    collection_request_id INT NULL,
    feedback_type NVARCHAR(20) NOT NULL,
    subject NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    severity NVARCHAR(20) NULL,
    status NVARCHAR(20) NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    CONSTRAINT pk_feedbacks PRIMARY KEY (id),
    CONSTRAINT uq_feedbacks_feedback_code UNIQUE (feedback_code),
    CONSTRAINT fk_feedbacks_citizen FOREIGN KEY (citizen_id) REFERENCES citizens(id),
    CONSTRAINT fk_feedbacks_collection_request FOREIGN KEY (collection_request_id) REFERENCES collection_requests(id)
);
GO

-- Invalidated Tokens
CREATE TABLE invalidated_tokens (
    id NVARCHAR(255) NOT NULL,
    expiry_time DATETIME2 NULL,
    CONSTRAINT pk_invalidated_tokens PRIMARY KEY (id)
);
GO

-- Leaderboard
CREATE TABLE leaderboard (
    id INT IDENTITY(1,1) NOT NULL,
    citizen_id INT NOT NULL,
    ward NVARCHAR(100) NULL,
    city NVARCHAR(100) NULL,
    period_type NVARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_points INT NOT NULL,
    total_reports INT NOT NULL,
    valid_reports INT NOT NULL,
    total_weight_kg DECIMAL(10,2) NULL,
    rank_position INT NULL,
    updated_at DATETIME2 NULL,
    CONSTRAINT pk_leaderboard PRIMARY KEY (id),
    CONSTRAINT fk_leaderboard_citizen FOREIGN KEY (citizen_id) REFERENCES citizens(id)
);
GO

-- Point Rules
CREATE TABLE point_rules (
    id INT IDENTITY(1,1) NOT NULL,
    enterprise_id INT NOT NULL,
    rule_name NVARCHAR(255) NOT NULL,
    rule_type NVARCHAR(30) NOT NULL,
    min_weight_kg DECIMAL(10,2) NULL,
    max_weight_kg DECIMAL(10,2) NULL,
    min_quality_rating INT NULL,
    max_processing_hours INT NULL,
    base_points INT NOT NULL,
    multiplier DECIMAL(3,2) NULL,
    is_active BIT NULL,
    valid_from DATETIME2 NULL,
    valid_to DATETIME2 NULL,
    priority INT NULL,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    CONSTRAINT pk_point_rules PRIMARY KEY (id),
    CONSTRAINT fk_point_rules_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise(id)
);
GO

-- Point Transactions
CREATE TABLE point_transactions (
    id INT IDENTITY(1,1) NOT NULL,
    citizen_id INT NOT NULL,
    report_id INT NULL,
    collection_request_id INT NULL,
    rule_id INT NULL,
    points INT NOT NULL,
    transaction_type NVARCHAR(30) NOT NULL,
    description NVARCHAR(500) NULL,
    balance_after INT NOT NULL,
    created_by INT NULL,
    created_at DATETIME2 NULL,
    CONSTRAINT pk_point_transactions PRIMARY KEY (id),
    CONSTRAINT fk_point_transactions_citizen FOREIGN KEY (citizen_id) REFERENCES citizens(id),
    CONSTRAINT fk_point_transactions_report FOREIGN KEY (report_id) REFERENCES waste_reports(id),
    CONSTRAINT fk_point_transactions_collection_request FOREIGN KEY (collection_request_id) REFERENCES collection_requests(id),
    CONSTRAINT fk_point_transactions_rule FOREIGN KEY (rule_id) REFERENCES point_rules(id),
    CONSTRAINT fk_point_transactions_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);
GO

-- System Settings
CREATE TABLE system_settings (
    id INT IDENTITY(1,1) NOT NULL,
    setting_key NVARCHAR(100) NOT NULL,
    setting_value NVARCHAR(MAX) NULL,
    data_type NVARCHAR(20) NULL,
    category NVARCHAR(50) NULL,
    description NVARCHAR(500) NULL,
    updated_by INT NULL,
    updated_at DATETIME2 NULL,
    CONSTRAINT pk_system_settings PRIMARY KEY (id),
    CONSTRAINT uq_system_settings_setting_key UNIQUE (setting_key),
    CONSTRAINT fk_system_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users(id)
);
GO
