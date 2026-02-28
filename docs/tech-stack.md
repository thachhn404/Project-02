# Kiến thức & Công nghệ sử dụng

## Nền tảng
- Java 24, Spring Boot 3.4.1 (Quản lý bằng Maven)
- Kiểu kiến trúc: Layered (Controller → Service → Repository → Entity)
- Lên lịch tác vụ: Spring Scheduling bật tại [CrowdsourcedWasteCollectionRecyclingSystemApplication.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/CrowdsourcedWasteCollectionRecyclingSystemApplication.java#L6-L15)

## Spring Starters & Thư viện chính
- Web: spring-boot-starter-web
- Dữ liệu: spring-boot-starter-data-jpa (Hibernate)
- Security: spring-boot-starter-security, spring-boot-starter-oauth2-resource-server (JWT Bearer)
- Validation: spring-boot-starter-validation
- Quan sát: spring-boot-starter-actuator
- Tài liệu API: springdoc-openapi-starter-webmvc-ui (Swagger UI)
- Mapping: MapStruct, ModelMapper
- Trợ giúp: Lombok

Tham chiếu dependency: [pom.xml](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/pom.xml#L1-L179)

## Cơ sở dữ liệu & ORM
- Runtime DB: Microsoft SQL Server (JDBC URL trong [application.yml](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/resources/application.yml#L1-L53))
- Test DB: H2 (scope test)
- ORM: JPA/Hibernate với `ddl-auto: update`

## Bảo mật
- Mô hình: OAuth2 Resource Server với JWT Bearer (HS512)
- Giải mã & kiểm tra thu hồi: [CustomJwtDecoder.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/CustomJwtDecoder.java#L37-L77)
- Cấu hình Security & CORS: [SecurityConfig.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SecurityConfig.java#L23-L118)
- Mã hóa mật khẩu: BCryptPasswordEncoder
- Hỗ trợ JWT: [JWTHelper.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/util/JWTHelper.java#L37-L125)

## Tài liệu API
- Swagger/OpenAPI: [SwaggerConfig.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SwaggerConfig.java#L16-L42)
- Đường dẫn mặc định: /swagger-ui/index.html

## Tích hợp bên thứ ba
- Cloudinary: cấu hình tại [CloudinaryConfig.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/CloudinaryConfig.java#L11-L24)

## Cấu hình ứng dụng
- Cấu hình tổng hợp tại [application.yml](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/resources/application.yml#L1-L53)
  - server.port, spring.datasource, spring.jpa, jwt.*, springdoc.*, app.seed.*, cloudinary.*

## Gợi ý thực hành
- Không commit secrets; dùng biến môi trường cho khóa JWT, thông tin Cloudinary/DB.
- Bật/tắt seed qua `app.seed.enabled` tùy môi trường.

