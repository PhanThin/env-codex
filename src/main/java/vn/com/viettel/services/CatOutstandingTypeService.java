package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.OutstandingTypeDto;
import vn.com.viettel.dto.OutstandingTypeSearchRequestDto;

import java.util.List;


/**
 * Service interface for CAT_OUTSTANDING_TYPE CRUD operations.
 */
public interface CatOutstandingTypeService {

    Page<OutstandingTypeDto> search(OutstandingTypeSearchRequestDto request);

    OutstandingTypeDto create(OutstandingTypeDto request);

    OutstandingTypeDto update(Long id, OutstandingTypeDto request);

    OutstandingTypeDto getById(Long id);

    List<OutstandingTypeDto> getAll();

    void delete(Long id);
}
