package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CatSurveyEquipmentDto {

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private String modelCode;

    private Long manufacturerId;
    private String manufacturerName;
    private Integer manufactureYear;

    private Long uomId;
    private String uomName;

    private Long manageUnitId;
    private String manageUnitName;

    private String note;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime createdAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserDto createdByUser;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime updatedAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserDto updatedByUser;

    private String isActive;


}
