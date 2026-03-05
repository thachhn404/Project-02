# Luồng Collector hoàn tất thu gom & tạo Collector Report (multipart)

Tài liệu này mô tả luồng code khi **Collector** hoàn tất một nhiệm vụ thu gom và **tạo báo cáo thu gom (Collector Report)** kèm ảnh, GPS và khối lượng theo danh mục. Trọng tâm là: actor nào gọi gì, code chạy qua các lớp nào, validate gì và vì sao validate như vậy.

## Actor & hệ thống liên quan

- **Collector (Mobile/Web client)**: người thực hiện nhiệm vụ thu gom, gửi API cập nhật trạng thái và tạo report.
- **Backend API**: nhận request, validate, ghi DB, upload ảnh, cộng điểm.
- **Cloudinary**: lưu trữ ảnh (upload theo module/folder).
- **Database**: lưu `collection_requests`, `collector_reports`, `collector_report_items`, `collector_report_images`, `point_transactions`, cập nhật `citizens.total_points`.

## Entry points (API)

- Đánh dấu “đã thu gom tại điểm” (bước tiền điều kiện):
  - [CollectionController.markCollected](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/collector/CollectionController.java#L151-L166)
  - `POST /api/collector/collections/{requestId}/collected`
- Tạo report khi hoàn tất (multipart/form-data):
  - [CollectionController.createCollectorReport](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/collector/CollectionController.java#L203-L215)
  - `POST /api/collector/collections/{requestId}/complete` (multipart)

## Luồng tổng quan (happy path)

1. **Collector gọi** `POST .../{requestId}/collected`
   - Backend chuyển trạng thái nhiệm vụ từ `on_the_way` → `collected`.
   - Mục đích: tách rõ “đã tới điểm và thu gom” khỏi “đã hoàn tất và gửi báo cáo”.
   - Code path nội bộ: [CollectorServiceImpl.completeTask](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorServiceImpl.java#L172-L185) → [CollectionRequestRepository.completeTask](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectionRequestRepository.java#L360-L372) (chỉ update nếu status đang `on_the_way`).
2. **Collector gọi** `POST .../{requestId}/complete` (multipart)
   - Backend validate (ownership, trạng thái, GPS, dữ liệu danh mục/khối lượng, ảnh).
   - Tạo `collector_reports` + `collector_report_items` + `collector_report_images`.
   - Cập nhật `collection_requests` từ `collected` → `completed` và set `actualWeightKg`, `completedAt`.
   - Cập nhật `waste_reports.status = collected`.
   - Cộng điểm cho `citizen` và tạo `point_transactions (EARN)` nếu chưa từng cộng cho request đó.

## Dữ liệu input (multipart) và validate ở tầng DTO

Multipart được bind vào DTO:
- [CreateCollectorReportRequest](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/dto/request/CreateCollectorReportRequest.java#L24-L46)

Các validate quan trọng (tự động qua `@Valid` ở controller):
- `images`: `@Size(min=1)` yêu cầu có ít nhất 1 ảnh.
- `categoryIds`: `@Size(min=1)` yêu cầu chọn ít nhất 1 danh mục.
- `collectorNote`: `@NotBlank`, `@Size(max=1000)`.
- `latitude/longitude`: `@NotNull` và range hợp lệ (lat `[-90,90]`, lng `[-180,180]`).

Vì sao vẫn còn validate ở service?
- Multipart + danh sách `quantities` thường khó đảm bảo đầy đủ bằng annotation (ví dụ: size của `quantities` phải khớp `categoryIds`, từng phần tử không âm, tổng > 0), nên service phải validate logic nghiệp vụ chi tiết hơn.

## Luồng chi tiết trong service (code path)

Luồng chính nằm ở:
- [CollectorReportCreationService.createCollectorReport](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorReportCreationService.java#L53-L228)

### 1) Xác thực actor (Collector) & chống request sai

- `requestId == null` → `400`.
- `collectorId == null` → `403`.
  - `collectorId` được lấy từ JWT claim trong controller:
    - [CollectionController.extractCollectorId](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/controller/collector/CollectionController.java#L267-L279)

Vì sao làm vậy?
- Buộc request được thực hiện bởi actor đúng role (Collector) và có danh tính rõ ràng; tránh gọi nhầm endpoint từ user khác.

### 2) Chặn tạo report trùng (idempotency)

- `existsByCollectionRequest_Id(requestId)` → `409 CONFLICT`.

Vì sao?
- Tránh tạo nhiều report cho cùng một nhiệm vụ, đặc biệt khi client retry do mạng yếu/timeout.

### 3) Ownership: request phải thuộc collector hiện tại

- Load `CollectionRequest` bằng `findByIdAndCollector_Id(requestId, collectorId)` → không có → `404`.

Vì sao?
- Đây là kiểm soát truy cập theo sở hữu dữ liệu (data ownership), tránh collector A tạo report cho nhiệm vụ của collector B.

### 4) Validate trạng thái trước khi “complete”

- Chỉ cho tạo report khi `collectionRequest.status == COLLECTED`, nếu không → `400`.

Vì sao?
- Đây là “state machine”: complete là bước sau collected. Check trạng thái giúp:
  - tránh nhảy bước (on_the_way → completed),
  - tránh ghi đè/hoàn tất lại nhiệm vụ đã completed/canceled,
  - giữ dữ liệu nhất quán với luồng nghiệp vụ.

### 5) Validate GPS trong bán kính cho phép

- Lấy tọa độ “gốc” từ `WasteReport` gắn với `CollectionRequest`.
  - thiếu `latitude/longitude` → `400`.
- Tính khoảng cách Haversine (km) giữa vị trí gốc và vị trí gửi lên.
- Nếu `distKm > workrule.reportRadiusKm` → `400`.
  - cấu hình: [WorkRuleProperties.reportRadiusKm](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/config/WorkRuleProperties.java#L12-L20) (default `0.5km`)

Vì sao validate GPS?
- Hạn chế gian lận (gửi report từ xa).
- Tăng độ tin cậy: report phải được tạo gần địa điểm thực tế đã report ban đầu.
- Dùng config để dễ điều chỉnh theo thực tế vận hành (khu vực đông đúc vs thưa thớt).

### 6) Validate ảnh + dữ liệu danh mục/khối lượng

Service tiếp tục validate:
- `images` null/empty → `400`.
- `categoryIds` null/empty → `400`.
- `quantities` null hoặc `quantities.size() != categoryIds.size()` → `400`.

Validate từng dòng (theo index):
- `categoryId` hoặc `qty` null → `400`.
- `qty < 0` → `400` (khối lượng không âm).
- `qty == 0` → bỏ qua (không tạo item).
- `WasteCategory` không tồn tại → `400`.

Kết quả tổng:
- `totalWeightKg <= 0` → `400` (bắt buộc có ít nhất 1 item khối lượng > 0).

Vì sao validate chặt?
- Ngăn dữ liệu rác/không đầy đủ (categoryIds có nhưng quantities thiếu).
- Ngăn “âm khối lượng” dẫn đến điểm âm hoặc số liệu sai.
- Tổng khối lượng > 0 là điều kiện tối thiểu để coi là hoàn tất thu gom.

### 7) Tính điểm theo danh mục + snapshot dữ liệu

- `pointPerUnit` lấy từ `WasteCategory.pointPerUnit` (null → 0).
- `itemPoints = qty * pointPerUnit` (làm tròn 0 chữ số, HALF_UP).
- Mỗi `CollectorReportItem` lưu snapshot:
  - `unitSnapshot`, `pointPerUnitSnapshot`, `totalPoint`, `quantity`.

Vì sao snapshot?
- Nếu admin thay đổi `WasteCategory.unit/pointPerUnit` sau này, report cũ vẫn giữ số liệu lịch sử đúng theo thời điểm collector tạo report.

### 8) Ghi DB: report + items + images

- Tạo `CollectorReport` với status `COMPLETED` và các field chính.
- Save report, rồi tạo `reportCode` theo format `CRR%06d` nếu thiếu.
- Gán FK report cho từng item và `saveAll(items)`.
- Upload ảnh lên Cloudinary và lưu DB:
  - Upload: [CloudinaryServiceImpl.uploadImage](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CloudinaryServiceImpl.java#L42-L69)
  - Validate file ảnh: [FileUpLoadUtil.assertAllowedImage](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/util/FileUpLoadUtil.java#L30-L56)
    - max 10MB, chỉ extension `jpg/jpeg/png/gif/bmp`.

Vì sao validate ảnh theo extension/size?
- Chặn upload file không phải ảnh và tránh quá tải lưu trữ/băng thông.
- Fail sớm trước khi gọi Cloudinary để giảm chi phí/độ trễ.

### 9) Cập nhật trạng thái CollectionRequest một cách “an toàn”

- `actualWeightKg = totalWeightKg.setScale(2, HALF_UP)`.
- Update bằng query có điều kiện status:
  - [CollectionRequestRepository.confirmCompletedWithWeight](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/repository/collector/CollectionRequestRepository.java#L394-L409)
  - Chỉ update nếu:
    - `cr.id == requestId`
    - `cr.collector.id == collectorId`
    - `cr.status == 'collected'`
- Nếu `updated == 0` → `400`.

Vì sao update theo điều kiện status?
- Tránh race condition: nếu request đã bị chuyển trạng thái bởi luồng khác thì update không “đè” lên.
- Là một dạng “CAS (compare-and-set)” ở DB: chỉ chuyển `collected` → `completed` khi đúng tiền điều kiện.
 - Query dùng literal lowercase (`'collected'`, `'completed'`) vì status enum được persist dạng lowercase trong DB.

### 10) Cập nhật WasteReport status

- Set `wasteReport.status = COLLECTED` và save.

Ghi chú:
- Trong flow này, `WasteReport` được đưa về `COLLECTED` (không có bước `COMPLETED` cho waste report trong đoạn code hiện tại).

### 11) Cộng điểm citizen + tạo PointTransaction (idempotent)

- Nếu chưa có transaction `EARN` cho request:
  - `existsByCollectionRequestIdAndTransactionType(requestId, "EARN")` → false thì mới cộng.
- Validate `citizenId` tồn tại → thiếu → `400`.
- Lock citizen để cộng điểm an toàn khi concurrent:
  - `citizenRepository.findByIdForUpdate(citizenId)`
- Update `citizen.totalPoints += totalPoints` và save.
- Tạo `PointTransaction`:
  - `transactionType="EARN"`, `points=totalPoints`, `balanceAfter`, `createdBy`, `createdAt`.

Vì sao check “đã có EARN” và lock citizen?
- Check tồn tại giúp chống cộng điểm lặp khi client retry hoặc request bị gửi lại.
- Lock giúp tránh “lost update” nếu có nhiều luồng cùng cộng điểm cho một citizen tại cùng thời điểm.

## Transaction boundary: vì sao dùng @Transactional

- `createCollectorReport` được đánh dấu `@Transactional`.
  - [CollectorReportCreationService](file:///d:/Documents/SWP/Crowdsourced-Waste-Collection-Recycling-System/src/main/java/com/team2/Crowdsourced_Waste_Collection_Recycling_System/service/impl/CollectorReportCreationService.java#L53)

Ý nghĩa thực tế:
- Các thao tác DB (tạo report/items/images, update request status, cộng điểm, tạo transaction) được thực hiện như một “gói” nhất quán.
- Nếu có lỗi ở phần DB sau (ví dụ: update status thất bại), các thay đổi DB trước đó sẽ rollback.

Lưu ý quan trọng:
- Upload ảnh lên Cloudinary là hệ thống ngoài DB, nên nếu DB rollback sau khi đã upload thì ảnh có thể vẫn tồn tại trên Cloudinary (đây là đặc trưng của tích hợp external service). Nếu cần “cleanup” ảnh theo publicId khi rollback, phải bổ sung cơ chế bù trừ (compensation) riêng.

## Tóm tắt: tại sao validate nhiều lớp như vậy

- **An ninh & phân quyền**: collectorId từ JWT + ownership check.
- **Tính đúng luồng nghiệp vụ**: bắt buộc đúng trạng thái (`COLLECTED`) trước khi complete.
- **Chống gian lận**: GPS trong bán kính cho phép.
- **Toàn vẹn dữ liệu**: categoryIds/quantities khớp nhau, khối lượng không âm, tổng > 0.
- **Tính nhất quán & chống trùng**: chặn duplicate report, chống cộng điểm lặp (EARN exists).
- **An toàn đồng thời**: update request theo điều kiện status + lock citizen khi cộng điểm.
