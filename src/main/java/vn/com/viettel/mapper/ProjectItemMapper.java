package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.com.viettel.dto.ProjectItemDto;
import vn.com.viettel.entities.ProjectItem;

/**
 * MapStruct mapper for PROJECT_ITEM.
 */
@Mapper(componentModel = "spring")
public interface ProjectItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    ProjectItem toEntity(ProjectItemDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget ProjectItem entity, ProjectItemDto dto);

    @Mapping(target = "project", source = "project")
    ProjectItemDto toDto(ProjectItem entity);
}
