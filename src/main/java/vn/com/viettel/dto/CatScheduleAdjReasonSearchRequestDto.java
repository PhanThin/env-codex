package vn.com.viettel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "CatScheduleAdjReasonSearchRequestDto", description = "DTO tìm kiếm lý do điều chỉnh tiến độ")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatScheduleAdjReasonSearchRequestDto {

    @Schema(name = "Trang", example = "0")
    private Integer page;

    @Schema(name = "Kích thước trang", example = "20")
    private Integer size;

    @Schema(name = "Trường sort", example = "createdAt", allowableValues = "reasonCode,reasonName,createdAt,updatedAt,isActive")
    private String sortBy;

    @Schema(name = "Chiều sort", example = "DESC", allowableValues = "ASC,DESC")
    private String sortDirection;

    @Schema(name = "Từ ngày tạo (yyyy-MM-dd hoặc yyyy-MM-dd'T'HH:mm:ss.SSS)", example = "2026-01-01")
    private String createdAtFrom;

    @Schema(name = "Đến ngày tạo (yyyy-MM-dd hoặc yyyy-MM-dd'T'HH:mm:ss.SSS)", example = "2026-01-31")
    private String createdAtTo;

    @Schema(name = "Mã lý do", example = "TB-")
    private String reasonCode;

    @Schema(name = "Tên lý do", example = "bàn giao")
    private String reasonName;

    @Schema(name = "Trạng thái hiệu lực", example = "true")
    private Boolean isActive;
}
