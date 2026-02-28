# Kế hoạch: Kiến thức dự án chi tiết từng thành phần được dùng

Mục tiêu: xây dựng tài liệu “Kiến Thức Dự Án” chi tiết, mô tả từng công nghệ/thư viện/thành phần đã sử dụng, cách áp dụng trong codebase, vị trí mã nguồn liên quan và cấu hình vận hành.

## Phạm vi
- Bao quát các lớp nền tảng (Spring Boot, Security, Scheduling), tầng dữ liệu (JPA, Entity, Repository), tầng dịch vụ, controller, tự động hóa (cron), và thành phần hỗ trợ (MapStruct, ModelMapper, Lombok, OpenAPI, Cloudinary, Commons IO, JDBC SQL Server).
- Không thay đổi chức năng hệ thống; chỉ biên soạn tài liệu và chèn liên kết “Code Reference”.

## Đầu ra
- Cập nhật/hoàn thiện tài liệu: `docs/kien-thuc-du-an.md` với cấu trúc chi tiết “từng cái dùng trong dự án”.
- Mục lục rõ ràng; mỗi thành phần có: Mô tả, Nơi dùng, Cách cấu hình, Code Reference, Lưu ý/pitfalls.

## Nguồn dữ liệu & điểm neo
- Entry point & Scheduler: [CrowdsourcedWasteCollectionRecyclingSystemApplication.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/CrowdsourcedWasteCollectionRecyclingSystemApplication.java)
- Tự động hóa: [TaskAutomationServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/TaskAutomationServiceImpl.java)
- Entity & trạng thái:
  - [Collector.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/Collector.java)
  - [CollectorStatus.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/enums/CollectorStatus.java)
- Repository:
  - [CollectorRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectorRepository.java)
  - [CollectionRequestRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectionRequestRepository.java)
  - [PointRuleRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/PointRuleRepository.java)
- Dịch vụ:
  - [CollectorService.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/CollectorService.java)
  - [CollectorServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorServiceImpl.java)
  - [EnterpriseAssignmentService.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/EnterpriseAssignmentService.java)
  - [EnterpriseAssignmentServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/EnterpriseAssignmentServiceImpl.java)
  - [CollectorReportService.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/CollectorReportService.java)
  - [CollectorReportServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorReportServiceImpl.java)
- Cấu hình quy tắc làm việc: [WorkRuleProperties.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/WorkRuleProperties.java)
- Mapper/DTO đại diện: [CollectorMapper.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/mapper/CollectorMapper.java)
- Phụ thuộc & công nghệ: `pom.xml`

## Dàn ý tài liệu chi tiết
1. Tổng quan dự án
   - Kiến trúc lớp: Controller → Service → Repository → DB; Automation Scheduler.
   - Quy ước đặt tên, package, kiểu trả về DTO.
2. Nền tảng & khởi động
   - Spring Boot: Auto-configuration, @SpringBootApplication, profile.
   - Scheduling: @EnableScheduling, tần suất, tác vụ định kỳ hiện có.
3. Bảo mật
   - Spring Security & OAuth2 Resource Server (JWT): mục đích, phạm vi bảo vệ, cấu hình chung.
   - Vai trò và ủy quyền ở controller/service (mô tả nơi áp dụng).
4. Dữ liệu & JPA
   - Entities chính (Collector, CollectionRequest, WasteReport, PointRule, Enterprise, User…).
   - Repository pattern, truy vấn đặc thù (timeout, SLA, eligible collectors).
   - Quy ước mapping, lazy/eager, precision tọa độ, thời gian.
5. Dịch vụ nghiệp vụ
   - Quản lý vòng đời yêu cầu thu gom (accept/start/reject/complete).
   - Gán collector theo điều kiện (khoảng cách, trạng thái, tải).
   - Báo cáo thu gom và tính điểm (PointRule).
6. Tự động hóa & Work Rules
   - Logic timeout nhận việc, SLA, treo collector.
   - Tái phân công trong giờ làm việc, bán kính tìm kiếm.
   - Cấu hình WorkRuleProperties và khóa YAML.
7. Hỗ trợ & Tiện ích
   - MapStruct/ModelMapper: vị trí mapper, đối tượng DTO phổ biến.
   - Lombok: chú ý @Getter/@Setter/@RequiredArgsConstructor.
   - OpenAPI: springdoc-starter, đường dẫn tài liệu.
   - Cloudinary & Commons IO: vai trò trong upload/xử lý file (liên kết đến service thực tế).
8. CSDL & JDBC
   - SQL Server JDBC, cấu hình datasource (tổng quan).
   - H2 test scope; chiến lược test.
9. API & Controller
   - Liệt kê controller chính (enterprise/collector), endpoints tiêu biểu, DTO vào/ra.
10. Môi trường & cấu hình
   - application.yml ví dụ cho workrule.*, security, datasource (ở mức hướng dẫn).
   - Lưu ý timezone, i18n (nếu có).
11. Pitfalls & khuyến nghị
   - Múi giờ trong cron.
   - Dữ liệu tọa độ thiếu làm hạn chế tái phân công.
   - Trạng thái collector và lọc eligible.

## Cách trình bày cho từng thành phần
- Mô tả: mục đích, lý do dùng.
- Ở đâu: lớp/đoạn mã tiêu biểu (kèm Code Reference).
- Cấu hình: khóa/yaml, annotation, properties.
- Lưu ý: ràng buộc/giới hạn, pitfall.
- Liên kết: đường dẫn file cụ thể.

## Quy trình thực hiện
1. Kiểm kê phụ thuộc từ `pom.xml` và nhóm theo chủ đề (nền tảng, dữ liệu, bảo mật, tiện ích).
2. Dò danh sách lớp liên quan cho từng thành phần; đối chiếu với code hiện có.
3. Viết từng mục theo mẫu trình bày, chèn Code Reference đầy đủ.
4. Thêm ví dụ cấu hình YAML cần thiết (đặc biệt workrule.*, security ở mức tham khảo).
5. Rà soát nội dung, kiểm thử liên kết file:/// trong IDE.

## Tiêu chuẩn chấp nhận
- Tài liệu có mục lục chi tiết, mỗi thành phần “được dùng trong dự án” có mô tả, nơi dùng, cấu hình và liên kết mã.
- Phản ánh đúng logic hiện hành (không giả định ngoài mã nguồn).
- Dễ tra cứu phục vụ onboarding/duy trì.

## Rủi ro & lưu ý
- Một số thành phần có thể chưa có cấu hình tùy biến rõ ràng (ví dụ Security) → ghi chú tình trạng thực tế và phạm vi áp dụng.
- Giới hạn múi giờ ảnh hưởng làm việc của cron → nêu rõ và khuyến nghị thiết lập TZ.

