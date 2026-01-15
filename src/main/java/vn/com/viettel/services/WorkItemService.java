package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.dto.WorkItemSearchRequest;

import java.util.List;


public interface WorkItemService {

    @Transactional(readOnly = true)
    Page<WorkItemDto> search(WorkItemSearchRequest request);

    WorkItemDto create(WorkItemDto dto);

    WorkItemDto update(Long workItemId, WorkItemDto dto);

    WorkItemDto getById(Long id);

    List<WorkItemDto> getAllByItemId(Long itemId);

    void delete(List<Long> ids);
}
