package vn.com.viettel.mapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import vn.com.viettel.dto.ProjectDto;
import vn.com.viettel.dto.ProjectItemDto;
import vn.com.viettel.entities.Project;
import vn.com.viettel.entities.ProjectItem;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProjectItemMapper {

    private final ModelMapper modelMapper;

    @PostConstruct
    private void configure() {

        // DTO -> Entity
        if (modelMapper.getTypeMap(ProjectItemDto.class, ProjectItem.class) == null) {
            modelMapper.typeMap(ProjectItemDto.class, ProjectItem.class)
                    .addMappings(mapper -> {
                        mapper.skip(ProjectItem::setId);
                        mapper.skip(ProjectItem::setProjectId);
                        mapper.skip(ProjectItem::setCreatedAt);
                        mapper.skip(ProjectItem::setCreatedBy);
                        mapper.skip(ProjectItem::setUpdatedAt);
                        mapper.skip(ProjectItem::setUpdatedBy);
                        mapper.skip(ProjectItem::setIsDeleted);
                        mapper.skip(ProjectItem::setIsActive);
                    });
        }

        // Entity -> DTO
        if (modelMapper.getTypeMap(ProjectItem.class, ProjectItemDto.class) == null) {
            modelMapper.typeMap(ProjectItem.class, ProjectItemDto.class)
                    .addMappings(mapper -> {
                        mapper.skip(ProjectItemDto::setProject);
                    });
        }
    }

    /**
     * Entity -> DTO (không FK, map Project thủ công)
     */
    public ProjectItemDto toDto(
            ProjectItem entity,
            Map<Long, Project> projectMap
    ) {
        if (entity == null) return null;

        ProjectItemDto dto = modelMapper.map(entity, ProjectItemDto.class);

        if (projectMap != null
                && entity.getProjectId() != null
                && projectMap.containsKey(entity.getProjectId())) {

            ProjectDto projectDto =
                    modelMapper.map(projectMap.get(entity.getProjectId()), ProjectDto.class);

            dto.setProject(projectDto);
        }

        return dto;
    }

    /**
     * DTO -> Entity (create)
     */
    public ProjectItem toEntity(ProjectItemDto dto, Long projectId) {
        if (dto == null) return null;

        ProjectItem entity = modelMapper.map(dto, ProjectItem.class);
        entity.setProjectId(projectId);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setIsActive(Boolean.TRUE);

        return entity;
    }

    /**
     * DTO -> Entity (update)
     */
    public void updateEntity(
            ProjectItemDto dto,
            ProjectItem entity
    ) {
        if (dto == null || entity == null) return;

        modelMapper.map(dto, entity);
    }
}
