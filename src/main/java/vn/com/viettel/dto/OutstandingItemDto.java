package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.viettel.entities.OutstandingAlertConfigDto;

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
    ProjectDto projectDto; // dự án

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Giai đoạn dự án"
    )
    CatProjectPhaseDto phaseDto; // giai đoạn dự án

    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Hạng mục dự án"
    )
    ProjectItemDto projectItemDto; // hạng mục dự án

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Công việc"
    )
    WorkItemDto workItemDto;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Loại tồn tại"
    )
    OutstandingTypeDto outstandingTypeDto;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Mức độ quan trọng"
    )
    PriorityDto priorityDto;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin người tạo"
    )
    UserDto createdByUserDto;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin đơn vị tạo"
    )
    OrgDto createdOrgDto;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian tạo"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createdAt;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Thông tin người xử lý"
    )
    UserDto assignedUserDto;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Thông tin đơn vị xử lý"
    )
    OrgDto assignedOrgDto;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Hạn xử lý"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate deadline;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Trạng thái xử lý"
    )
    StatusDto statusDto;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian cập nhật cuối"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime lastUpdate;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin người cập nhật cuối"
    )
    UserDto lastUpdateByDto;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Danh sách file đính kèm"
    )
    List<AttachmentDto> attachments;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Danh sách cấu hình cảnh báo"
    )
    List<OutstandingAlertConfigDto> outstandingAlertConfigs;

    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Danh sách file đã xóa"
    )
    List<AttachmentDto> deletedAttachments;
}
