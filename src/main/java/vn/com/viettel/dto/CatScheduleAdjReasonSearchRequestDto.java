package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Schema(name = "CatScheduleAdjReasonSearchRequestDto", description = "DTO tìm kiếm lý do điều chỉnh tiến độ")
@Getter
@Setter
public class CatScheduleAdjReasonSearchRequestDto {

    @Schema(name = "Trang", example = "0")
    private Integer page;

    @Schema(name = "Kích thước trang", example = "20")
    private Integer size;

    @Schema(name = "Trường sort", example = "createdAt", allowableValues = "reasonCode,reasonName,createdAt,updatedAt,isActive")
    private String sortBy;

    @Schema(name = "Chiều sort", example = "DESC", allowableValues = "ASC,DESC")
    private String sortDirection;

    @Schema(description = "Tìm từ ngày tạo (dd-MM-yyyy)", example = "20-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdFrom;

    @Schema(description = "Tìm đến ngày tạo (dd-MM-yyyy)", example = "22-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdTo;

    @Schema(name = "Từ khóa tìm kiếm", example = "bàn giao")
    private String keyword;

    @Schema(name = "Trạng thái hiệu lực", example = "true")
    private Boolean isActive;
}
