package vn.com.viettel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link vn.com.viettel.entities.OutstandingItem}
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(name = "OutstandingItem", description = "Thông tin tồn tại")
public class OutstandingItemDto implements Serializable {
    Long id;

    @NotNull(message = "Tên tồn tại là bắt buộc")
    @Size(max = 250)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Tên tồn tại"
    )
    String outstandingTitle;

    @Size(max = 50)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Loại nghiệm thu tồn tại"
    )
    AcceptanceTypeDto acceptanceType;

    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Mã tham chiếu trường hợp nghiệm thu điện tử"
    )
    String acceptanceRefId;

    @Size(max = 1000)
    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Đường link file nghiệm thu trường hợp nghiệm thu điện tử"
    )
    String acceptanceFileUrl;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Dự án"
    )
    ProjectDto project; // dự án

    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Hạng mục dự án"
    )
    ProjectItemDto projectItem; // hạng mục dự án

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Danh sách công việc"
    )
    List<WorkItemDto> workItems;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Loại tồn tại"
    )
    OutstandingTypeDto outstandingType;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Mức độ quan trọng"
    )
    PriorityDto priority;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin người tạo"
    )
    UserDto createdByUser;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin đơn vị tạo"
    )
    OrgDto createdOrg;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian tạo"
    )
    LocalDateTime createdAt;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Thông tin người xử lý"
    )
    UserDto assignedUser;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Thông tin đơn vị xử lý"
    )
    OrgDto assignedOrg;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Hạn xử lý"
    )
    LocalDate deadline;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Trạng thái xử lý"
    )
    StatusDto status;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian cập nhật cuối"
    )
    LocalDateTime lastUpdate;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin người cập nhật cuối"
    )
    UserDto lastUpdateBy;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Danh sách file đính kèm"
    )
    List<AttachmentDto> attachments;
}
