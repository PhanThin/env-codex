package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectTypeDto {
    Long id;
    @NotNull
    @Size(max = 250)
    String projectTypeName;
}
