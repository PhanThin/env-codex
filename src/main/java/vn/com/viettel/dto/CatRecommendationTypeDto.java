package vn.com.viettel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private UserDto createdByUser;
    private UserDto updatedByUser;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    LocalDateTime createdAt;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    LocalDateTime updatedAt;
}

