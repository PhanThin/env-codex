package vn.com.viettel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Thông tin kiến nghị")
public class RecommendationDto implements Serializable {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String recommendationCode;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 250)
    private String recommendationTitle;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 500)
    private String content;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private CatRecommendationTypeDto recommendationType;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private PriorityDto priority;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate deadline;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private ProjectDto project;

    private ProjectItemDto projectItem;

    private CatProjectPhaseDto phase;

    private List<WorkItemDto> workItems;

    private List<UserDto> assignedUsers;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UserDto currentProcessUser;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UserDto createdByUser;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UserDto lastUpdateByUser;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UserDto closedByUser;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastUpdateAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime closedAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<AttachmentDto> attachments;

    private List<Long> deletedAttachments;
}
