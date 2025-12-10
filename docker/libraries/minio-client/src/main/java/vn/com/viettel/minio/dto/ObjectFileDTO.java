package vn.com.viettel.minio.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ObjectFileDTO {
    String linkUrl;
    String linkUrlPublic;
    String filePath;
    String fileName;
    Long fileSize;
}
