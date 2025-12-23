package vn.com.viettel.services;

import vn.com.viettel.dto.WorkItemDto;

import java.util.List;


public interface WorkItemService {

    WorkItemDto create(Long itemId, WorkItemDto request);

    WorkItemDto update(Long itemId, Long workItemId, WorkItemDto request);

    WorkItemDto getById(Long itemId, Long workItemId);

    List<WorkItemDto> getAll(Long itemId);

    void delete(Long itemId, Long workItemId);
}
