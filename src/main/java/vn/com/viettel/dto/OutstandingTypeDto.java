package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link vn.com.viettel.entities.CatOutstandingType}
 */
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OutstandingTypeDto implements Serializable {
    Long id;
    @NotNull
    @Size(max = 50)
    String typeCode;
    String typeName;
    Boolean isActive;
}
