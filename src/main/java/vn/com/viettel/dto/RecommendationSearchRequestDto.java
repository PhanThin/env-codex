package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RecommendationSearchRequestDto {

    // Từ khóa - tìm theo tên kiến nghị
    private String keyword;

    // Dự án (Toàn bộ nếu null)
    private Long projectId;

    // Hạng mục (Toàn bộ nếu null)
    private Long itemId;

    // Công việc (Toàn bộ nếu null)
    private Long workItemId;

    // Loại kiến nghị (Toàn bộ nếu null)
    private Long recommendationTypeId;

    // Mức độ quan trọng: VERY_IMPORTANT, IMPORTANT, NORMAL, hoặc ALL/null = Toàn bộ
    private String priority;

    // Trạng thái: NEW, IN_PROGRESS, DONE, hoặc ALL/null = Toàn bộ
    private String status;

    // Đơn vị (map với createdOrgId) - Toàn bộ nếu null
    private Long orgId;

    // Người tạo - Toàn bộ nếu null
    private Long createdById;

    // Người xử lý - Toàn bộ nếu null
    private Long closedById;

    // Thời gian tạo (lọc theo createdAt)
    @JsonFormat(pattern = "ddMMyyyy")
    private LocalDate createdFrom;

    @JsonFormat(pattern = "ddMMyyyy")
    private LocalDate createdTo;

    // Hạn xử lý (lọc theo deadline)
    @JsonFormat(pattern = "ddMMyyyy")
    private LocalDate deadlineFrom;

    @JsonFormat(pattern = "ddMMyyyy")
    private LocalDate deadlineTo;

    // Phân trang
    private Integer page = 0;      // mặc định trang 0
    private Integer size = 20;     // mặc định 20 bản ghi
    private String sortBy = "createdAt";
    @Pattern(
            regexp = "(?i)ASC|DESC",
            message = "sortDirection chỉ được phép là ASC hoặc DESC"
    )
    private String sortDirection = "DESC"; // ASC/DESC
}
