package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CatSurveyEquipmentSearchRequestDto {

    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    @Schema(description = "Tìm từ ngày tạo (dd-MM-yyyy)", example = "20-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdFrom;

    @Schema(description = "Tìm đến ngày tạo (dd-MM-yyyy)", example = "22-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdTo;
    private String keyword;
    private Long manageUnitId;
    private Boolean isActive;


}
