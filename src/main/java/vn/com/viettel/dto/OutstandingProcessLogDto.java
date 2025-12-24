package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for OUTSTANDING_PROCESS_LOG create/update/response.
 * Create & update share the same DTO as required.
 */
@Getter
@Setter
@Builder
public class OutstandingProcessLogDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long processId;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long outstandingId;

    /**
     * SAVE_RESULT or SEND_FOR_ACCEPTANCE.
     */
    @NotBlank
    @Size(max = 30)
    @Schema(description = "Loại hành động", allowableValues = {"SAVE_RESULT", "SEND_FOR_ACCEPTANCE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private OutstandingProcessActionEnum actionType;

    /**
     * Processing content/result.
     */
    @NotBlank
    @Size(max = 2000)
    @Schema(description = "Nội dung xử lý/kết quả", requiredMode = Schema.RequiredMode.REQUIRED)
    private String processContent;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UserDto createdBy;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UserDto updatedBy;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Danh sách attachment")
    private List<AttachmentDto> attachments;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Danh sách attachment bị xoá")
    private List<AttachmentDto> deletedAttachments;
}
