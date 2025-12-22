package vn.com.viettel.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.dto.ProjectDto;
import vn.com.viettel.entities.Project;
import vn.com.viettel.mapper.ProjectMapper;
import vn.com.viettel.repositories.jpa.ProjectRepository;
import vn.com.viettel.services.ProjectService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for PROJECT CRUD operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectDto create(ProjectDto request) {
        Project entity = projectMapper.toEntity(request);
        entity.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(entity);
        return projectMapper.toResponse(saved);
    }

    @Override
    public ProjectDto update(Long id, ProjectDto request) {
        Project entity = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        projectMapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(entity);
        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto getById(Long id) {
        Project entity = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return projectMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDto> getAll() {
        return projectRepository.findAllByIsDeletedFalse()
                .stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}
