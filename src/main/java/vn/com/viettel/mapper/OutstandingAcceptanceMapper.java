
package vn.com.viettel.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.com.viettel.dto.OutstandingAcceptanceDto;
import vn.com.viettel.entities.OutstandingAcceptance;


/**
 * MapStruct mapper for OUTSTANDING_ACCEPTANCE.
 */
@Mapper(componentModel = "spring")
public interface OutstandingAcceptanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "outstandingId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OutstandingAcceptance toEntity(OutstandingAcceptanceDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "outstandingId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget OutstandingAcceptance entity, OutstandingAcceptanceDto dto);

    OutstandingAcceptanceDto toDto(OutstandingAcceptance entity);
}
