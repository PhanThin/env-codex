package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SysOrgDto dùng chung cho Create/Update/Response theo rule.
 * Lưu ý: Service sẽ tự set createdAt/updatedAt/isDeleted theo nghiệp vụ, không lấy từ client.
 */
@Getter
@Setter
@NoArgsConstructor
public class SysOrgDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orgCode;
    private String orgName;

    private Long updatedBy;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime updatedAt;

    private Long createdBy;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime createdAt;

    private Boolean isActive;
    private Boolean isDeleted;


}
