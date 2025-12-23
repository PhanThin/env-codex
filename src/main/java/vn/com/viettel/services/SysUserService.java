package vn.com.viettel.services;



import vn.com.viettel.dto.SysUserDto;

import java.util.List;

public interface SysUserService {

    SysUserDto create(SysUserDto dto);

    SysUserDto update(Long userId, SysUserDto dto);

    SysUserDto getById(Long userId);

    List<SysUserDto> getAll();

    void delete(Long userId);
}
