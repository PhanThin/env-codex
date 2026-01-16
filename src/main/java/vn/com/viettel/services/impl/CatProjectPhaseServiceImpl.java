package vn.com.viettel.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.entities.CatProjectPhase;
import vn.com.viettel.entities.Project;
import vn.com.viettel.mapper.CatProjectPhaseMapper;
import vn.com.viettel.repositories.jpa.CatProjectPhaseRepository;
import vn.com.viettel.repositories.jpa.ProjectRepository;
import vn.com.viettel.services.CatProjectPhaseService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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


        CatProjectPhase entity = mapper.toEntity(request, projectId);
        entity.setCreatedAt(LocalDateTime.now());

        CatProjectPhase saved = repository.save(entity);

        // load project để map DTO
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("project.notfound", projectId)
                ));

        return mapper.toDto(saved, Map.of(projectId, project));
    }

    @Override
    public CatProjectPhaseDto update(Long projectId, Long phaseId, CatProjectPhaseDto request) {
        validateProject(projectId);
        validateRequest(request);

        CatProjectPhase entity = getOrThrow(projectId, phaseId);
        mapper.updateEntity(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        CatProjectPhase saved = repository.save(entity);

        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("project.notfound", projectId)
                ));

        return mapper.toDto(saved, Map.of(projectId, project));
    }

    @Override
    @Transactional(readOnly = true)
    public CatProjectPhaseDto getById(Long projectId, Long phaseId) {
        validateProject(projectId);
        CatProjectPhase entity = getOrThrow(projectId, phaseId);

        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("project.notfound", projectId)
                ));

        return mapper.toDto(entity, Map.of(projectId, project));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatProjectPhaseDto> getAll(Long projectId) {
        validateProject(projectId);
        List<CatProjectPhase> phases =
                repository.findAllByProjectIdAndIsDeletedFalseOrderByDisplayOrderAsc(projectId);
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("project.notfound", projectId)
                ));

        Map<Long, Project> projectMap = Map.of(projectId, project);

        return phases.stream()
                .map(phase -> mapper.toDto(phase, projectMap))
                .toList();
    }

    @Override
    public List<CatProjectPhaseDto> getAllByProjectType(Long projectTypeId) {
        List<CatProjectPhase> phases =
                repository.findAllByProjectTypeId(projectTypeId);

        return phases.stream()
                .map(phase -> mapper.toDto(phase, null))
                .toList();
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

        if (request == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("project.phase.payload.null")
            );
        }

        if (!StringUtils.hasText(request.getPhaseCode())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("project.phase.phaseCode.required")
            );
        }

        if (!StringUtils.hasText(request.getPhaseName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("project.phase.phaseName.required")
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
