package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.entities.WorkItem;
import vn.com.viettel.mapper.WorkItemMapper;
import vn.com.viettel.repositories.jpa.ProjectItemRepository;
import vn.com.viettel.repositories.jpa.WorkItemRepository;
import vn.com.viettel.services.WorkItemService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;


@Service
@RequiredArgsConstructor
@Transactional
public class WorkItemServiceImpl implements WorkItemService {

    private final WorkItemRepository repository;
    private final ProjectItemRepository projectItemRepository;
    private final WorkItemMapper mapper;
    private final Translator translator;
    @Override
    public WorkItemDto create(Long itemId, WorkItemDto request) {
        validateItem(itemId);
        validateRequest(request);

        if (repository.existsByItemIdAndWorkItemNameAndIsDeletedFalse(itemId, request.getWorkItemName())) {
            throw new CustomException(
                HttpStatus.CONFLICT.value(),
                translator.getMessage("workitem.duplicate", request.getWorkItemName())
            );
        }

        WorkItem entity = mapper.toEntity(request);
        entity.setItemId(itemId);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setCreatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(entity));
    }

    @Override
    public WorkItemDto update(Long itemId, Long workItemId, WorkItemDto request) {
        validateItem(itemId);
        validateRequest(request);

        WorkItem entity = getOrThrow(itemId, workItemId);
        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkItemDto getById(Long itemId, Long workItemId) {
        validateItem(itemId);
        return mapper.toDto(getOrThrow(itemId, workItemId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkItemDto> getAll(Long itemId) {
        validateItem(itemId);
        return repository.findAllByItemIdAndIsDeletedFalse(itemId)
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long itemId, Long workItemId) {
        validateItem(itemId);
        WorkItem entity = getOrThrow(itemId, workItemId);
        entity.setIsDeleted(Boolean.TRUE);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateItem(Long itemId) {
        if (!projectItemRepository.existsByIdAndIsDeletedFalse(itemId)) {
            throw new CustomException(
                HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("project.item.notfound", itemId)
            );
        }
    }

    private void validateRequest(WorkItemDto request) {
        if (request == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("workitem.request.null")
            );
        }

        if (!StringUtils.hasText(request.getWorkItemName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("workitem.name.required")
            );
        }
    }


    private WorkItem getOrThrow(Long itemId, Long workItemId) {
        WorkItem entity = repository.findByIdAndIsDeletedFalse(workItemId)
                .orElseThrow(() -> new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("workitem.notfound", workItemId)
                ));

        if (!itemId.equals(entity.getItemId())) {
            throw new CustomException(
                HttpStatus.NOT_FOUND.value(),
                translator.getMessage("workitem.notfound", workItemId)
            );
        }
        return entity;
    }
}
