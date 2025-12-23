package vn.com.viettel.mapper;

import vn.com.viettel.dto.SysUserDto;
import vn.com.viettel.entities.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SysUserMapper {

    SysUserDto toDto(SysUser entity);

    /**
     * Create mapping. Service sẽ set isDeleted/isActive/audit theo nghiệp vụ.
     */
    @Mapping(target = "id", ignore = true)
    SysUser toEntity(SysUserDto dto);
}
