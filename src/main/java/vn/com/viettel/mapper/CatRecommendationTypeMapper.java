package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.com.viettel.dto.CatRecommendationTypeDto;
import vn.com.viettel.entities.CatRecommendationType;

/**
 * MapStruct mapper for CAT_RECOMMENDATION_TYPE.
 */
@Mapper(componentModel = "spring")
public interface CatRecommendationTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    CatRecommendationType toEntity(CatRecommendationTypeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(@MappingTarget CatRecommendationType entity, CatRecommendationTypeDto dto);

    CatRecommendationTypeDto toDto(CatRecommendationType entity);
}
