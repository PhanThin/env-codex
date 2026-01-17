package vn.com.viettel.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
public class CatSurveyEquipmentSearchRequestDto {

    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;
    private String equipmentCode;
    private String equipmentName;
    private Long manageUnitId;
    private String isActive;


}
