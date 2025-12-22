package vn.com.viettel.services;

import vn.com.viettel.dto.CatOutstandingTypeDto;

import java.util.List;


/**
 * Service interface for CAT_OUTSTANDING_TYPE CRUD operations.
 */
public interface CatOutstandingTypeService {

    CatOutstandingTypeDto create(CatOutstandingTypeDto request);

    CatOutstandingTypeDto update(Long id, CatOutstandingTypeDto request);

    CatOutstandingTypeDto getById(Long id);

    List<CatOutstandingTypeDto> getAll();

    void delete(Long id);
}
