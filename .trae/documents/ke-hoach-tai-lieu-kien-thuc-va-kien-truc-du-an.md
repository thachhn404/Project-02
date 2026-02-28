# Kế hoạch: Viết tài liệu “Kiến thức đã dùng” và “Kiến trúc dự án”

Tên dự án: Crowdsourced-Waste-Collection-Recycling-System (CWCRS)

## Mục tiêu
- Tổng hợp kiến thức, công nghệ và các quyết định thiết kế đã dùng trong dự án.
- Mô tả rõ kiến trúc hệ thống (mức C4: Context → Container → Component), luồng nghiệp vụ chính, chuẩn mã nguồn và quy ước.
- Tạo bộ tài liệu dễ bảo trì, có liên kết đến vị trí mã nguồn cụ thể.

## Phạm vi & giả định
- Tập trung vào backend Java (nhiều khả năng Spring Boot) dựa trên package `com.team2...`.
- Xác định thêm frontend/infra nếu tồn tại trong repo.
- Kiểm tra và ghi nhận sự nhất quán giữa cấu trúc thư mục và khai báo package (ví dụ: SeedWorkflowInitializer.java đang ở đường dẫn thư mục lowercase nhưng package sử dụng chữ hoa và gạch dưới).

## Đầu ra dự kiến (artifacts)
- docs/overview.md: Tóm tắt hệ thống, mục tiêu và phạm vi.
- docs/tech-stack.md: Danh sách công nghệ, lý do chọn, trade-offs.
- docs/architecture/overview.md: Kiến trúc tổng thể theo C4 (Context/Container).
- docs/architecture/components.md: Thành phần chính, ranh giới, phụ thuộc, pattern (layered/hexagonal).
- docs/architecture/layers.md: Controller/Service/Repository/Config/Security và nguyên tắc phụ trợ.
- docs/data-model.md: Thực thể, quan hệ (từ JPA/Hibernate, Flyway/Liquibase nếu có).
- docs/api/endpoints.md: Danh sách API, cấu trúc request/response, mã lỗi, chuẩn hóa phản hồi.
- docs/security.md: Cơ chế authN/authZ (ví dụ Spring Security, JWT), CORS, bảo mật cấu hình.
- docs/conventions.md: Quy ước đặt tên, DTO vs Entity, logging, xử lý lỗi, validate.
- docs/workflows/seed-workflow.md: Cách khởi tạo dữ liệu (tham chiếu SeedWorkflowInitializer), khi nào chạy và dữ liệu ví dụ.
- docs/operations/build-run.md: Cách build/run, profile, biến môi trường, Docker/Compose.
- docs/operations/ci-cd.md: Mô tả pipeline nếu có.
- diagrams/: Hình C4 và sequence cho luồng nghiệp vụ chính.

Tất cả tài liệu sẽ chèn liên kết đến mã nguồn bằng dạng link file:///absolute/path#Lx-Ly theo quy ước của môi trường.

## Quy trình thực hiện (có thể lặp)
1) Khảo sát cấu trúc repo
   - Liệt kê thư mục cấp cao, xác định backend, frontend, infra, scripts.
   - Ghi nhận những tệp mấu chốt: pom.xml/gradle, application.yml/properties, Dockerfile, docker-compose, README.
2) Xác định công nghệ và dependencies
   - Phân tích pom.xml/gradle.build để liệt kê framework, starter, DB driver, libs (Spring Web, Data JPA, Security, Validation…).
   - Ghi lý do chọn (nếu suy luận được) và các trade-offs phổ biến.
3) Kiến trúc tổng thể (C4)
   - Vẽ Context: hệ thống và các phụ thuộc bên ngoài.
   - Vẽ Container: service backend, DB, message broker (nếu có), frontend.
4) Phân tích thành phần & pattern
   - Duyệt package: controller, service, repository, config, security, exception, dto, mapper.
   - Mô tả ranh giới, nguyên tắc phụ thuộc, flow điển hình request → response.
5) Mô hình dữ liệu
   - Quét Entity, quan hệ (OneToMany/ManyToMany…), cascade, fetch.
   - Kiểm tra migration (Flyway/Liquibase) và đồng bộ với Entity.
6) API surface
   - Trích xuất @RestController, @RequestMapping, HTTP method, path, DTO.
   - Chuẩn hóa response, error handling, validation.
7) Bảo mật
   - Phân tích cấu hình Spring Security (filter chain, auth manager, JWT, password encoder).
   - Xác định vai trò/quyền, chính sách CORS.
8) Cấu hình & môi trường
   - application-(profile).yml/properties, biến môi trường, secret (không đưa giá trị nhạy cảm vào repo).
9) Workflow khởi tạo dữ liệu
   - Mô tả SeedWorkflowInitializer: thời điểm chạy, dữ liệu, cách mở rộng/tắt.
10) Vận hành: build/run/CI-CD
   - Câu lệnh build, profile, Docker/Compose, pipeline.
11) Quy ước & chất lượng
   - Logging, exception mapping, đặt tên, cấu trúc thư mục, coding style.
12) Rà soát nhất quán package ↔ thư mục
   - Phát hiện và đề xuất sửa các sai lệch (ví dụ case/underscore trong package).
13) Vẽ diagram & minh họa luồng
   - C4 (Context/Container/Component) và sequence cho luồng chính (đăng ký, báo cáo rác, nhận nhiệm vụ thu gom…).
14) Liên kết đến mã nguồn
   - Chèn liên kết file:///… đến lớp/hàm quan trọng (ví dụ Controller, Service, Config).
15) Kiểm tra chéo & hoàn thiện
   - Review với nhóm, cập nhật tài liệu theo phản hồi.

## Tiêu chí hoàn thành
- Tài liệu đầy đủ các mục ở “Đầu ra dự kiến”.
- Mỗi mục đều có liên kết đến mã nguồn liên quan.
- Diagram C4 và ít nhất 1–2 sequence cho luồng nghiệp vụ cốt lõi.
- Hướng dẫn build/run có thể thực thi được.

## Rủi ro & lưu ý
- Sai lệch giữa package và cấu trúc thư mục có thể gây lỗi classpath; cần ghi rõ và đề xuất cách sửa.
- Không tiết lộ secrets/keys trong tài liệu; chỉ mô tả vị trí và cách cấu hình.
- Nếu repo thiếu một số phần (frontend/CI), ghi chú “không áp dụng/không tìm thấy”.

## Phụ lục: tệp dự kiến rà soát
- pom.xml hoặc build.gradle
- src/main/resources/application.properties|yml (+ các profile)
- Dockerfile, docker-compose.yml (nếu có)
- src/main/java/.../config/Security, SeedWorkflowInitializer.java và các package: controller, service, repository, dto, entity
- README.md và tài liệu hiện có

## Kế hoạch triển khai (không thời gian)
- Pha 1: Khảo sát & lập dàn ý tài liệu.
- Pha 2: Viết tech stack, kiến trúc tổng thể, conventions.
- Pha 3: Bổ sung data model, API, security, seed workflow.
- Pha 4: Diagram & liên kết mã nguồn.
- Pha 5: Rà soát, hiệu chỉnh, bàn giao.

