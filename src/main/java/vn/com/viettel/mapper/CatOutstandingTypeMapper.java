package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import vn.com.viettel.dto.CatOutstandingTypeDto;
import vn.com.viettel.entities.CatOutstandingType;

/**
 * MapStruct mapper for CAT_OUTSTANDING_TYPE.
 */
@Mapper(componentModel = "spring")
public interface CatOutstandingTypeMapper {

    @Mapping(target = "isDeleted", ignore = true)
    // nếu DTO đã thêm createdByUser/updatedByUser:
//    @Mapping(target = "createdByUser", ignore = true)
//    @Mapping(target = "updatedByUser", ignore = true)
    CatOutstandingType toEntity(CatOutstandingTypeDto dto);

    @Mapping(target = "isDeleted", ignore = true)
//    @Mapping(target = "createdByUser", ignore = true)
//    @Mapping(target = "updatedByUser", ignore = true)
    void updateEntity(@MappingTarget CatOutstandingType entity, CatOutstandingTypeDto dto);

//    @Mapping(target = "createdByUser", ignore = true)
//    @Mapping(target = "updatedByUser", ignore = true)
    CatOutstandingTypeDto toDto(CatOutstandingType entity);
}
