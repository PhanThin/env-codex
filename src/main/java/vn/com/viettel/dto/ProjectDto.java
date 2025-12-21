package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link vn.com.viettel.entities.Project}
 */
@Getter
@Setter
@NoArgsConstructor
public class ProjectDto implements Serializable {
    Long id;
    @NotNull
    @Size(max = 50)
    String projectCode;
    @NotNull
    @Size(max = 250)
    String projectName;
    Boolean isActive;
}
