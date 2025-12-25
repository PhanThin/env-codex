package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.entities.CatProjectPhase;
import vn.com.viettel.mapper.CatProjectPhaseMapper;
import vn.com.viettel.repositories.jpa.CatProjectPhaseRepository;
import vn.com.viettel.repositories.jpa.ProjectRepository;
import vn.com.viettel.services.CatProjectPhaseService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

@Service
@RequiredArgsConstructor
@Transactional
public class CatProjectPhaseServiceImpl implements CatProjectPhaseService {

    private final CatProjectPhaseRepository repository;
    private final ProjectRepository projectRepository;
    private final CatProjectPhaseMapper mapper;
    private final Translator translator;

    @Override
    public CatProjectPhaseDto create(Long projectId, CatProjectPhaseDto request) {
        validateProject(projectId);
        validateRequest(request);

        if (repository.existsByProjectIdAndPhaseCodeAndIsDeletedFalse(projectId, request.getPhaseCode())) {
            throw new CustomException(
                HttpStatus.CONFLICT.value(),
                translator.getMessage("project.phase.duplicate", request.getPhaseCode())
            );
        }

        CatProjectPhase entity = mapper.toEntity(request);
        entity.setProjectId(projectId);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setCreatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(entity));
    }

    @Override
    public CatProjectPhaseDto update(Long projectId, Long phaseId, CatProjectPhaseDto request) {
        validateProject(projectId);
        validateRequest(request);

        CatProjectPhase entity = getOrThrow(projectId, phaseId);
        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CatProjectPhaseDto getById(Long projectId, Long phaseId) {
        validateProject(projectId);
        return mapper.toDto(getOrThrow(projectId, phaseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatProjectPhaseDto> getAll(Long projectId) {
        validateProject(projectId);
        return repository.findAllByProjectIdAndIsDeletedFalseOrderByDisplayOrderAsc(projectId)
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long projectId, Long phaseId) {
        validateProject(projectId);
        CatProjectPhase entity = getOrThrow(projectId, phaseId);
        entity.setIsDeleted(Boolean.TRUE);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateProject(Long projectId) {
        if (!projectRepository.existsByIdAndIsDeletedFalse(projectId)) {
            throw new CustomException(
                HttpStatus.NOT_FOUND.value(),
                translator.getMessage("project.notfound", projectId)
            );
        }
    }

    private void validateRequest(CatProjectPhaseDto request) {
        if (request == null
                || !StringUtils.hasText(request.getPhaseCode())
                || !StringUtils.hasText(request.getPhaseName())) {
            throw new CustomException(
                HttpStatus.BAD_REQUEST.value(),
                translator.getMessage("invalid.request")
            );
        }
    }

    private CatProjectPhase getOrThrow(Long projectId, Long phaseId) {
        CatProjectPhase entity = repository.findByIdAndIsDeletedFalse(phaseId)
                .orElseThrow(() -> new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("project.phase.notfound", phaseId)
                ));

        if (!projectId.equals(entity.getProjectId())) {
            throw new CustomException(
                HttpStatus.NOT_FOUND.value(),
                translator.getMessage("project.phase.notfound", phaseId)
            );
        }
        return entity;
    }
}
