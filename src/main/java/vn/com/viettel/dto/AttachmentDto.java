package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import vn.com.viettel.entities.Attachment;

import java.io.Serializable;

/**
 * DTO for {@link Attachment}
 */
@Value
public class AttachmentDto implements Serializable {
    Long id;
    @NotNull
    Long referenceId;
    @NotNull
    @Size(max = 50)
    String referenceType;
    @NotNull
    @Size(max = 255)
    String fileName;
    @Size(max = 20)
    String fileExt;
    Long fileSize;
    @NotNull
    @Size(max = 1000)
    String fileUrl;
    @NotNull
    @Size(max = 500)
    String filePath;
}
