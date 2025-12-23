package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.com.viettel.dto.OutstandingProcessLogDto;
import vn.com.viettel.entities.OutstandingProcessLog;


/**
 * MapStruct mapper for OUTSTANDING_PROCESS_LOG.
 */
@Mapper(componentModel = "spring")
public interface OutstandingProcessLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "outstandingId", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    OutstandingProcessLog toEntity(OutstandingProcessLogDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "outstandingId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget OutstandingProcessLog entity, OutstandingProcessLogDto dto);

    OutstandingProcessLogDto toDto(OutstandingProcessLog entity);
}
