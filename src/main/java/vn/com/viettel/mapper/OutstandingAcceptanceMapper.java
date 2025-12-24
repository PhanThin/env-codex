
package vn.com.viettel.mapper;


import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.com.viettel.dto.OutstandingAcceptanceDto;
import vn.com.viettel.entities.OutstandingAcceptance;

import java.util.List;


/**
 * MapStruct mapper for OUTSTANDING_ACCEPTANCE.
 */
@Mapper(componentModel = "spring")
@DecoratedWith(OutstandingAcceptanceMapperDecorator.class)
public interface OutstandingAcceptanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "outstandingId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "acceptedAt", ignore = true)
    @Mapping(target = "acceptedBy", ignore = true)
    OutstandingAcceptance toEntity(OutstandingAcceptanceDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "outstandingId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "acceptedAt", ignore = true)
    @Mapping(target = "acceptedBy", ignore = true)
    void updateEntity(@MappingTarget OutstandingAcceptance entity, OutstandingAcceptanceDto dto);

    @Mapping(target = "acceptanceId", source = "id")
    @Mapping(target = "outstandingId", source = "outstandingId")
    @Mapping(target = "acceptedAt", source = "acceptedAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "acceptedByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "deletedAttachments", ignore = true)
    OutstandingAcceptanceDto toDto(OutstandingAcceptance entity);

    List<OutstandingAcceptanceDto> toDtoList(List<OutstandingAcceptance> entityList);
}
