package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CatUnitDto {

    private Long id;
    private String unitName;
    private String unitType;

    private Boolean isActive;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean isDeleted;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long createdBy;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long updatedBy;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;

}
