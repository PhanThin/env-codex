package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.CatOutstandingTypeDto;
import vn.com.viettel.dto.OutstandingTypeSearchRequestDto;

import java.util.List;


/**
 * Service interface for CAT_OUTSTANDING_TYPE CRUD operations.
 */
public interface CatOutstandingTypeService {


    Page<CatOutstandingTypeDto> search(OutstandingTypeSearchRequestDto request);

    CatOutstandingTypeDto create(CatOutstandingTypeDto request);

    CatOutstandingTypeDto update(Long id, CatOutstandingTypeDto request);

    CatOutstandingTypeDto getById(Long id);

    List<CatOutstandingTypeDto> getAll();

    void delete(List<Long> ids);         // bulk delete
}
