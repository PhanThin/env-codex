package vn.com.viettel.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Long id;

    /**
     * Type code.
     */
    @Size(max = 50)
    private String typeCode;

    /**
     * Type name.
     */
    @NotBlank
    @Size(max = 250)
    private String typeName;

    private UserDto  createdByUser;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime createdAt;

    private String isActive;

    private Boolean isDeleted;

    private UserDto  updatedByUser;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime updatedAt;
    @Size(max = 500)
    String note;
}
