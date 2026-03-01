﻿﻿# Kiến Thức Dự Án

Tài liệu tóm tắt kiến trúc, domain và luồng nghiệp vụ của hệ thống Crowdsourced Waste Collection & Recycling.

## Tổng quan kỹ thuật
- Nền tảng: Spring Boot 3 (Java 24), Spring Data JPA, Spring Security (Resource Server JWT), Scheduling.
- Mapping: MapStruct và ModelMapper cho DTO/Entity mapping.
- CSDL: SQL Server (devtools và H2 test).
- Tài liệu API: OpenAPI/Swagger UI.
- Khởi động & Scheduling: [CrowdsourcedWasteCollectionRecyclingSystemApplication.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/CrowdsourcedWasteCollectionRecyclingSystemApplication.java).

## Domain & Entity cốt lõi
- Collector: nhân viên thu gom, trạng thái hoạt động và thống kê; có đếm vi phạm.
  - Entity: [Collector.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/Collector.java).
  - Trạng thái: [CollectorStatus.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/enums/CollectorStatus.java).
- CollectionRequest: yêu cầu thu gom sinh ra từ báo cáo chất thải, gắn với Collector, có trạng thái vòng đời và mốc thời gian.
- WasteReport: báo cáo chất thải (tọa độ, chi tiết).
- PointRule: luật tính điểm/khuyến khích theo doanh nghiệp.
  - Entity: [PointRule.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/PointRule.java).

## Repository & Truy vấn
- CollectorRepository: lọc collector theo enterprise, trạng thái và danh sách khả dụng.
  - [CollectorRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectorRepository.java)
- CollectionRequestRepository: nhận nhiệm vụ quá hạn, vi phạm SLA, truy vấn lịch sử/tác vụ của collector.
  - [CollectionRequestRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectionRequestRepository.java)
- PointRuleRepository: lấy luật active/hết hạn theo enterprise.
  - [PointRuleRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/PointRuleRepository.java)

## Dịch vụ nghiệp vụ
- Quản lý Collector & tác vụ thu gom:
  - Giao diện: [CollectorService.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/CollectorService.java)
  - Triển khai: [CollectorServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorServiceImpl.java)
- Gán Collector theo doanh nghiệp (xét trạng thái, khoảng cách, online, tải):
  - [EnterpriseAssignmentService.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/EnterpriseAssignmentService.java)
  - [EnterpriseAssignmentServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/EnterpriseAssignmentServiceImpl.java)
- Báo cáo thu gom & tính điểm:
  - [CollectorReportService.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/CollectorReportService.java)
  - [CollectorReportServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorReportServiceImpl.java)

## Tự động hóa & Work Rules
- Cron kiểm tra timeout nhận việc, vi phạm SLA và tái phân công trong giờ làm việc:
  - [TaskAutomationServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/TaskAutomationServiceImpl.java)
- Cấu hình luật làm việc tập trung:
  - [WorkRuleProperties.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/WorkRuleProperties.java)

## Luồng nghiệp vụ chính
1. Người dùng tạo WasteReport → hệ thống sinh CollectionRequest.
2. Doanh nghiệp gán Collector đáp ứng điều kiện.
3. Collector accept → start → complete; báo cáo thu gom được tạo và phê duyệt.
4. Hệ thống tính điểm dựa trên PointRule và ghi nhận giao dịch.
5. Cron giám sát timeout nhận việc và SLA; tăng vi phạm, treo collector nếu vượt ngưỡng và tái phân công khi trong giờ làm việc.

## Bảo mật & Phân quyền
- Spring Security Resource Server, xác thực JWT.
- Phân quyền theo vai trò (enterprise, collector, user) tại controller/service.

## API & Tài liệu
- Swagger UI có sẵn qua springdoc-openapi.
- Mapper: MapStruct/ModelMapper cho DTO trả ra client; ví dụ CollectorMapper, các DTO Create/Eligible/Assign.

## Điểm mở rộng
- Tối ưu thuật toán tái phân công: cân nhắc tải thực tế, điều kiện giao thông, ưu tiên theo lịch sử.
- Bổ sung cấu hình múi giờ, đa lịch làm việc theo doanh nghiệp/địa phương.
- Luật điểm thưởng linh hoạt hơn (ưu tiên phân loại rác, khu vực).

## Phụ lục: Công nghệ & Thư viện chi tiết
- spring-boot-starter-web
  - Dùng cho lớp REST controller, mapping HTTP.
  - Ví dụ: [EnterpriseCollectorController.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/enterprise/EnterpriseCollectorController.java)
- spring-boot-starter-data-jpa
  - Quản lý Entity và Repository, truy vấn đặc thù.
  - Ví dụ: [CollectorRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectorRepository.java)
- spring-boot-starter-security + spring-boot-starter-oauth2-resource-server
  - Xác thực/ủy quyền dựa trên JWT (Resource Server).
  - Endpoint xác thực: [AuthController.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/authentication/AuthController.java)
- Scheduling
  - Bật @EnableScheduling tại entrypoint; tác vụ @Scheduled trong tự động hóa.
  - [TaskAutomationServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/TaskAutomationServiceImpl.java)
- spring-boot-starter-validation
  - Hỗ trợ Bean Validation cho DTO (áp dụng khi khai báo @Valid trong controller/service).
- MapStruct
  - Mapper compile-time; ví dụ: [CollectorMapper.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/mapper/CollectorMapper.java)
- ModelMapper
  - Thư viện có trong phụ thuộc để map động; hiện chưa thấy tham chiếu trực tiếp trong code.
- Lombok
  - Giảm boilerplate (@Getter/@Setter/@RequiredArgsConstructor…); dùng rộng khắp entity/service/controller.
- springdoc-openapi-starter-webmvc-ui
  - Cấu hình OpenAPI + UI; xem [SwaggerConfig.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SwaggerConfig.java)
- Cloudinary HTTP44 + Commons IO
  - Upload/xoá ảnh: [CloudinaryConfig.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/CloudinaryConfig.java), [CloudinaryServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CloudinaryServiceImpl.java), [FileController.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/cloudinary/FileController.java)
- com.microsoft.sqlserver:mssql-jdbc
  - Trình điều khiển JDBC cho SQL Server.
- Devtools (runtime, optional)
  - Hỗ trợ hot reload trong môi trường dev.
- Kiểm thử
  - spring-boot-starter-test, h2, spring-security-test; ví dụ test Cloudinary: [CloudinaryServiceImplTest.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/test/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/CloudinaryServiceImplTest.java)

## Phụ lục: Controller & API nổi bật
- Xác thực/JWT: [AuthController.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/authentication/AuthController.java)
- Quản lý collector doanh nghiệp: [EnterpriseCollectorController.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/enterprise/EnterpriseCollectorController.java)
- Tải tệp/ảnh: [FileController.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/cloudinary/FileController.java)
- Giao diện cho collector: [CollectorController.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/collector/CollectorController.java)

## Phụ lục: Môi trường & Cấu hình
- Work Rules (application.yml):
  - workrule.accept-timeout-hours, workrule.sla-hours, workrule.suspend-threshold
  - workrule.working-start-hour, workrule.working-end-hour
  - workrule.reassign-radius-km
- Cloudinary:
  - cloudinary.cloud-name, cloudinary.api-key, cloudinary.api-secret, cloudinary.folder
- OpenAPI:
  - open.api.title, open.api.version, open.api.description
