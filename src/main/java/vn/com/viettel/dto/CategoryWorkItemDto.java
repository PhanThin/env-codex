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
@Schema(name = "CategoryWorkItemDto", description = "Thông tin hạng mục công việc")
public class CategoryWorkItemDto {
    Long id;

    @NotNull(message = "Mã hạng mục công việc là bắt buộc")
    @Size(max = 250)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Mã hạng mục công việc"
    )
    String categoryWorkItemCode;

    @NotNull(message = "Tên hạng mục công việc là bắt buộc")
    @Size(max = 250)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Tên hạng mục công việc"
    )
    String categoryWorkItemName;

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

}
