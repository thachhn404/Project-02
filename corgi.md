# SWP391 – Backend System Design (Full Version)

## 1. System Overview

Hệ thống **SWP391 – Crowdsourced Waste Collection & Recycling Platform** là một nền tảng backend phục vụ việc **báo cáo, điều phối và thu gom rác thải**, được thiết kế theo hướng **chuẩn nghiệp vụ, dễ audit, chống gian lận và triển khai thực tế** cho môi trường đô thị Việt Nam.

### Mục tiêu thiết kế

* Tuân thủ **100% business rules đã chuẩn hoá**
* Mọi quyết định nghiệp vụ đều **có log, có truy vết**
* Ngăn chặn gian lận điểm thưởng
* Phân tách rõ **trách nhiệm Citizen – Enterprise – Collector – System**

### Actors

* **Citizen**: tạo báo cáo rác, theo dõi trạng thái, nhận điểm thưởng
* **Recycling Enterprise**: duyệt báo cáo, điều phối và gán Collector (**chỉ 1 Enterprise duy nhất**)
* **Collector**: thực hiện thu gom, báo cáo thực tế bằng ảnh & checklist
* **Administrator**: giám sát hệ thống, audit, xử lý tranh chấp

### High-level Flow

```
Citizen → WasteReport → Enterprise → Collector → Collected → Reward
                      ↘ Complaint (parallel)
```

---

## 2. Business Principles (Cốt lõi)

1. Enterprise là **trung tâm điều phối duy nhất**
2. Collector **không tự pick task**
3. Một task – một Collector tại một thời điểm
4. **Không giới hạn số task của Collector ở tầng system**
5. Enterprise **chịu trách nhiệm filter & phân bổ task** cho Collector
6. Collector **chỉ báo cáo thực tế**, không quyết định điểm
7. Điểm thưởng do **System tự động tính** sau khi Collected

---

## 3. State Model (Chuẩn nghiệp vụ)

```
Pending
→ Accepted (Enterprise)
→ Assigned
→ Accepted (Collector)
→ On the way
→ Collected
```

### Active Tasks của Collector

> Hệ thống **không áp đặt hard limit** số lượng active tasks cho Collector.
> Việc kiểm soát tải công việc thuộc **trách nhiệm nghiệp vụ của Enterprise** thông qua filter, SLA và năng lực Collector.

```
Active Tasks = Assigned + Accepted (Collector) + On the way
```

---

## 4. Entity & Domain Model

### 4.1 User

* id
* role (CITIZEN | ENTERPRISE | COLLECTOR | ADMIN)
* status (ACTIVE | SUSPENDED)
* created_at

### 4.2 WasteReport

* id
* citizen_id (FK → User)
* waste_type_id (FK → WasteType)
* status (PENDING | ACCEPTED_ENTERPRISE | ASSIGNED | ACCEPTED_COLLECTOR | ON_THE_WAY | COLLECTED | REJECTED | TIMED_OUT)
* latitude
* longitude
* address_text
* created_at
* updated_at

### 4.3 WasteType

* id
* code (HOUSEHOLD | RECYCLABLE | HAZARDOUS)
* sla_hours

### 4.4 CollectionTask

* id
* report_id (FK → WasteReport)
* collector_id (FK → User)
* assigned_at
* accepted_at
* started_at
* completed_at
* status (ASSIGNED | ACCEPTED | ON_THE_WAY | COMPLETED | CANCELLED)

### 4.5 RewardRule

* id
* version
* waste_type_id
* base_point
* effective_from

### 4.6 RewardPoint

* id
* citizen_id
* report_id
* rule_version
* point
* created_at

### 4.7 Complaint

* id
* report_id
* citizen_id
* status (OPEN | IN_REVIEW | RESOLVED | REJECTED)
* description
* created_at

### 4.8 AuditLog

* id
* actor_id
* actor_role
* action
* target_type
* target_id
* metadata
* created_at

---

## 5. Database Rules & Constraints

### Mandatory Constraints

* Citizen tối đa **5 báo cáo/ngày**
* **Không có hard limit** số task của Collector ở tầng system
* Enterprise phải filter task theo SLA, khu vực, tải hiện tại trước khi gán
* 1 report chỉ có **1 Collector tại một thời điểm**
* RewardPoint chỉ được tạo khi report = COLLECTED

### Indexing

* WasteReport(status, created_at)
* CollectionTask(collector_id, status)
* RewardPoint(citizen_id, created_at)
* AuditLog(target_type, target_id)

---

## 6. API Design (Logical)

### 6.1 Citizen APIs

* POST /reports
* PUT /reports/{id} (PENDING only)
* DELETE /reports/{id} (PENDING only)
* GET /reports/{id}
* GET /rewards/history
* POST /complaints

### 6.2 Enterprise APIs

* GET /enterprise/reports?status=PENDING
* POST /enterprise/reports/{id}/accept
* POST /enterprise/reports/{id}/reject
* POST /enterprise/reports/{id}/assign-collector

### 6.3 Collector APIs

* GET /collector/tasks
* POST /collector/tasks/{id}/accept
* POST /collector/tasks/{id}/reject
* POST /collector/tasks/{id}/start
* POST /collector/tasks/{id}/complete (upload image + checklist)

> Lưu ý: Collector **chỉ thao tác trên task đã được Enterprise gán**, không có API tự pick task

### 6.4 Admin APIs

* GET /admin/audit-logs
* GET /admin/reports/statistics
* POST /admin/complaints/{id}/resolve

---

## 7. Workflow Summary

### Citizen Reporting

1. Create report → PENDING
2. System validate limit + log

### Enterprise Processing

1. Review PENDING (FCFS)
2. Accept → ACCEPTED_ENTERPRISE
3. Assign Collector → ASSIGNED

### Collector Execution

1. Accept task → ACCEPTED_COLLECTOR
2. Start collection → ON_THE_WAY
3. Upload image + checklist → COLLECTED

---

## 8. Reward Point Logic

* Trigger: Report → COLLECTED
* Validate daily cap ≤ 100
* Apply RewardRule(versioned)
* Persist RewardPoint
* Leaderboard updated daily at 21:00

Collector **không can thiệp** vào điểm

---

## 9. SLA & Timeout Handling

* SLA theo WasteType.sla_hours
* Background job check overdue
* Overdue → mark Late + notify Citizen
* PENDING > 24h → TIMED_OUT

---

## 10. Security & Audit

* JWT Authentication
* Role + Data-level authorization
* Encrypt GPS & images
* Audit log cho:

    * State change
    * Assignment
    * Reward creation
    * Complaint handling

---

## 11. Risks & Edge Cases

* Spam report → daily limit + GPS dedup
* Collector overload → hard limit 3 tasks
* Gian lận ảnh → audit + checklist
* Trễ SLA → ưu tiên xử lý

---

**END OF DOCUMENT**
