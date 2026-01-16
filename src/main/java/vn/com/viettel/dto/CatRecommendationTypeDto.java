package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link vn.com.viettel.entities.CatRecommendationType}
 */
@Getter
@Setter
@NoArgsConstructor
public class CatRecommendationTypeDto implements Serializable {

    Long id;
    @Size(max = 50)
    String typeCode;
    @NotNull
    @Size(max = 250)
    String typeName;
    Boolean isActive;
    @Size(max = 500)
    String note;

    // === audit fields ===
    Long createdBy;
    LocalDateTime createdAt;
    Long updatedBy;
    LocalDateTime updatedAt;
}

