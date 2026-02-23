# CORE RULES (Enterprise Java Backend – Viettel Style)

> Áp dụng cho **mọi module** (không phụ thuộc PADT).  
> Tech stack: Spring Boot, Spring Data JPA (Hibernate), Oracle DB, Java 8+  
> Base package: `vn.com.viettel`

---

## 1) Time & Date Rules

- **KHÔNG dùng `Instant`** → dùng `LocalDateTime`.
- `LocalDateTime.now()` cho mọi timestamp server-side.

---

## 2) Soft Delete Rules (BẮT BUỘC)

- **KHÔNG hard delete.**
- DB dùng `IS_DELETED CHAR(1) ∈ {'Y','N'}`.
- Entity map thành `Boolean isDeleted` và **luôn filter `isDeleted = false`** ở mọi truy vấn list/search/detail.

### Mapping chuẩn
```java
@Convert(converter = org.hibernate.type.YesNoConverter.class)
@Column(name = "IS_DELETED")
private Boolean isDeleted;

@Convert(converter = org.hibernate.type.YesNoConverter.class)
@Column(name = "IS_ACTIVE")
private Boolean isActive;
```

### Lưu ý
- **Không so sánh 'Y'/'N' trong code** (Repository/Specification chỉ dùng Boolean).

---

## 3) Entity Rules (JPA – Flat Mapping)

- Entity **CHỈ chứa field tồn tại trong bảng**.
- **KHÔNG thêm field phụ**, không map object từ bảng khác.
- **CẤM dùng `@ManyToOne` / `@OneToMany`**.
- Chỉ dùng FK dạng **ID** (`Long`) (ví dụ: `planId`, `taskId`, ...).

---

## 4) Exception & Error Handling Rules

- **TUYỆT ĐỐI KHÔNG**:
  - `throw RuntimeException`
  - `ResponseStatusException`
- Mọi lỗi phải throw **CustomException** theo format:
```java
throw new CustomException(HttpStatus.<STATUS>.value(), msg("<message.key>", params...));
```

---

## 5) Repository Validation: EXISTS-FIRST (BẮT BUỘC)

### Nguyên tắc
- Mọi validation kiểu “id có tồn tại không” **PHẢI** dùng:
  - `existsBy...AndIsDeletedFalse(...)`, hoặc
  - query `select 1`, hoặc
  - projection `findExistingIds(...)`

### CẤM
- `findById(...)` rồi check null chỉ để validate
- `findAllById(...)` rồi so size
- “Load entity/collection” chỉ để kiểm tra tồn tại

### Validate danh sách IDs (BẮT BUỘC 1 query)
- Dùng 1 query projection:
```java
@Query("select e.id from Entity e where e.id in :ids and e.isDeleted = false")
List<Long> findExistingIds(@Param("ids") Collection<Long> ids);
```
Hoặc `countByIdInAndIsDeletedFalse(...)` (kèm distinct nếu cần).

---

## 6) Reuse Existing Repositories (BẮT BUỘC)

- Nếu dự án **đã có sẵn** repository:
  - `CatSurveyEquipmentRepository`
  - `WorkItemRepository`
  - `CategoryWorkItemRepository`
- **KHÔNG được sinh lại / KHÔNG đổi tên / KHÔNG đổi package**.
- Nếu cần method mới: **chỉ bổ sung method vào file đã có**, không tạo repo mới thay thế.

---

## 7) Swagger / OpenAPI Rules

### DTO
- Mọi DTO class + field cần `@Schema`.
- Field bắt buộc dùng `@NotNull`, `@NotBlank`, `@Size` và `requiredMode = REQUIRED`.
- Field output-only: `@Schema(accessMode = Schema.AccessMode.READ_ONLY)`.

### Controller
- Có `@Tag`, `@Operation`, `@ApiResponses`, `@ApiResponse`, `@Content`.
- Validate request bằng `@Valid`.

---

## 8) User & Audit Enrichment Rules (GIỐNG ProjectTypeServiceImpl)

### Audit fields trong Entity (nếu bảng có)
- `createdBy/updatedBy: Long`
- `createdAt/updatedAt: LocalDateTime`

### Response DTO bắt buộc trả
- `UserDto createdByUser`
- `UserDto updatedByUser` (nếu có updatedBy)

### Enrich user theo batch (tránh N+1)
- Collect tất cả `createdBy/updatedBy/actorId` → query 1 lần từ `SysUserRepository`
- Nếu cần org: enrich qua `SysOrgRepository`

### Lấy current user
- Theo `SecurityContextHolder`
- Fallback: `Constants.DEFAULT_USER_ID` nếu null

---

## 9) Mapper Rules (Mapping Guarantee)

- Mapper (MapStruct/ModelMapper) **không được map field readOnly từ DTO -> Entity**.
- Update: chỉ map các field input whitelist.
- MapStruct gợi ý:
  - `@BeanMapping(ignoreByDefault = true)`
  - `@Mapping` từng field input

---

## 10) Project Structure Convention

### Java packages
```
vn.com.viettel
  ├─ config
  ├─ constant
  ├─ controllers
  ├─ dto
  ├─ entities
  ├─ feign
  ├─ handlers
  ├─ mapper
  ├─ repositories.jpa
  ├─ services
  ├─ utils
  ├─ MainGenCode
  └─ ServiceApplication
```

### Resources
```
resources/
  ├─ application.properties
  ├─ application-k8s.properties
  ├─ config.properties
  ├─ logback.xml
  └─ messages/
      ├─ messages_en.properties
      └─ messages_vi.properties
```

---

## 11) Coding Prohibitions (Quick Guardrails)

- No `Instant`
- No `Optional.get()`
- No JPA relations
- No hard delete
- No validation via loading entities
- CustomException only
- Always filter `isDeleted=false`

