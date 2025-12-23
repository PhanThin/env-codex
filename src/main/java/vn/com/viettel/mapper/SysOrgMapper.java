package vn.com.viettel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.viettel.dto.SysOrgDto;
import vn.com.viettel.entities.SysOrg;

@Mapper(componentModel = "spring")
public interface SysOrgMapper {

    SysOrgDto toDto(SysOrg entity);

    @Mapping(target = "id", ignore = true)
    SysOrg toEntity(SysOrgDto dto);
}
