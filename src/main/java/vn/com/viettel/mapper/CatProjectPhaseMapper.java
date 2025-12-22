package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.entities.CatProjectPhase;


/**
 * MapStruct mapper for CAT_PROJECT_PHASE.
 */
@Mapper(componentModel = "spring")
public interface CatProjectPhaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    CatProjectPhase toEntity(CatProjectPhaseDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget CatProjectPhase entity, CatProjectPhaseDto dto);

    CatProjectPhaseDto toDto(CatProjectPhase entity);
}
