package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import vn.com.viettel.dto.CatRecommendationSourceDto;
import vn.com.viettel.entities.CatRecommendationSource;
/**
 * MapStruct mapper for CAT_RECOMMENDATION_SOURCE.
 */
@Mapper(componentModel = "spring")
public interface CatRecommendationSourceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    CatRecommendationSource toEntity(CatRecommendationSourceDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget CatRecommendationSource entity, CatRecommendationSourceDto dto);

    CatRecommendationSourceDto toDto(CatRecommendationSource entity);
}
