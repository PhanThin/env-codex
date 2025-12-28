package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link vn.com.viettel.entities.Recommendation}
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(name = "Recommendation", description = "Thông tin kiến nghị")
public class RecommendationDto implements Serializable {
    Long id;
    @Size(max = 50)
    String recommendationCode;
    @NotNull(message = "Tên kiến nghị là bắt buộc")
    @Size(max = 250)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Tên kiến nghị"
    )
    String recommendationTitle;
    @NotNull(message = "Nội dung kiến nghị là bắt buộc")
    @Size(max = 500)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Nội dung kiến nghị"
    )
    String content;
    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Dự án"
    )
    ProjectDto project; // dự án
    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Hạng mục dự án"
    )
    ProjectItemDto projectItem; // hạng mục dự án
    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Giai đoạn dự án"
    )
    CatProjectPhaseDto phase; // giai đoạn dự án

    @NotNull(message = "Loại kiến nghị là bắt buộc")
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Loại kiến nghị"
    )
    CatRecommendationTypeDto recommendationType;

    @NotNull(message = "Mức độ quan trọng là bắt buộc")
    @Size(max = 20)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Mức độ quan trọng",
            allowableValues = {"HIGH_PRIORITY", "PRIORITY", "LOW_PRIORITY"}, example = "LOW_PRIORITY"
    )
    PriorityDto priority;

    @NotNull(message = "Hạn xử lý là bắt buộc")
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Hạn xử lý"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate deadline;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Trạng thái kiến nghị"
    )
    StatusDto status;
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
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian tạo kiến nghị"
    )
    LocalDateTime createdAt;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian đóng kiến nghị"
    )
    LocalDateTime closedAt;
    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin người đóng kiến nghị"
    )
    UserDto closedByUser;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian cập nhật kiến nghị"
    )
    LocalDateTime lastUpdate;
    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin người cập nhật"
    )
    UserDto lastUpdateByUser;

    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Danh sách công việc"
    )
    List<WorkItemDto> workItems;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Danh sách người được giao xử lý"
    )
    List<UserDto> assignedUsers;

    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Người được giao xử lý hiện tại"
    )
    UserDto currentProcessUser;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Danh sách file đính kèm"
    )
    List<AttachmentDto> attachments;

    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Danh sách file đã xóa"
    )
    List<AttachmentDto> deletedAttachments;
}
