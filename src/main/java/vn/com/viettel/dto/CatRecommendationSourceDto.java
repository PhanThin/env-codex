package vn.com.viettel.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for create/update and response of CAT_RECOMMENDATION_SOURCE.
 * Create & update share the same DTO as required.
 */
@Getter
@Setter
@Builder
public class CatRecommendationSourceDto {

    private Long sourceId;

    /**
     * Mã nguồn kiến nghị.
     */
    @NotBlank
    @Size(max = 50)
    private String sourceCode;

    /**
     * Tên nguồn kiến nghị.
     */
    @NotBlank
    @Size(max = 250)
    private String sourceName;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 1)
    private String isActive;

    @NotNull
    private LocalDateTime createdAt;

    private Long updatedBy;
    private LocalDateTime updatedAt;

    private Long createdBy;

    private Boolean isDeleted;
}
