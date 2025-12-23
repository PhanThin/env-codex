package vn.com.viettel.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for OUTSTANDING_PROCESS_LOG create/update/response.
 * Create & update share the same DTO as required.
 */
@Getter
@Setter
@Builder
public class OutstandingProcessLogDto {

    private Long processId;

    private Long outstandingId;

    /**
     * SAVE_RESULT or SEND_FOR_ACCEPTANCE.
     */
    @NotBlank
    @Size(max = 30)
    private String actionType;

    /**
     * Processing content/result.
     */
    @NotBlank
    @Size(max = 2000)
    private String processContent;

    private Long updatedBy;
    private LocalDateTime updatedAt;

    private Boolean isDeleted;
}
