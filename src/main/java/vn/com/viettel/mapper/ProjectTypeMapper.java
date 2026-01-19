package vn.com.viettel.mapper;

import org.mapstruct.*;
import vn.com.viettel.dto.ProjectTypeDto;
import vn.com.viettel.entities.ProjectType;

@Mapper(componentModel = "spring")
public interface ProjectTypeMapper {

    ProjectTypeDto toDto(ProjectType entity);

    ProjectType toEntity(ProjectTypeDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "isDeleted", ignore = true),

    })
    void updateEntityFromDto(ProjectTypeDto dto, @MappingTarget ProjectType entity);
}
