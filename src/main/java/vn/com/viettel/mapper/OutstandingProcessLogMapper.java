package vn.com.viettel.mapper;

import org.mapstruct.*;
import vn.com.viettel.dto.OutstandingProcessLogDto;
import vn.com.viettel.dto.UserDto;
import vn.com.viettel.entities.OutstandingProcessLog;

import java.util.List;


/**
 * MapStruct mapper for OUTSTANDING_PROCESS_LOG.
 */
@Mapper(componentModel = "spring")
@DecoratedWith(OutstandingProcessLogMapperDecorator.class)
public interface OutstandingProcessLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "outstandingId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    OutstandingProcessLog toEntity(OutstandingProcessLogDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "outstandingId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget OutstandingProcessLog entity, OutstandingProcessLogDto dto);

    @Mapping(target = "processId", source = "id")
    @Mapping(target = "outstandingId", source = "outstandingId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    OutstandingProcessLogDto toDto(OutstandingProcessLog entity);

    List<OutstandingProcessLogDto> toDtoList(List<OutstandingProcessLog> entityList);
}
