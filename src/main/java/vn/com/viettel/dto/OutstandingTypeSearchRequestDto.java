package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Schema(name = "OutstandingTypeSearchRequestDto", description = "DTO tìm kiếm loại tồn tại")
@Getter
@Setter
public class OutstandingTypeSearchRequestDto {

    /**
     * Từ khóa tìm kiếm theo TYPE_NAME
     */
    @Schema(name = "Từ khóa tìm kiếm")
    private String keyword;

//    /**
//     * Trạng thái hiệu lực:
//     * - ALL (mặc định)
//     * - ACTIVE
//     * - INACTIVE
//     */
//    private String status;

    @Schema(name = "Trạng thái hiệu lực", example = "true")
    private Boolean isActive;

    /**
     * Lọc theo ngày tạo (từ ngày)
     */
    @Schema(description = "Tìm từ ngày tạo (dd-MM-yyyy)", example = "20-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdFrom;

    /**
     * Lọc theo ngày tạo (đến ngày)
     */
    @Schema(description = "Tìm đến ngày tạo (dd-MM-yyyy)", example = "22-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdTo;

    @Schema(name = "Trang", example = "0")
    private Integer page;

    @Schema(name = "Kích thước trang", example = "20")
    private Integer size;

    @Schema(name = "Trường sort", example = "createdAt", allowableValues = "typeName,createdAt,updatedAt,isActive")
    private String sortBy;

    @Schema(name = "Chiều sort", example = "DESC", allowableValues = "ASC,DESC")
    private String sortDirection;
}
