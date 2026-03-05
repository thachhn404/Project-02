# Kiến thức Reward/Point (điểm thưởng) trong hệ thống

Tài liệu này mô tả **hệ thống điểm thưởng (reward/point)**: dùng bảng/đối tượng nào, cộng điểm ở đâu, chống cộng trùng ra sao, và các API liên quan (lịch sử điểm + complaint type `POINT`).

## Mục tiêu & phạm vi

- Hệ thống hiện tại tập trung vào **cộng điểm (EARN)** cho **Citizen** khi Collector hoàn tất thu gom và tạo **Collector Report**.
- Chưa thấy luồng **trừ điểm** (spend/deduct/penalty) trong codebase tại thời điểm hiện tại.

## Data model (DB/JPA)

### 1) Số dư điểm hiện tại (snapshot)

- `citizens.total_points` được dùng làm **số dư hiện tại**.
- Khi cộng điểm, hệ thống cập nhật `Citizen.totalPoints`.
  - Lock record theo citizen để tránh race condition: [CitizenRepository.findByIdForUpdate](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/profile/CitizenRepository.java#L14-L25)

### 2) Lịch sử giao dịch điểm

- Bảng/Entity: `point_transactions` / [PointTransaction](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/PointTransaction.java#L21-L64)
- Ý nghĩa các field quan trọng:
  - `citizen`: chủ sở hữu điểm.
  - `collectionRequest`: nhiệm vụ thu gom liên quan (để trace thưởng theo task).
  - `report`: `WasteReport` liên quan (để trace theo báo cáo).
  - `points`: số điểm thay đổi (hiện chỉ thấy dùng số dương).
  - `transactionType`: string (đang dùng hằng `"EARN"`).
  - `balanceAfter`: snapshot số dư sau giao dịch.
  - `createdBy`, `createdAt`: audit.
- Repository: [PointTransactionRepository](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/reward/PointTransactionRepository.java)
  - Idempotency/check: `existsByCollectionRequestIdAndTransactionType(...)`.
  - Query lịch sử: `findByCitizenId(...)`.

## Rule cộng điểm (EARN)

### 1) Điểm được tính từ WasteCategory

- Mỗi loại rác có `pointPerUnit`: [WasteCategory.pointPerUnit](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/entity/WasteCategory.java#L32-L38)
- Khi Collector tạo report, hệ thống tính điểm theo từng item: `itemPoints = quantity * pointPerUnit`.
- Report item lưu snapshot `unitSnapshot` + `pointPerUnitSnapshot` để đảm bảo lịch sử không bị ảnh hưởng nếu admin đổi cấu hình category về sau.

### 2) Điểm được cộng khi nào (entry point)

- API: `POST /api/collector/collections/{requestId}/complete`.
  - Controller: [CollectionController.createCollectorReport](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/collector/CollectionController.java#L203-L215)
- Service chính: [CollectorReportCreationService.createCollectorReport](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorReportCreationService.java)
- Đoạn cộng điểm + tạo transaction: [CollectorReportCreationService.rewardCitizen](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorReportCreationService.java#L237-L279)

### 3) Điều kiện cộng điểm

Trong [rewardCitizen](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorReportCreationService.java#L237-L279):

- Nếu `points <= 0` thì không cộng.
- `WasteReport` phải có `citizen` (thiếu thì trả `400`).
- Nếu đã có `PointTransaction` loại `EARN` cho `collectionRequestId` thì bỏ qua (chống cộng trùng).

## Chống cộng trùng & concurrency

Hệ thống đang chống cộng trùng theo 2 lớp:

### 1) Chặn tạo Collector Report trùng

- `collectorReportRepository.existsByCollectionRequest_Id(requestId)` → trả `409` (để client retry không tạo thêm report).

### 2) Chặn cộng điểm trùng theo transaction

- Check tồn tại trước khi lock: `existsByCollectionRequestIdAndTransactionType(requestId, "EARN")`.
- Lock citizen bằng pessimistic write: [CitizenRepository.findByIdForUpdate](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/profile/CitizenRepository.java#L14-L25)
- Check lại lần 2 sau khi lock (double-check) để tránh race: [rewardCitizen](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorReportCreationService.java#L247-L260)

Ghi chú:

- DB schema hiện không thấy unique constraint ở mức DB trên `(collection_request_id, transaction_type)`; vì vậy idempotency dựa vào logic ứng dụng + lock citizen.

## Lịch sử điểm & API liên quan

### 1) Xem lịch sử điểm

- API: `GET /api/citizen/rewards/history?startDate=&endDate=`
  - Controller: [CitizenController.getRewardHistory](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/citizen/CitizenController.java#L129-L143)
  - Service: [WasteReportServiceImpl.getRewardHistory](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/WasteReportServiceImpl.java#L434-L454)
- Hiện tại service lấy toàn bộ `PointTransaction` theo citizen rồi filter theo thời gian ở memory.

### 2) Bảng xếp hạng (dựa trên tổng điểm)

- API: `GET /api/citizen/leaderboard?region=`
  - Logic sort theo `Citizen.totalPoints`: [WasteReportServiceImpl.getLeaderboard](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/WasteReportServiceImpl.java#L456-L481)

## Complaint type POINT (validate phụ thuộc reward)

Complaint `POINT` được dùng để khiếu nại về điểm thưởng và chỉ hợp lệ khi report đã được cộng điểm.

### 1) Normalize type

- `type` được normalize: trim + thay whitespace bằng `_` + uppercase.
  - [WasteReportServiceImpl.normalizeComplaintType](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/WasteReportServiceImpl.java#L519-L525)

### 2) Rule validate cho POINT

Trong [WasteReportServiceImpl.createComplaint](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/WasteReportServiceImpl.java#L483-L517):

- Nếu `type == "POINT"`:
  - Report phải có `CollectionRequest` (nếu không có → `INVALID_REQUEST`).
  - Phải tồn tại `PointTransaction` loại `EARN` cho `collectionRequestId`.
    - `existsByCollectionRequestIdAndTransactionType(crId, "EARN")`.

Ý nghĩa thực tế:

- Complaint `POINT` chỉ xử lý khi có “bằng chứng” đã từng cộng điểm cho nhiệm vụ đó.

## Những điều cần biết khi mở rộng (nếu làm trừ điểm)

- Nếu bổ sung `transactionType` mới (ví dụ `DEDUCT`, `SPEND`, `PENALTY`):
  - Cần quyết định key idempotency (theo `collectionRequestId`, `reportId`, hay `complaintId`).
  - Nên cân nhắc unique constraint ở DB để chống trùng ở mức dữ liệu.
  - Cần định nghĩa rõ `points` âm hay luôn dương kèm `transactionType`.
