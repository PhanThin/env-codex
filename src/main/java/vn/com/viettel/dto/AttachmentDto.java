package vn.com.viettel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {
    private Long id;
    private Long referenceId;
    private String referenceType;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private String fileUrl;
    private String filePath;
    private LocalDateTime uploadedAt;
    private Long uploadedBy;
}