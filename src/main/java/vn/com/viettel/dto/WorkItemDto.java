package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "WorkItemDto", description = "Thông tin công việc")
public class WorkItemDto {
    Long id;
    @NotNull(message = "Tên công việc là bắt buộc")
    @Size(max = 250)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Tên công việc"
    )
    String workItemName;
    @NotNull(message = "Mã công việc là bắt buộc")
    @Size(max = 250)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Mã công việc"
    )
    String workItemCode;

    @Size(max = 500)
    @Schema(
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            description = "Ghi chú"
    )
    String note;

    @NotNull(message = "Loại dự án là bắt buộc")
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Loại dự án"
    )
    ProjectTypeDto projectType;

    @NotNull(message = "Hạng mục dự án là bắt buộc")
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Hạng mục dự án"
    )
    ProjectItemDto projectItem;

    @NotNull(message = "Giai đoạn dự án là bắt buộc")
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Giai đoạn dự án"
    )
    CatProjectPhaseDto projectPhase;

    @NotNull(message = "Đơn vị khối lượng là bắt buộc")
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Đơn vị khối lượng"
    )
    CatUnitDto unit;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin cập nhật"
    )
    UserDto updatedBy;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian cập nhật hạng mục công việc"
    )
    LocalDateTime updatedAt;

    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thông tin người tạo"
    )
    UserDto createdBy;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    @Schema(
            accessMode = Schema.AccessMode.READ_ONLY,
            description = "Thời gian tạo hạng mục công việc"
    )
    LocalDateTime createdAt;

    @NotNull(message = "Trạng thái hoạt động là bắt buộc")
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Trạng thái hoạt động"
    )
    Boolean isActive;

    @NotNull(message = "Hạng mục công việc là bắt buộc")
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Hạng mục công việc"
    )
    CategoryWorkItemDto categoryWorkItem;
}
