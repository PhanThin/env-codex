package vn.com.viettel.services;


import vn.com.viettel.dto.SysOrgDto;

import java.util.List;

public interface SysOrgService {

    SysOrgDto create(SysOrgDto dto);

    SysOrgDto update(Long orgId, SysOrgDto dto);

    SysOrgDto getById(Long orgId);

    List<SysOrgDto> getAll();

    void delete(Long orgId);
}
