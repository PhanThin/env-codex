package vn.com.viettel.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO dùng chung cho create / update / response.
 * Lưu ý: client KHÔNG được gửi các trường audit (createdBy/createdAt/updatedBy/updatedAt).
 * Service sẽ tự set; nếu client có gửi lên cũng sẽ bị ignore.
 */
@Getter
@Setter
public class ProjectTypeDto implements Serializable {

    private Long id;
    private String projectTypeName;

    private Long updatedBy;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private LocalDateTime createdAt;

    private Boolean isActive;  // 'Y'/'N'
    private Boolean isDeleted; // 'Y'/'N'


}
