package vn.com.viettel.mapper;

import org.mapstruct.*;
import vn.com.viettel.dto.CatManufacturerDto;
import vn.com.viettel.entities.CatManufacturer;

@Mapper(componentModel = "spring")
public interface CatManufacturerMapper {

    CatManufacturerDto toDto(CatManufacturer entity);

    @Mapping(target = "isDeleted", ignore = true)
    CatManufacturer toEntity(CatManufacturerDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntityFromDto(CatManufacturerDto dto, @MappingTarget CatManufacturer entity);
}
