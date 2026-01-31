# Spring Security & Authorization (JWT - Nimbus) – Tài liệu dự án

## Tổng Quan
- Dự án sử dụng Spring Security 6 kết hợp với thư viện Nimbus JOSE + JWT để triển khai xác thực và phân quyền dựa trên JSON Web Token (JWT).
- Hệ thống hoạt động ở chế độ Stateless (không lưu session), mọi request đều phải đi kèm với một JWT hợp lệ trong header `Authorization: Bearer <token>`.

## Kiến Trúc Bảo Mật

### 1. Cấu hình chính (SecurityConfig)
- **File**: `com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.SecurityConfig`
- **Chức năng**:
    - Định nghĩa các endpoint công khai (PUBLIC_ENDPOINTS) không cần xác thực (Login, Register, Swagger UI, ...).
    - Cấu hình OAuth2 Resource Server để tự động giải mã và xác thực JWT.
    - Cấu hình CORS để cho phép các ứng dụng frontend truy cập.
    - Vô hiệu hóa CSRF vì hệ thống là Stateless.
    - Cấu hình `JwtAuthenticationConverter` để chuyển đổi các scope trong JWT thành các Authority/Role của Spring Security.

### 2. Giải mã JWT (CustomJwtDecoder)
- **File**: `com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.CustomJwtDecoder`
- **Chức năng**:
    - Sử dụng `NimbusJwtDecoder` để giải mã Token.
    - Tích hợp với `AuthenticationService.introspect` để kiểm tra tính hợp lệ của Token trước khi giải mã (kiểm tra Token có bị thu hồi/logout hay không).

### 3. Xử lý lỗi xác thực (JwtAuthenticationEntryPoint)
- **File**: `com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.JwtAuthenticationEntryPoint`
- **Chức năng**:
    - Tùy chỉnh phản hồi khi người dùng truy cập vào tài nguyên yêu cầu xác thực nhưng không cung cấp Token hoặc Token không hợp lệ.
    - Trả về cấu trúc `ApiResponse` đồng nhất với mã lỗi `UNAUTHENTICATED (1006)`.

## Dịch vụ xác thực (AuthenticationService)
- **File**: `com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthenticationService`
- **Nghiệp vụ chính**:
    - `authenticate`: Xác thực người dùng bằng email/password và sinh JWT.
    - `generateToken`: Tạo JWT sử dụng thuật toán HS512, chứa các thông tin như `sub` (email), `iss` (issuer), `iat` (issue time), `exp` (expiry time), `jti` (JWT ID), và `scope` (danh sách Roles & Permissions).
    - `introspect`: Kiểm tra Token có còn hiệu lực và chưa bị thu hồi (không nằm trong bảng `invalidated_tokens`).
    - `logout`: Thu hồi Token bằng cách lưu `jti` vào bảng `invalidated_tokens`.

## Phân quyền (Authorization)
- **Quy tắc**:
    - Sử dụng `@PreAuthorize` trên các Controller method.
    - Role được lưu trong JWT claim `scope` với tiền tố `ROLE_` (ví dụ: `ROLE_ADMIN`, `ROLE_CITIZEN`).
    - Quyền chi tiết (Permission) được lưu trực tiếp tên quyền (ví dụ: `ASSIGN_COLLECTOR`).
- **Ví dụ**:
    ```java
    @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAuthority('ASSIGN_COLLECTOR')")
    ```

## Cấu hình (application.yml)
```yaml
jwt:
  signerKey: "chuỗi-bí-mật-độ-dài-tối-thiểu-32-byte"
  valid-duration: 3600 # Thời gian hiệu lực token (giây)
```

## Các API Bảo mật chính
- `POST /api/auth/token`: Đăng nhập lấy Token (Sử dụng AuthenticationRequest).
- `POST /api/auth/login`: Đăng nhập (Sử dụng LoginRequest, trả về ApiResponse).
- `POST /api/auth/register`: Đăng ký tài khoản mới (trả về ApiResponse).
- `POST /api/auth/introspect`: Kiểm tra trạng thái Token (trả về ApiResponse).
- `POST /api/auth/logout`: Đăng xuất (vô hiệu hóa Token).

## Vai trò trong hệ thống (Roles)
Theo khung **C.O.R.G.I**, hệ thống hỗ trợ 4 vai trò chính:
1. **CITIZEN**: Người dân báo cáo rác và nhận thưởng.
2. **ENTERPRISE**: Doanh nghiệp tái chế điều phối thu gom.
3. **COLLECTOR**: Đơn vị/nhân viên thu gom thực hiện công việc.
4. **ADMIN**: Quản trị viên giám sát hệ thống.

Mọi tài khoản khi đăng ký mặc định sẽ là `CITIZEN` nếu không được chỉ định vai trò cụ thể.
