package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import vn.com.viettel.entities.Attachment;

import java.io.Serializable;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto implements Serializable {
    Long id;
    Long referenceId;
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
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    LocalDateTime uploadedAt;
    Long uploadedBy;
}