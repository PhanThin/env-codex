
package vn.com.viettel.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for OUTSTANDING_ACCEPTANCE create/update/response.
 */
@Getter
@Setter
@Builder
public class OutstandingAcceptanceDto {

    private Long acceptanceId;

    private Long outstandingId;

    @NotBlank
    @Size(max = 20)
    private String result;

    @NotBlank
    @Size(max = 2000)
    private String acceptanceNote;

    @NotNull
    private UserDto acceptedByUser;

    @NotNull
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime acceptedAt;

    private Boolean isDeleted;

    private UserDto updatedByUser;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime updatedAt;

    private List<AttachmentDto> attachments;

    private List<AttachmentDto> deletedAttachments;
}
