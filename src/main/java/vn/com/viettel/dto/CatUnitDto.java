package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatUnitDto {
    Long id;
    @Size(max = 50)
    String unitName;
    @Size(max = 50)
    String unitType;
    @NotNull
    Boolean isActive;
}
