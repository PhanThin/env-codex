package vn.com.viettel.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendationTypeSearchRequestDto {

    /** Từ khóa tìm kiếm theo TYPE_NAME */
    private String keyword;

    /**
     * Trạng thái hiệu lực:
     * - ALL (mặc định)
     * - ACTIVE
     * - INACTIVE
     */
    private String status;

    /** Lọc theo ngày tạo (từ ngày) */
    private LocalDate createdFrom;

    /** Lọc theo ngày tạo (đến ngày) */
    private LocalDate createdTo;

    /** Paging */
    private Integer page;
    private Integer size;

    /** Sorting */
    private String sortBy;
    private String sortDirection;
}
