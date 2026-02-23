package vn.com.viettel.mapper;

import org.springframework.stereotype.Component;
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.Recommendation;

@Component
public class RecommendationMapper {

    public Recommendation toEntityForCreate(RecommendationDto dto, Long currentUserId, Long currentOrgId) {
        Recommendation entity = new Recommendation();
        entity.setRecommendationTitle(dto.getRecommendationTitle() != null ? dto.getRecommendationTitle().trim() : null);
        entity.setContent(dto.getContent() != null ? dto.getContent().trim() : null);
        entity.setRecommendationTypeId(dto.getRecommendationType() != null ? dto.getRecommendationType().getId() : null);
        entity.setPriority(dto.getPriority() != null ? dto.getPriority().getCode() : null);
        entity.setDeadline(dto.getDeadline());
        entity.setProjectId(dto.getProject() != null ? dto.getProject().getId() : null);
        entity.setItemId(dto.getProjectItem() != null ? dto.getProjectItem().getId() : null);
        entity.setPhaseId(dto.getPhase() != null ? dto.getPhase().getId() : null);
        entity.setCurrentProcessById(dto.getCurrentProcessUser() != null ? dto.getCurrentProcessUser().getId() : null);
        entity.setStatus(RecommendationStatusEnum.NEW.name());
        entity.setCreatedById(currentUserId);
        entity.setCreatedOrgId(currentOrgId);
        entity.setCreatedAt(java.time.LocalDateTime.now());
        entity.setLastUpdateBy(currentUserId);
        entity.setLastUpdateAt(java.time.LocalDateTime.now());
        entity.setIsDeleted(false);
        return entity;
    }

    public void updateEntityFromDto(RecommendationDto dto, Recommendation entity, Long currentUserId) {
        entity.setRecommendationTitle(dto.getRecommendationTitle() != null ? dto.getRecommendationTitle().trim() : null);
        entity.setContent(dto.getContent() != null ? dto.getContent().trim() : null);
        entity.setRecommendationTypeId(dto.getRecommendationType() != null ? dto.getRecommendationType().getId() : null);
        entity.setPriority(dto.getPriority() != null ? dto.getPriority().getCode() : null);
        entity.setDeadline(dto.getDeadline());
        entity.setProjectId(dto.getProject() != null ? dto.getProject().getId() : null);
        entity.setItemId(dto.getProjectItem() != null ? dto.getProjectItem().getId() : null);
        entity.setPhaseId(dto.getPhase() != null ? dto.getPhase().getId() : null);
        entity.setCurrentProcessById(dto.getCurrentProcessUser() != null ? dto.getCurrentProcessUser().getId() : null);
        entity.setLastUpdateBy(currentUserId);
        entity.setLastUpdateAt(java.time.LocalDateTime.now());
    }

    public RecommendationDto toDto(Recommendation entity) {
        RecommendationDto dto = new RecommendationDto();
        dto.setId(entity.getId());
        dto.setRecommendationCode(entity.getRecommendationCode());
        dto.setRecommendationTitle(entity.getRecommendationTitle());
        dto.setContent(entity.getContent());
        dto.setDeadline(entity.getDeadline());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastUpdateAt(entity.getLastUpdateAt());
        dto.setClosedAt(entity.getClosedAt());

        PriorityDto priorityDto = new PriorityDto();
        priorityDto.setCode(entity.getPriority());
        if (entity.getPriority() != null) {
            try {
                priorityDto.setName(PriorityEnum.valueOf(entity.getPriority()).getVietnameseName());
            } catch (Exception ignore) {
                priorityDto.setName(entity.getPriority());
            }
        }
        dto.setPriority(priorityDto);

        CatRecommendationTypeDto typeDto = new CatRecommendationTypeDto();
        typeDto.setId(entity.getRecommendationTypeId());
        dto.setRecommendationType(typeDto);

        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(entity.getProjectId());
        dto.setProject(projectDto);

        if (entity.getItemId() != null) {
            ProjectItemDto itemDto = new ProjectItemDto();
            itemDto.setId(entity.getItemId());
            dto.setProjectItem(itemDto);
        }
        if (entity.getPhaseId() != null) {
            CatProjectPhaseDto phaseDto = new CatProjectPhaseDto();
            phaseDto.setId(entity.getPhaseId());
            dto.setPhase(phaseDto);
        }
        if (entity.getCurrentProcessById() != null) {
            UserDto userDto = new UserDto();
            userDto.setId(entity.getCurrentProcessById());
            dto.setCurrentProcessUser(userDto);
        }
        return dto;
    }
}
