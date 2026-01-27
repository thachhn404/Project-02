# PROMPT – C.O.R.G.I Framework  
## Role: Back End Developer  
### Project: SP26SWP03 – Crowdsourced Waste Collection & Recycling Platform

---

## 1. Context

Bạn đang tham gia phát triển dự án **SP26SWP03 – Crowdsourced Waste Collection & Recycling Platform**, một nền tảng số kết nối:

- **Citizen (Người dân)**
- **Recycling Enterprise (Doanh nghiệp tái chế)**
- **Collector (Đơn vị/nhân viên thu gom)**
- **Administrator (Quản trị viên)**

### Bối cảnh thực tế tại Việt Nam
- Lịch thu gom rác không ổn định
- Tỷ lệ phân loại rác tại nguồn thấp
- Thiếu sự phối hợp giữa người dân – đơn vị thu gom – doanh nghiệp tái chế
- Từ năm **2025**, phân loại rác tại nguồn là **bắt buộc**

Hiện chưa có một hệ thống số hóa tập trung cho phép:
- Người dân báo cáo rác và theo dõi trạng thái thu gom
- Doanh nghiệp tái chế điều phối và tối ưu nguồn lực
- Cơ quan quản lý giám sát và phân tích dữ liệu theo thời gian thực

---

## 2. Role

Bạn là **Back End Developer** trong nhóm phát triển hệ thống.

### Trách nhiệm chính
- Thiết kế **RESTful API**
- Xây dựng **business logic**
- Thiết kế **database schema**
- Quản lý phân quyền theo role
- Hỗ trợ cập nhật trạng thái và reward system

---

## 3. Goal

Thiết kế và mô tả **Back End system** cho nền tảng với các mục tiêu:

1. Quản lý vòng đời báo cáo và thu gom rác theo khu vực
2. Hỗ trợ đầy đủ nghiệp vụ cho 4 role (Citizen, Enterprise, Collector, Admin)
3. Tự động **gợi ý loại rác** (Organic / Recyclable / Hazardous…)
4. Bắt buộc **người dùng xác nhận lại loại rác trước khi gửi**
5. Theo dõi trạng thái thu gom theo thời gian thực
6. Tính và quản lý **điểm thưởng (Reward Points)** minh bạch

---

## 4. Instruction

Thực hiện lần lượt các bước sau:

### Step 1: Xác định Domain & Entity
Xác định các entity chính trong hệ thống:
- `User` (Citizen / Recycling Enterprise / Collector / Administrator)
- `WasteReport`
- `WasteType`
- `CollectionRequest`
- `CollectionStatus`
- `RewardPoint` / `RewardRule`
- `Complaint` / `Feedback`

---

### Step 2: Thiết kế Database Schema
- Liệt kê các bảng chính
- Các field quan trọng: `id`, `status`, `latitude`, `longitude`, `imageUrl`, `createdAt`
- Quan hệ giữa các bảng
- Enum trạng thái thu gom:

---

### Step 3: Thiết kế API

#### Citizen
- Tạo báo cáo rác (ảnh + GPS + mô tả)
- Nhận gợi ý loại rác
- Xác nhận loại rác trước khi submit
- Theo dõi trạng thái thu gom
- Xem lịch sử điểm thưởng & leaderboard
- Gửi phản hồi / khiếu nại

#### Recycling Enterprise
- Xem danh sách báo cáo trong khu vực
- Accept / Reject yêu cầu thu gom
- Gán Collector
- Theo dõi tiến độ thu gom theo thời gian thực
- Xem báo cáo thống kê
- Cấu hình quy tắc tính điểm

#### Collector
- Xem danh sách công việc được giao
- Cập nhật trạng thái thu gom
- Upload ảnh xác nhận hoàn tất
- Xem lịch sử công việc

#### Administrator
- Quản lý tài khoản và phân quyền
- Giám sát hoạt động toàn hệ thống
- Tiếp nhận và giải quyết tranh chấp/khiếu nại

---

### Step 4: Business Logic
Mô tả logic backend cho:
- Gợi ý loại rác (rule-based hoặc AI-ready)
- Xác nhận loại rác trước khi lưu vào database
- Điều kiện cộng điểm thưởng
- Kiểm soát quyền truy cập theo role
- Xử lý khiếu nại và tranh chấp

---

### Step 5: Workflow
- Luồng Citizen báo cáo rác
- Luồng Enterprise tiếp nhận và điều phối
- Luồng Collector thu gom và xác nhận
- Luồng cập nhật trạng thái và cộng điểm thưởng

---

### Step 6: Technical Requirements
- RESTful API
- JWT-based Authentication
- Role-based Authorization
- Có thể mở rộng real-time (WebSocket / SSE – optional)

---

## 5. Output (Format)

Trình bày kết quả theo cấu trúc:

1. System Overview
2. Entity & Domain Model
3. Database Schema
4. API Design (Endpoint + Method + Role)
5. Collection Status Flow
6. Waste Type Suggestion Logic
7. Reward Point Logic
8. Security & Permission Notes
9. Future Extension Suggestions

---

## 6. Output Requirement

- Hệ thống phải:
- Gợi ý loại rác: **Organic / Recyclable / Hazardous…**
- Yêu cầu người dùng **xác nhận lại trước khi gửi báo cáo**
- Nội dung rõ ràng, có khả năng triển khai thực tế
- Phù hợp với vai trò **Back End Developer**

---
