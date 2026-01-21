package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Dùng chung cho create/update/response.
 * Các trường audit để READ_ONLY, backend tự set.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(name = "CatSurveyEquipmentDto", description = "DTO thông tin thiết bị khảo sát")
public class CatSurveyEquipmentDto {

    @Schema(description = "ID thiết bị", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long equipmentId;

    @Schema(description = "Mã thiết bị", example = "EQ-0001")
    private String equipmentCode;

    @Schema(description = "Tên thiết bị", example = "Máy toàn đạc")
    private String equipmentName;

    @Schema(description = "Mã model", example = "MODEL-123")
    private String modelCode;

//    @Schema(description = "ID hãng sản xuất", example = "10")
//    private Long manufacturerId;
//
//    @Schema(description = "Tên hãng sản xuất", example = "Leica")
//    private String manufacturerName;

//    @Schema(description = "Năm sản xuất", example = "2022")
//    private Integer manufactureYear;

    @Schema(description = "Đơn vị tính (UOM)", example = "5")
    private CatUnitDto unit;

//    @Schema(description = "ID đơn vị tính (UOM)", example = "5")
//    private Long uomId;
//
//    @Schema(description = "Tên đơn vị tính", example = "Cái")
//    private String uomName;

//    @Schema(description = "ID đơn vị quản lý", example = "100")
//    private Long manageUnitId;
//
//    @Schema(description = "Tên đơn vị quản lý", example = "Trung tâm Khảo sát 1")
//    private String manageUnitName;

    @Schema(description = "Ghi chú", example = "Thiết bị đang bảo trì định kỳ")
    private String note;

    @Schema(description = "Thời điểm tạo", example = "2024-01-01T10:00:00.000", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime createdAt;

    @Schema(description = "Thông tin người tạo", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserDto createdByUser;

    @Schema(description = "Thời điểm cập nhật", example = "2024-01-02T15:30:00.000", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime updatedAt;

    @Schema(description = "Thông tin người cập nhật", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserDto updatedByUser;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}
