package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.CatManufacturerDto;
import vn.com.viettel.dto.CatManufacturerSearchRequestDto;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;

public interface CatManufacturerService {

    List<CatManufacturerDto> getAll(String sortBy, String sortDir);

    CatManufacturerDto create(CatManufacturerDto dto) throws CustomException;

    CatManufacturerDto update(Long id, CatManufacturerDto dto) throws CustomException;

    void delete(List<Long> manufacturerIds) throws CustomException;

    CatManufacturerDto getDetail(Long id) throws CustomException;

    Page<CatManufacturerDto> search(CatManufacturerSearchRequestDto request) throws CustomException;
}
