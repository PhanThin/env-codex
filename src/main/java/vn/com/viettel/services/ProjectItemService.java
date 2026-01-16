package vn.com.viettel.services;

import vn.com.viettel.dto.ProjectItemDto;

import java.util.List;


/**
 * Service interface for PROJECT_ITEM CRUD operations.
 * All operations are scoped by projectId.
 */
public interface ProjectItemService {

    ProjectItemDto create(Long projectId, ProjectItemDto request);

    ProjectItemDto update(Long projectId, Long itemId, ProjectItemDto request);

    ProjectItemDto getById(Long projectId, Long itemId);

    List<ProjectItemDto> getAll(Long projectId);

    List<ProjectItemDto> getAllByPhaseId(Long phaseId);

    void delete(Long projectId, Long itemId);
}
