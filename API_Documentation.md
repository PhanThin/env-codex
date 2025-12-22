# Tài liệu API Dịch vụ Khuyến nghị (RecommendationService)

Đây là tài liệu mô tả chi tiết các API chính trong `RecommendationServiceImpl`.

---

## 1. `createRecommendation`

*   **Mô tả chung:**
    Hàm này chịu trách nhiệm tạo mới một bản ghi khuyến nghị (`Recommendation`) trong hệ thống. Nó tiếp nhận dữ liệu từ client, kiểm tra tính hợp lệ, lưu vào cơ sở dữ liệu, và xử lý các mối quan hệ với các đối tượng khác như `WorkItem`, `User` (người được giao), và các tệp đính kèm.

*   **Các hàm liên quan và chi tiết xử lý:**

    *   **`getCurrentUser()`**
        *   **Mô tả:** Lấy thông tin người dùng đang đăng nhập từ context bảo mật của Spring Security.
        *   **Chi tiết:** Gọi `SecurityContextHolder.getContext().getAuthentication()` để lấy đối tượng `Authentication`. Nếu đối tượng `Principal` trong đó là một instance của `SysUser`, nó sẽ được trả về. Nếu không, nó sẽ lấy tên người dùng và truy vấn CSDL qua `userRepository.findByUsername()` để tìm đối tượng `SysUser`.

    *   **`validate(dto, isUpdate = false)`**
        *   **Mô tả:** Kiểm tra toàn bộ dữ liệu đầu vào (`RecommendationDto`) để đảm bảo tính hợp lệ và toàn vẹn trước khi thực hiện ghi vào CSDL.
        *   **Chi tiết các bước kiểm tra:**
            1.  **Payload null:** `dto` không được null. Lỗi: `recommendation.payload.null`.
            2.  **Tiêu đề:**
                *   Không được trống (`isBlank`). Lỗi: `recommendation.title.required`.
                *   Độ dài không quá 250 ký tự. Lỗi: `recommendation.title.length`.
                *   **Không được trùng lặp:** Truy vấn `recommendationRepository.findByRecommendationTitle()` để tìm xem có tiêu đề nào đã tồn tại chưa. Vì `isUpdate = false`, nếu tìm thấy bất kỳ bản ghi nào, sẽ báo lỗi `recommendation.title.duplicate`.
            3.  **Nội dung:**
                *   Không được trống. Lỗi: `recommendation.content.required`.
                *   Độ dài không quá 500 ký tự. Lỗi: `recommendation.content.length`.
            4.  **Loại khuyến nghị (`recommendationType`):** ID của loại khuyến nghị phải tồn tại trong CSDL. Truy vấn `recommendationTypeRepo.existsById()`. Lỗi: `recommendation.type.notfound`.
            5.  **Độ ưu tiên (`priority`):**
                *   Không được trống. Lỗi: `recommendation.priority.required`.
                *   Phải là một trong các giá trị của `RecommendationPriorityEnum` (ví dụ: "HIGH", "MEDIUM", "LOW"). Lỗi: `recommendation.priority.invalid`.
            6.  **Kiểm tra các mối quan hệ (nếu có `projectId`):**
                *   `projectId` phải tồn tại. Truy vấn `projectRepo.existsById()`. Lỗi: `recommendation.project.notfound`.
                *   `itemId` (hạng mục dự án) phải được cung cấp và tồn tại. Lỗi: `recommendation.item.required`, `recommendation.item.notfound`.
                *   `itemId` phải thuộc về `projectId` đã cung cấp. Lỗi: `recommendation.item.not_belong_to_project`.
                *   `phaseId` (giai đoạn dự án) nếu có, phải tồn tại và thuộc về `projectId`. Lỗi: `recommendation.phase.notfound`, `recommendation.phase.not_belong_to_project`.
                *   Mỗi `workItemId` trong danh sách `workItems` phải tồn tại và thuộc về `itemId` đã cung cấp. Lỗi: `recommendation.workitem.id_required`, `recommendation.workitem.notfound`, `recommendation.workitem.not_belong_to_item`.

    *   **`recommendationMapper.toEntity()`**
        *   **Mô tả:** Chuyển đổi `RecommendationDto` thành entity `Recommendation` và thiết lập các giá trị mặc định/audit.
        *   **Chi tiết các trường được thiết lập:**
            *   **Các trường cơ bản:** Sao chép từ DTO (ví dụ: `recommendationTitle`, `content`, `priority`, `deadline`).
            *   **Các trường ID khóa ngoại:** `projectId`, `itemId`, `phaseId`, `recommendationTypeId` được lấy từ các đối tượng con trong DTO.
            *   **Giá trị mặc định và Audit:**
                *   `isDeleted`: `false`
                *   `status`: `RecommendationStatusEnum.NEW.getValue()` (chuỗi "NEW")
                *   `createdAt`: `LocalDateTime.now()`
                *   `createdById`: ID của người dùng hiện tại.
                *   `createdOrgId`: ID tổ chức của người dùng hiện tại.

    *   **`recommendationMapper.mapToRecommendationWorkItem(List<WorkItemDto>...)`**
        *   **Mô tả:** Tạo danh sách các entity liên kết `RecommendationWorkItem` từ danh sách `WorkItemDto`.
        *   **Chi tiết:** Với mỗi `WorkItemDto`, tạo một `RecommendationWorkItem` mới và gán `recommendationId` (từ bản ghi chính vừa lưu), `workItemId` (từ DTO), cùng thông tin audit (`createdAt`, `createdBy`).

    *   **`recommendationMapper.mapToRecommendationAssignment(...)`**
        *   **Mô tả:** Tạo danh sách các entity phân công `RecommendationAssignment` từ danh sách `UserDto`.
        *   **Chi tiết:** Với mỗi `UserDto`, tạo một `RecommendationAssignment` mới và gán `recommendationId`, `userId`, `orgId`, `assignedAt`, và `isPrimary = true`.

---

## 2. `updateRecommendation`

*   **Mô tả chung:** Cập nhật một khuyến nghị đã có, bao gồm cả việc đồng bộ hóa các danh sách liên quan như `WorkItem` và người được giao.

*   **Các hàm liên quan và chi tiết xử lý:**

    *   **`recommendationRepository.findById()`**
        *   **Mô tả:** Tìm bản ghi `Recommendation` trong CSDL bằng khóa chính.
        *   **Truy vấn:** `SELECT * FROM recommendation WHERE id = ?`.

    *   **`validate(dto, isUpdate = true)`**
        *   **Mô tả:** Tương tự như khi tạo mới, nhưng có một điểm khác biệt quan trọng.
        *   **Chi tiết:** Khi kiểm tra trùng lặp tiêu đề, nó sẽ cho phép tiêu đề trùng với chính bản ghi đang được sửa. Lỗi `recommendation.title.duplicate` chỉ được ném ra nếu tiêu đề đã tồn tại ở một bản ghi có ID khác.

    *   **`recommendationMapper.updateEntityFromDto()`**
        *   **Mô tả:** Cập nhật entity đã có từ DTO mà không làm mất các giá trị audit ban đầu.
        *   **Chi tiết:**
            1.  Lưu lại các giá trị gốc: `createdAt`, `createdById`, `createdOrgId`, `recommendationCode`.
            2.  Dùng `ModelMapper` để sao chép các giá trị mới từ DTO vào entity.
            3.  **Khôi phục lại các giá trị gốc** đã lưu ở bước 1 để đảm bảo chúng không bị ghi đè.
            4.  Cập nhật các trường audit cho việc sửa đổi: `lastUpdate = LocalDateTime.now()` và `lastUpdateBy = currentUserId`.

    *   **`updateWorkItem()` và `updateAssigment()`**
        *   **Mô tả:** Đồng bộ hóa danh sách các bản ghi liên kết (Work Items hoặc Assignments).
        *   **Chi tiết luồng xử lý:**
            1.  **Lấy danh sách cũ:** Truy vấn CSDL để lấy tất cả các bản ghi liên kết hiện có (`findAllByRecommendationIdAndIsDeletedFalse`).
            2.  **Lấy danh sách ID mới:** Lấy tất cả ID từ danh sách trong DTO gửi lên.
            3.  **Xác định các mục cần xóa:** Lọc ra những bản ghi trong *danh sách cũ* mà ID của chúng không có trong *danh sách mới*. Đánh dấu các bản ghi này là `isDeleted = true` và lưu lại.
            4.  **Xác định các mục cần thêm:** Lọc ra những DTO trong *danh sách mới* mà ID của chúng không có trong *danh sách cũ*. Tạo các entity mới cho chúng và lưu vào CSDL.

---

## 3. `deleteRecommendation`

*   **Mô tả chung:** Xóa mềm một khuyến nghị và tất cả các dữ liệu liên quan trực tiếp.

*   **Các hàm liên quan và chi tiết xử lý:**

    *   **`recommendationRepository.save(entity)`**
        *   **Mô tả:** Cập nhật bản ghi `Recommendation` để đánh dấu đã xóa.
        *   **Chi tiết:** Cập nhật các trường: `isDeleted = true`, `deletedAt = LocalDateTime.now()`, `deletedById = currentUser.getId()`.

    *   **`recommendationWorkItemRepository.findAll...` và `attachmentRepository.findAll...`**
        *   **Mô tả:** Tìm tất cả các bản ghi liên kết (work item, tệp đính kèm) vẫn còn đang hoạt động (`isDeleted = false`) của khuyến nghị này.
        *   **Truy vấn:** `SELECT * FROM recommendation_work_item WHERE recommendation_id = ? AND is_deleted = false`. Tương tự cho bảng `attachment`.

    *   **`...Repository.saveAll(...)`**
        *   **Mô tả:** Cập nhật hàng loạt các bản ghi liên kết để đánh dấu chúng đã bị xóa (`isDeleted = true`).

---

## 4. `getRecommendationById` và `searchRecommendations`

Hai hàm này đều sử dụng chung một cơ chế làm giàu dữ liệu cốt lõi.

*   **Các hàm liên quan và chi tiết xử lý:**

    *   **`RecommendationSpecifications.buildSpecification()` (dùng trong `search`)**
        *   **Mô tả:** Xây dựng một tập hợp các điều kiện truy vấn `WHERE` động dựa trên các tham số tìm kiếm.
        *   **Chi tiết:** Nó tạo ra các `Predicate` (điều kiện) cho JPA. Ví dụ:
            *   Nếu `request.getRecommendationTitle()` có giá trị, nó sẽ thêm điều kiện `recommendationTitle LIKE '%value%'`.
            *   Nếu `request.getProjectId()` có giá trị, nó sẽ thêm điều kiện `projectId = value`.
            *   Nếu `request.getCreatedAtFrom()` có giá trị, nó sẽ thêm điều kiện `createdAt >= value`.
            *   Các điều kiện này được kết hợp với nhau bằng toán tử `AND`.

    *   **`recommendationRepository.findAll(spec, pageable)` (dùng trong `search`)**
        *   **Mô tả:** Thực thi truy vấn tìm kiếm có phân trang, sắp xếp và các điều kiện động.
        *   **Chi tiết:** JPA sẽ tạo và thực thi 2 câu lệnh SQL:
            1.  `SELECT count(...) FROM recommendation WHERE ...` (các điều kiện từ `spec`).
            2.  `SELECT * FROM recommendation WHERE ... ORDER BY ... LIMIT ... OFFSET ...` (điều kiện từ `spec`, sắp xếp và phân trang từ `pageable`).

    *   **`recommendationMapper.mapToRecommendationWorkItem(List<Recommendation>)` (Hàm làm giàu dữ liệu)**
        *   **Mô tả:** Đây là hàm tối ưu hóa hiệu năng, chuyển đổi danh sách `Recommendation` thô thành danh sách `RecommendationDto` đầy đủ thông tin bằng cách giảm thiểu số lần truy vấn CSDL.
        *   **Chi tiết luồng xử lý (Batch Fetching):**
            1.  **Thu thập ID:** Duyệt qua danh sách `Recommendation` đầu vào, thu thập tất cả các ID khóa ngoại cần thiết vào các `List<Long>` riêng biệt (ví dụ: `projectIds`, `itemIds`, `phaseIds`, `recommendationTypeIds`, `userIds`).
            2.  **Truy vấn hàng loạt:** Thực hiện một truy vấn duy nhất cho mỗi loại ID đã thu thập.
                *   `projectRepo.findAllByIdInAndIsDeletedFalse(projectIds)` -> `SELECT * FROM project WHERE id IN (...) AND is_deleted = false`
                *   `workItemRepo.findAllByRecommendationIdInAndIsDeletedFalse(recommendationIds)` -> `SELECT * FROM work_item WHERE id IN (SELECT work_item_id FROM recommendation_work_item WHERE recommendation_id IN (...))`
                *   `sysUserRepo.findAllByIsDeletedFalse()` -> `SELECT * FROM sys_user WHERE is_deleted = false` (lấy tất cả người dùng)
                *   Tương tự cho các repository khác (`phaseRepo`, `projectItemRepo`, `recommendationTypeRepo`...).
            3.  **Tạo Map để tra cứu nhanh:** Chuyển đổi kết quả của mỗi truy vấn thành một `Map` với key là ID và value là đối tượng Entity (ví dụ: `Map<Long, Project>`). Việc này giúp tra cứu thông tin chi tiết của một đối tượng bằng ID của nó với độ phức tạp O(1) thay vì phải truy vấn lại. Với các mối quan hệ một-nhiều (như work items), nó tạo ra `Map<Long, List<Entity>>`.
            4.  **Ánh xạ sang DTO (`toDto`):** Lặp qua từng `Recommendation` trong danh sách đầu vào. Với mỗi `Recommendation`, nó tạo một `RecommendationDto` và điền thông tin:
                *   Dùng `ModelMapper` cho các trường cơ bản.
                *   Dùng các `Map` đã tạo ở bước 3 để lấy các đối tượng đầy đủ (như `Project`, `SysUser`, `WorkItem`) dựa trên ID có trong `Recommendation` và gán chúng vào DTO. Ví dụ: `dto.setProject(modelMapper.map(projectMap.get(recommendation.getProjectId()), ProjectDto.class))`.
                *   Quá trình này không phát sinh thêm bất kỳ truy vấn CSDL nào.
