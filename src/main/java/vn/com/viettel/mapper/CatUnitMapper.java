package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import vn.com.viettel.dto.CatUnitDto;
import vn.com.viettel.entities.CatUnit;

@Mapper(componentModel = "spring")
public interface CatUnitMapper {

    CatUnitDto toDto(CatUnit entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    CatUnit toEntity(CatUnitDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntityFromDto(CatUnitDto dto, @MappingTarget CatUnit entity);
}
