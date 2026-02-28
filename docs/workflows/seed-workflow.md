# Seed Workflow

## Mục đích
Khởi tạo dữ liệu phục vụ phát triển/demo: quyền, vai trò, người dùng, doanh nghiệp/collector, danh mục rác, luồng yêu cầu/giám sát, điểm thưởng, phản hồi.

## Bật/tắt & chế độ
- Bật seed: `app.seed.enabled=true` trong [application.yml](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/resources/application.yml#L1-L53)
- Chế độ modular: `app.seed.modular=true` để chạy các seed tách rời theo module

## Cơ chế chạy
Seed được thực thi khi ứng dụng khởi động. Khi bật modular, mỗi module được chạy riêng; nếu tắt modular, dùng trình seeder tổng hợp.

## Triển khai
- Seeder tổng hợp:
  - [DataSeeder.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/DataSeeder.java#L51-L158)
- Modular seeders:
  - Quyền & Vai trò: [SeedAuthInitializer.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SeedAuthInitializer.java#L14-L55)
  - Người dùng mẫu: [SeedUsersInitializer.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SeedUsersInitializer.java#L15-L44)
  - Doanh nghiệp & Collector: [SeedEnterpriseCollectorInitializer.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SeedEnterpriseCollectorInitializer.java#L17-L39)
  - Danh mục rác: [SeedWasteCatalogInitializer.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SeedWasteCatalogInitializer.java#L14-L40)
  - Luồng nghiệp vụ (request/tracking/report/feedback): [SeedWorkflowInitializer.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/SeedWorkflowInitializer.java#L21-L84)

## Khuyến nghị môi trường
- Dev/demo: bật `app.seed.enabled=true`; cân nhắc modular để kiểm soát.
- Prod: tắt seed để tránh ghi dữ liệu thử vào hệ thống thực.

## Lưu ý an toàn
- Tránh nhân bản dữ liệu mỗi lần khởi động; các seed nên kiểm tra tồn tại trước khi chèn.
- Tuyệt đối không chứa bí mật trong dữ liệu mẫu.

