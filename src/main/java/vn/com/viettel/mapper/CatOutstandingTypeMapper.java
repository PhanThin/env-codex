package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import vn.com.viettel.dto.CatOutstandingTypeDto;
import vn.com.viettel.dto.OutstandingTypeDto;
import vn.com.viettel.entities.CatOutstandingType;

/**
 * MapStruct mapper for CAT_OUTSTANDING_TYPE.
 */
@Mapper(componentModel = "spring")
public interface CatOutstandingTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    CatOutstandingType toEntity(OutstandingTypeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget CatOutstandingType entity, OutstandingTypeDto dto);

    OutstandingTypeDto toDto(CatOutstandingType entity);
}
