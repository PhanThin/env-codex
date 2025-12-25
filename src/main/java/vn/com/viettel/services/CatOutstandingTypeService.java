package vn.com.viettel.services;

import vn.com.viettel.dto.OutstandingTypeDto;

import java.util.List;


/**
 * Service interface for CAT_OUTSTANDING_TYPE CRUD operations.
 */
public interface CatOutstandingTypeService {

    OutstandingTypeDto create(OutstandingTypeDto request);

    OutstandingTypeDto update(Long id, OutstandingTypeDto request);

    OutstandingTypeDto getById(Long id);

    List<OutstandingTypeDto> getAll();

    void delete(Long id);
}
