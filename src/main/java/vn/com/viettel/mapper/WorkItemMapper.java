package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.entities.WorkItem;

/**
 * MapStruct mapper for WORK_ITEM.
 */
@Mapper(componentModel = "spring")
public interface WorkItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    WorkItem toEntity(WorkItemDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget WorkItem entity, WorkItemDto dto);

    WorkItemDto toDto(WorkItem entity);
}
