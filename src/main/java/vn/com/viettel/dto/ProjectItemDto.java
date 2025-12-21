package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link vn.com.viettel.entities.ProjectItem}
 */
@Getter
@Setter
@NoArgsConstructor
public class ProjectItemDto implements Serializable {
    Long id;
    @NotNull
    ProjectDto project;
    @NotNull
    @Size(max = 50)
    String itemCode;
    @NotNull
    @Size(max = 250)
    String itemName;
}
