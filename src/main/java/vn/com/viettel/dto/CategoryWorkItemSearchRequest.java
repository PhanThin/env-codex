package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@Schema(description = "Đối tượng chứa điều kiện tìm kiếm hạng mục công việc")
public class CategoryWorkItemSearchRequest {
    @Schema(description = "Từ khóa tìm kiếm có thể là ID, mã, tên hạng mục công việc")
    private String keyword;

    @Schema(description = "ID của loại dự án. Null nếu tìm kiếm toàn bộ")
    private Long projectTypeId;

    @Schema(description = "ID của giai đoạn. Null nếu tìm kiếm toàn bộ")
    private Long phaseId;

    @Schema(description = "ID của hạng mục dự án. Null nếu tìm kiếm toàn bộ")
    private Long projectItemId;

    @Schema(description = "ID của đơn vị khối lượng. Null nếu tìm kiếm toàn bộ")
    private Long unitId;

    @Schema(description = "Trạng thái hiệu lực. Null nếu tìm kiếm toàn bộ")
    private Boolean isActive;

    @Schema(description = "Tìm từ ngày tạo (dd-MM-yyyy)", example = "20-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdFrom;

    @Schema(description = "Tìm đến ngày tạo (dd-MM-yyyy)", example = "22-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdTo;

    @Schema(description = "Số thứ tự trang (bắt đầu từ 0)", example = "0")
    private Integer page = 0;

    @Schema(description = "Số bản ghi trên mỗi trang", example = "20")
    private Integer size = 20;

    @Schema(description = "Trường dùng để sắp xếp", example = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "Hướng sắp xếp", allowableValues = {"ASC", "DESC"}, example = "DESC")
    @Pattern(
            regexp = "(?i)ASC|DESC",
            message = "sortDirection chỉ được phép là ASC hoặc DESC"
    )
    private String sortDirection = "DESC"; // ASC/DESC
}
