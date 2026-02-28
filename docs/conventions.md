# Quy ước & Lưu ý

## Quy ước đặt tên & cấu trúc
- Package Java khuyến nghị dùng chữ thường và dấu chấm, không dùng gạch dưới.
- Hiện tại có sự không nhất quán giữa tên package và thư mục (chữ hoa/gạch dưới). Cần lập kế hoạch chuẩn hóa để giảm rủi ro classpath.

## Phân tách lớp & chuyển đổi
- DTO tách khỏi Entity; Controller nhận/trả DTO, Service thao tác domain, Repository thao tác Entity.
- Mapping: ưu tiên MapStruct/ModelMapper.
  - Thư mục mapper: [mapper/](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/mapper)
  - Ví dụ MapStruct: [UserMapper.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/mapper/UserMapper.java#L9-L23)

## Bảo mật & phản hồi lỗi
- Sử dụng JWT Bearer; public endpoints giới hạn cho đăng nhập/đăng ký và Swagger.
- Entry point bảo mật trả JSON thống nhất khi 401:
  - [JwtAuthenticationEntryPoint.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/JwtAuthenticationEntryPoint.java#L26-L47)
- Không log thông tin nhạy cảm (token, password, keys).

## Tài liệu API
- Truy cập Swagger UI tại /swagger-ui/index.html.
- Cấu hình OpenAPI tại [SwaggerConfig.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SwaggerConfig.java#L16-L42)

## Cấu hình & bí mật
- Đặt biến môi trường cho `jwt.signerKey`, thông tin DB, Cloudinary.
- Không commit secrets vào repo; chỉ mô tả cách cấu hình trong tài liệu.

## Mã hoá định danh
- Report code: định dạng `WR` + 3 số dựa trên ID (ví dụ ID=7 → WR007). Nếu ID ≥ 1000, mã sẽ có hơn 3 số (ví dụ WR1001).
