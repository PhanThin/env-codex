package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Đối tượng chứa điều kiện tìm kiếm kiến nghị")
public class RecommendationSearchRequestDto {

    @Schema(description = "Từ khóa tìm kiếm (theo tên kiến nghị)", example = "Kiến nghị về tiến độ")
    private String keyword;

    @Schema(description = "ID của dự án. Null nếu tìm kiếm toàn bộ")
    private Long projectId;

    @Schema(description = "ID của hạng mục. Null nếu tìm kiếm toàn bộ")
    private Long itemId;

    @Schema(description = "ID của công việc. Null nếu tìm kiếm toàn bộ")
    private Long workItemId;

    @Schema(description = "ID của loại kiến nghị. Null nếu tìm kiếm toàn bộ")
    private Long recommendationTypeId;

    @Schema(description = "Mức độ quan trọng. Null nếu tìm kiếm toàn bộ", allowableValues = {"HIGH_PRIORITY", "PRIORITY", "LOW_PRIORITY"}, example = "LOW_PRIORITY")
    private String priority;

    @Schema(description = "Trạng thái kiến nghị. Null nếu tìm kiếm toàn bộ", allowableValues = {"NEW", "IN_PROGRESS", "DONE", "CLOSED", "REJECTED", "ACCEPTED"}, example = "NEW")
    private String status;

    @Schema(description = "ID đơn vị tạo. Null nếu tìm kiếm toàn bộ")
    private Long orgId;

    @Schema(description = "ID người tạo. Null nếu tìm kiếm toàn bộ")
    private Long createdById;

    @Schema(description = "ID người xử lý kiến nghị. Null nếu tìm kiếm toàn bộ")
    private Long processedById;

    @Schema(description = "Tìm từ ngày tạo (dd-MM-yyyy)", example = "20-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdFrom;

    @Schema(description = "Tìm đến ngày tạo (dd-MM-yyyy)", example = "22-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdTo;

    @Schema(description = "Tìm từ ngày hết hạn (dd-MM-yyyy)", example = "20-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate deadlineFrom;

    @Schema(description = "Tìm đến ngày hết hạn (dd-MM-yyyy)", example = "20-12-2023")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate deadlineTo;

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
