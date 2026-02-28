﻿# Work Rules

Quy tắc làm việc điều khiển bởi cấu hình, áp dụng trên luồng tự động hóa nhiệm vụ (timeout nhận việc, SLA hoàn thành, tái phân công, treo collector).

## Mục tiêu
- Bảo đảm nhiệm vụ được tiếp nhận kịp thời.
- Cam kết hoàn thành trong SLA.
- Duy trì chất lượng bằng cơ chế vi phạm và treo tài khoản.
- Tự động tái phân công trong giờ làm việc để giảm trễ.

## Nguồn thực thi & liên kết
- Scheduler kích hoạt: [CrowdsourcedWasteCollectionRecyclingSystemApplication.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/CrowdsourcedWasteCollectionRecyclingSystemApplication.java)
- Luồng tự động hóa: [TaskAutomationServiceImpl.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/TaskAutomationServiceImpl.java)
- Trạng thái collector: [CollectorStatus.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/enums/CollectorStatus.java)
- Trường vi phạm: [Collector.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/Collector.java)
- Truy vấn timeout/SLA: [CollectionRequestRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectionRequestRepository.java)
- Lọc collector đủ điều kiện: [CollectorRepository.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectorRepository.java)
- Cấu hình Work Rules: [WorkRuleProperties.java](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/WorkRuleProperties.java)

## Cấu hình WorkRuleProperties
Các khóa cấu hình (prefix `workrule`) và ý nghĩa:
- accept-timeout-hours: số giờ tối đa ở trạng thái ASSIGNED trước khi bị coi là không nhận việc (mặc định 4).
- sla-hours: thời lượng tối đa để hoàn thành nhiệm vụ trước khi được đánh dấu vi phạm SLA (mặc định 72).
- suspend-threshold: số vi phạm tích lũy để treo collector (mặc định 3).
- working-start-hour: giờ bắt đầu khung làm việc để cho phép tái phân công (mặc định 7).
- working-end-hour: giờ kết thúc khung làm việc (mặc định 17).
- reassign-radius-km: bán kính tìm collector thay thế (km) (mặc định 10.0).

Ví dụ cấu hình trong application.yml:

```yaml
workrule:
  accept-timeout-hours: 4
  sla-hours: 72
  suspend-threshold: 3
  working-start-hour: 7
  working-end-hour: 17
  reassign-radius-km: 10.0
```

## Quy tắc chi tiết
1. Timeout nhận việc
   - Mốc: nhiệm vụ ở trạng thái ASSIGNED quá `accept-timeout-hours`.
   - Hệ quả: tăng `violationCount` của collector thêm 1.
   - Treo: nếu `violationCount` ≥ `suspend-threshold` → đặt trạng thái `SUSPEND`.
   - Tái phân công: nếu đang trong khung giờ làm việc → tìm collector cùng enterprise, trạng thái `AVAILABLE`/`ACTIVE`, có tọa độ và nằm trong `reassign-radius-km`; chọn gần nhất và gán lại.
2. Vi phạm SLA hoàn thành
   - Mốc: nhiệm vụ chưa COMPLETED quá `sla-hours`.
   - Đánh dấu: set `slaViolated = true`.
   - Hệ quả: tăng `violationCount` và xét treo như trên.
3. Khung giờ làm việc
   - Xác định bởi `working-start-hour` đến `working-end-hour` theo thời gian hệ thống.
   - Các hành động tái phân công chỉ thực hiện trong khung này.

## Bảng tác động & trạng thái
- Thuộc tính Collector:
  - violationCount: tăng khi timeout nhận việc hoặc vi phạm SLA.
  - status: chuyển `SUSPEND` khi vượt ngưỡng.
- Thuộc tính CollectionRequest:
  - assignedAt/updatedAt: cập nhật khi tái phân công.
  - status: đảm bảo `ASSIGNED` khi gán lại.
  - slaViolated: đánh dấu khi vượt SLA.

## Kịch bản kiểm thử nhanh
- Timeout nhận việc:
  - Seed một request ở trạng thái ASSIGNED với `assignedAt` cũ hơn `accept-timeout-hours`.
  - Chạy schedule/hoặc gọi trực tiếp method liên quan trong `TaskAutomationServiceImpl`.
  - Kiểm tra: violationCount tăng, collector có thể bị SUSPEND, request được tái phân công nếu trong giờ.
- Vi phạm SLA:
  - Seed request chưa COMPLETED với `assignedAt` cũ hơn `sla-hours`.
  - Kiểm tra: `slaViolated = true`, violationCount tăng và xét treo.
- Tái phân công:
  - Có ít nhất 2 collector `AVAILABLE/ACTIVE` cùng enterprise, có tọa độ; bảo đảm một collector trong bán kính.
  - Kiểm tra request gán sang collector gần nhất khác collector hiện tại.

## Lưu ý triển khai
- Múi giờ: phụ thuộc múi giờ hệ thống; cân nhắc thiết lập TZ nhất quán giữa môi trường.
- Dữ liệu vị trí: tái phân công yêu cầu collector có `currentLatitude/Longitude`; nếu thiếu → bỏ qua.
- Có thể mở rộng thành lịch làm việc linh hoạt theo doanh nghiệp/địa phương.

