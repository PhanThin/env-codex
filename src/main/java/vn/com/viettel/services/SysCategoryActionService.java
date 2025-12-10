package vn.com.viettel.services;


import vn.com.viettel.dto.SysCategoryActionDTO;

import java.util.List;

public interface SysCategoryActionService {
  List<SysCategoryActionDTO> getSysCategoryActionForExport(SysCategoryActionDTO dto);
}
