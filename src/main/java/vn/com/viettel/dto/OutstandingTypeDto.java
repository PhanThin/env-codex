package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link vn.com.viettel.entities.CatOutstandingType}
 */
@Getter
@Setter
@NoArgsConstructor
public class OutstandingTypeDto implements Serializable {
    Long id;
    @NotNull
    @Size(max = 50)
    String typeCode;
    String typeName;
    Boolean isActive;
}
