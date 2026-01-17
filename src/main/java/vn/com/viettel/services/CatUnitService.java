package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.CatUnitDto;
import vn.com.viettel.dto.CatUnitSearchRequestDto;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;

public interface CatUnitService {

    CatUnitDto create(CatUnitDto dto) throws CustomException;

    CatUnitDto update(Long id, CatUnitDto dto) throws CustomException;

    void delete(List<Long> unitIds) throws CustomException;

    CatUnitDto getDetail(Long id) throws CustomException;

    Page<CatUnitDto> search(CatUnitSearchRequestDto request) throws CustomException;

    List<CatUnitDto> findAllUnitType(String unitType) throws CustomException;
}
