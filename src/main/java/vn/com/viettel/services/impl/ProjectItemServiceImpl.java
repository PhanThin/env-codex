package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.ProjectItemDto;
import vn.com.viettel.entities.ProjectItem;
import vn.com.viettel.mapper.ProjectItemMapper;
import vn.com.viettel.repositories.jpa.ProjectItemRepository;
import vn.com.viettel.repositories.jpa.ProjectRepository;
import vn.com.viettel.services.ProjectItemService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

/**
 * Service implementation for PROJECT_ITEM CRUD operations.
 * CRUD only, no business logic beyond required validations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectItemServiceImpl implements ProjectItemService {

    private final ProjectItemRepository projectItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectItemMapper mapper;
    private final Translator translator;
    @Override
    public ProjectItemDto create(Long projectId, ProjectItemDto request) {
        validateProjectExists(projectId);
        validateRequest(request);

        if (projectItemRepository.existsByProjectIdAndItemCodeAndIsDeletedFalse(projectId, request.getItemCode())) {
            throw new CustomException(
                    HttpStatus.CONFLICT.value(),
                    translator.getMessage("project.item.duplicate", request.getItemCode())
            );
        }

        ProjectItem entity = mapper.toEntity(request);
        entity.setProjectId(projectId);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setCreatedAt(LocalDateTime.now());

        ProjectItem saved = projectItemRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public ProjectItemDto update(Long projectId, Long itemId, ProjectItemDto request) {
        validateProjectExists(projectId);
        validateRequest(request);

        ProjectItem entity = getItemOrThrow(projectId, itemId);

        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        ProjectItem saved = projectItemRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectItemDto getById(Long projectId, Long itemId) {
        validateProjectExists(projectId);
        ProjectItem entity = getItemOrThrow(projectId, itemId);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectItemDto> getAll(Long projectId) {
        validateProjectExists(projectId);
        return projectItemRepository.findAllByProjectIdAndIsDeletedFalse(projectId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long projectId, Long itemId) {
        validateProjectExists(projectId);

        ProjectItem entity = getItemOrThrow(projectId, itemId);
        entity.setIsDeleted(Boolean.TRUE);
        entity.setUpdatedAt(LocalDateTime.now());

        projectItemRepository.save(entity);
    }

    private void validateProjectExists(Long projectId) {
        if (projectId == null || !projectRepository.existsByIdAndIsDeletedFalse(projectId)) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("project.notfound", projectId)
            );
        }
    }

    private void validateRequest(ProjectItemDto request) {

        if (request == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("project.item.payload.null")
            );
        }

        if (!StringUtils.hasText(request.getItemCode())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("project.item.itemCode.required")
            );
        }

        if (!StringUtils.hasText(request.getItemName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("project.item.itemName.required")
            );
        }
    }


    private ProjectItem getItemOrThrow(Long projectId, Long itemId) {
        ProjectItem entity = projectItemRepository.findByIdAndIsDeletedFalse(itemId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("project.item.notfound", itemId)
                ));

        if (!projectId.equals(entity.getProjectId())) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("project.item.notfound", itemId)
            );
        }

        return entity;
    }
}
