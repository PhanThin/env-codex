package vn.com.viettel.services;

import vn.com.viettel.dto.ProjectDto;

import java.util.List;

/**
 * Service interface for PROJECT CRUD operations.
 */
public interface ProjectService {

    ProjectDto create(ProjectDto request);

    ProjectDto update(Long id, ProjectDto request);

    ProjectDto getById(Long id);

    List<ProjectDto> getAll();

    void delete(Long id);
}
