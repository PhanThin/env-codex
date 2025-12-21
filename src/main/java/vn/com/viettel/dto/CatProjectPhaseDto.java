package vn.com.viettel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link vn.com.viettel.entities.CatProjectPhase}
 */
@Getter
@Setter
@NoArgsConstructor
public class CatProjectPhaseDto implements Serializable {
    Long id;
    @NotNull
    ProjectDto project;
    @NotNull
    @Size(max = 50)
    String phaseCode;
    @NotNull
    @Size(max = 250)
    String phaseName;
    @NotNull
    Boolean isActive;
}
