# Spring Security & Authorization (JWT) – Tài liệu dự án

## Tổng Quan
- Dự án sử dụng Spring Security 6 theo mô hình OAuth2 Resource Server để xác thực JWT một cách chuẩn và bảo mật.
- Token được tạo bằng jjwt (HS256), xác thực/giải mã token do Spring thực hiện tự động, hệ thống hoạt động ở chế độ stateless.
- Phân quyền dựa trên:
  - Role cấp cao (ROLE_CITIZEN, ROLE_COLLECTOR, ROLE_ENTERPRISE, ROLE_ENTERPRISE_ADMIN, ROLE_ADMIN)
  - Authority chi tiết (ví dụ: ASSIGN_COLLECTOR, MANAGE_USERS, …) lấy từ bảng Permission thông qua RolePermission.

## Kiến Trúc Bảo Mật
- Cấu hình chính: [SecurityConfig.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/SecurityConfig.java)
  - Kích hoạt oauth2ResourceServer để Spring tự kiểm tra JWT cho mọi request.
  - JwtDecoder (HS256) đọc secret từ cấu hình và giải mã token.
  - JwtAuthenticationConverter đọc claim `scope` để chuyển thành GrantedAuthority.
  - Stateless: không dùng session, mọi request dựa vào JWT.
- Tạo token: [JwtServiceImpl.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/impl/JwtServiceImpl.java)
  - Sinh Access Token/Refresh Token.
  - Đưa toàn bộ quyền (ROLE_… và permission) vào claim `scope` (chuỗi cách nhau bởi khoảng trắng).
- Nạp quyền người dùng: [UserPrincipal.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/UserPrincipal.java)
  - Từ Role → thêm `ROLE_{roleCode}`.
  - Từ RolePermission → thêm `{permissionCode}` (authority chi tiết).
- Quan hệ Role–Permission:
  - [Role.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/Role.java)
  - [Permission.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/Permission.java)
  - [RolePermission.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/RolePermission.java)

## Phân Quyền Theo URL
- Đặt tại SecurityConfig:
  - `/api/auth/**` → permitAll (login/register/refresh)
  - `/api/citizen/**` → hasRole("CITIZEN")
  - `/api/enterprise/**` → hasAnyRole("ENTERPRISE", "ENTERPRISE_ADMIN")
  - `/api/collector/**` → hasRole("COLLECTOR")
  - `/api/admin/**` → hasRole("ADMIN")

Ví dụ trong cấu hình: xem [SecurityConfig.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/SecurityConfig.java#L58-L64)

## Phân Quyền Theo Hàm (Annotation)
- Dùng `@PreAuthorize` trên Controller để kiểm tra Role/Authority chi tiết.
- Ví dụ:
  - Citizen: [WasteReportController.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/WasteReportController.java)
    - `@PreAuthorize("hasRole('CITIZEN')")`
  - Collector: [CollectionController.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/CollectionController.java)
    - `@PreAuthorize("hasRole('COLLECTOR')")`
  - Enterprise: [EnterpriseController.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/EnterpriseController.java)
    - `@PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")`
    - `@PreAuthorize("hasAuthority('ASSIGN_COLLECTOR')")`
  - Admin: [AdminController.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/AdminController.java)
    - `@PreAuthorize("hasRole('ADMIN')")`
    - `@PreAuthorize("hasAuthority('MANAGE_USERS')")`

## Luồng Token & Refresh
- Login/Register trả về: [AuthServiceImpl.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/AuthServiceImpl.java#L85-L114)
  - `AuthenResponse` gồm: access token, refresh token, token type, thời hạn, thông tin user.
- Refresh token:
  - Endpoint: `POST /api/auth/refresh-token` trong [AuthController.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/AuthController.java#L33-L42)
  - Logic: [AuthServiceImpl.refreshToken](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/AuthServiceImpl.java#L148-L183)
  - Trả về: [TokenResponse.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/dto/response/TokenResponse.java)

## Xử Lý Lỗi Tập Trung
- [GlobalHandleException.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/exception/GlobalHandleException.java)
  - Chuẩn hóa phản hồi lỗi cho ResponseStatusException, BadCredentialsException, lỗi hệ thống.

## Cấu Hình Bắt Buộc
- Các thuộc tính cần thiết (ví dụ trong application.yml hoặc biến môi trường):
  - `security.jwt.secret` – secret HS256 (tối thiểu 32 bytes, có thể Base64)
  - `security.jwt.expiration-ms` – thời hạn Access Token (ms)
  - `security.jwt.refresh-expiration-ms` – thời hạn Refresh Token (ms)
- JwtServiceImpl sẽ cảnh báo nếu secret không đủ mạnh.

## Cách Client Sử Dụng
- Đăng nhập (lấy token):
  - `POST /api/auth/login` với email/password
  - Nhận `token` (access), `refreshToken`
- Gọi API được bảo vệ:
  - Thêm header: `Authorization: Bearer {accessToken}`
- Làm mới token:
  - `POST /api/auth/refresh-token` với body: `{ "refreshToken": "..." }`
  - Nhận bộ token mới từ `TokenResponse`.

## Nguyên Tắc Phát Triển
- Mọi API mới:
  - Xác định nhóm người dùng → đặt rule trong SecurityConfig hoặc `@PreAuthorize`.
  - Nếu cần quyền chi tiết, tạo Permission tương ứng và gán vào Role qua RolePermission.
- DTO/Service/Controller:
  - Tách business logic vào Service; Controller chỉ điều phối và kiểm tra quyền.

---

## Vì Sao Thiết Kế Như Vậy (Rationale)

### 1) Sử dụng OAuth2 Resource Server thay vì Filter tự viết
- Chuẩn hóa theo Spring Security 6: giảm mã nguồn, tăng độ tin cậy, tối ưu hiệu năng vì Spring đã tối ưu chuỗi filter.
- Tự động kiểm tra chữ ký, thời hạn, và format JWT; giảm rủi ro bảo mật do implement sai.
- Đơn giản hóa test: Controller/Service test không phải giả lập thủ công quá nhiều tầng filter.
- Xem [SecurityConfig.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/SecurityConfig.java).

### 2) Lưu quyền vào claim `scope` trong JWT
- Stateless đúng nghĩa: tránh hit Database ở mỗi request để tra lại quyền; quyền được "đóng gói" vào token.
- Tương thích tiêu chuẩn OAuth2: nhiều công cụ/gateway mặc định đọc `scope`.
- Dễ mở rộng: thêm quyền mới chỉ cần đẩy vào claim khi phát hành token.
- Xem [JwtServiceImpl.generateToken](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/impl/JwtServiceImpl.java).

### 3) Kết hợp Role và Permission (fine-grained)
- Role kiểm soát đường dẫn lớn (module-level), Permission kiểm soát hành động chi tiết (function-level).
- Tránh “role explosion”: nhiều biến thể role chỉ để khác vài quyền nhỏ → dùng authority chi tiết linh hoạt hơn.
- Xem [UserPrincipal.getAuthorities](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/UserPrincipal.java).

### 4) `@PreAuthorize` tại Controller
- Rõ ràng tại điểm sử dụng: đọc code là thấy ngay yêu cầu quyền cho hành động đó.
- Không trộn lẫn với mapping URL: tách concerns giữa điều hướng (route) và nghiệp vụ (method).
- Dễ test đơn vị: có thể bật/tắt security filter và xác nhận logic cho từng endpoint.
- Ví dụ: [CollectionController.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/CollectionController.java) và [EnterpriseController.java](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/EnterpriseController.java).

### 5) Refresh Token tách riêng endpoint
- Bảo mật: Chỉ nhận Refresh Token, không cần access token; dễ đặt hạn xử lý và theo dõi.
- Trải nghiệm người dùng: Client có thể tự làm mới phiên một cách mượt mà.
- Linh hoạt triển khai: có thể thay đổi chiến lược cấp lại refresh token (rolling) hoặc giữ nguyên.
- Xem [AuthController.refreshToken](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/AuthController.java#L33-L42) và [AuthServiceImpl.refreshToken](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/AuthServiceImpl.java#L148-L183).

### 6) Yêu cầu secret mạnh và HS256
- Secret tối thiểu 32 bytes: giảm rủi ro brute force; enforce trong [JwtServiceImpl](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/impl/JwtServiceImpl.java).
- HS256 phù hợp backend single-origin: dễ triển khai, ít phức tạp hơn RSA khi không cần public key phân phối.
- Có thể nâng cấp RS256 khi nhiều dịch vụ cùng xác thực dựa trên public key (mở rộng tương lai).

### 7) CORS và WebSecurityCustomizer
- CORS giới hạn origins và headers giúp frontend phát triển thuận tiện mà vẫn an toàn.
- `WebSecurityCustomizer` bỏ qua swagger/actuator: tiện lợi debug, tránh gây nhiễu phân quyền.
- Xem [SecurityConfig.corsConfigurationSource](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/SecurityConfig.java#L112-L123) và [SecurityConfig.webSecurityCustomizer](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/security/SecurityConfig.java#L135-L140).

### 8) Transactional ở AuthServiceImpl
- Đăng ký (write) cần `@Transactional` để đảm bảo atomicity khi tạo user/role.
- Đăng nhập và refresh (read) dùng `readOnly = true` để tối ưu và tránh ghi nhầm.
- Xem [AuthServiceImpl](file:///d:/Documents/GIT_HUB/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/AuthServiceImpl.java).
