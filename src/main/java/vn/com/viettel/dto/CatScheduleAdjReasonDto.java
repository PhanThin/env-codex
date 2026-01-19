package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(name = "CatScheduleAdjReasonDto", description = "DTO lý do điều chỉnh tiến độ")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatScheduleAdjReasonDto {

    @Schema(description = "ID lý do", example = "1")
    private Long reasonId;

    @Schema(description = "Mã lý do (TB-xxxxxx)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "TB-000001")
    @Size(max = 50)
    @Pattern(regexp = "^TB-[A-Za-z0-9_-]+$")
    private String reasonCode;

    @Schema(description = "Lý do điều chỉnh tiến độ", requiredMode = Schema.RequiredMode.REQUIRED, example = "Chậm bàn giao mặt bằng")
    @NotBlank
    @Size(max = 250)
    private String reasonName;

    @Schema(description = "Ghi chú", example = "Mặt bằng chưa được bàn giao đầy đủ")
    @Size(max = 500)
    private String note;

    @Schema(description = "Trạng thái hiệu lực", example = "true")
    private Boolean isActive;

    @Schema(description = "Ngày tạo", example = "2026-01-18T10:30:15.123")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @Schema(description = "Người tạo")
    private UserDto createdByUser;

    @Schema(description = "Ngày cập nhật", example = "2026-01-18T10:30:15.123")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    @Schema(description = "Người cập nhật")
    private UserDto updatedByUser;
}
