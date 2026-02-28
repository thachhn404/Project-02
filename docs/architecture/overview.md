# Kiến trúc tổng thể

## Tổng quan (C4 Context/Container – mô tả ngắn)
- Hệ thống CWCRS cung cấp API backend phục vụ ứng dụng quản lý và cộng đồng báo cáo/thu gom rác.
- Container chính: Spring Boot service; phụ thuộc SQL Server và dịch vụ lưu trữ media Cloudinary.
- Người dùng: công dân, doanh nghiệp, collector, admin; truy cập qua REST API được bảo vệ bằng JWT.

## Mô hình Layered
- Controller: nhận HTTP, mapping DTO, trả về phản hồi chuẩn.
  - [controller/](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller)
- Service: chứa logic nghiệp vụ, orchestration giữa repository và tích hợp.
  - [service/](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service)
- Repository: thao tác dữ liệu với JPA/Hibernate.
  - [repository/](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository)
- Entity/Enums: mô hình domain và giá trị liệt kê.
  - [entity/](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity)
  - [enums/](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/enums)
- Config & Security: cấu hình ứng dụng, Swagger, Cloudinary, Security.
  - [config/](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config)

## Luồng xử lý điển hình
1) HTTP request → Security filter chain (JWT) → Controller
2) Controller xác thực/validate → gọi Service
3) Service áp dụng logic nghiệp vụ → gọi Repository/JPA → SQL Server
4) Service dựng DTO/response → Controller trả JSON

## Bảo mật & CORS
- Resource Server với JWT Bearer; public endpoints cho auth và Swagger.
- Cấu hình tại [SecurityConfig.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SecurityConfig.java#L23-L118)
- JWT helper & decoder: [JWTHelper.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/util/JWTHelper.java#L37-L125), [CustomJwtDecoder.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/CustomJwtDecoder.java#L37-L77)

## Cấu hình & vận hành
- Ứng dụng: [application.yml](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/resources/application.yml#L1-L53)
  - server.port, spring.datasource (SQL Server), jpa, jwt, springdoc, app.seed.*, cloudinary.*
- Build/Dependencies: [pom.xml](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/pom.xml#L1-L179)
- Scheduling bật trong main: [CrowdsourcedWasteCollectionRecyclingSystemApplication.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/CrowdsourcedWasteCollectionRecyclingSystemApplication.java#L6-L15)

## Seed dữ liệu
- Bật qua `app.seed.enabled`; chế độ modular qua `app.seed.modular`.
- Seeder tổng hợp: [DataSeeder.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/DataSeeder.java#L51-L158)
- Modular seeders (quyền, người dùng, danh mục, luồng nghiệp vụ): xem thư mục [config/](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config)

## Tích hợp bên ngoài
- Cloudinary: tải/lưu trữ media qua [CloudinaryConfig.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/CloudinaryConfig.java#L11-L24)

