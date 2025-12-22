package vn.com.viettel.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for create/update and response of CAT_OUTSTANDING_TYPE.
 * Create & update share the same DTO as required.
 */
@Getter
@Setter
@Builder
public class CatOutstandingTypeDto {

    /**
     * Primary key.
     */
    private Long outstandingTypeId;

    /**
     * Type code.
     */
    @NotBlank
    @Size(max = 50)
    private String typeCode;

    /**
     * Type name.
     */
    @NotBlank
    @Size(max = 250)
    private String typeName;

    private Long createdBy;
    private LocalDateTime createdAt;

    private String isActive;

    private Boolean isDeleted;

    private Long updatedBy;
    private LocalDateTime updatedAt;
}
