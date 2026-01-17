package vn.com.viettel.services.impl;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.com.viettel.dto.ProjectTypeDto;
import vn.com.viettel.dto.ProjectTypeSearchRequestDto;
import vn.com.viettel.entities.ProjectType;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.ProjectTypeMapper;
import vn.com.viettel.repositories.jpa.ProjectTypeRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.ProjectTypeService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static vn.com.viettel.repositories.jpa.ProjectTypeSpecifications.buildSpecification;


@Service
public class ProjectTypeServiceImpl implements ProjectTypeService {

    @Autowired
    private ProjectTypeRepository projectTypeRepository;

    @Autowired
    private ProjectTypeMapper projectTypeMapper;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private Translator translator;

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "projectTypeName", "projectTypeName",
            "isActive", "isActive"
    );

    @Override
    @Transactional
    public ProjectTypeDto create(ProjectTypeDto dto) throws CustomException {
        validatePayload(dto);

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        String nameNorm = normalizeName(dto.getProjectTypeName());
        if (projectTypeRepository.existsDuplicateName(nameNorm)) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("projectType.duplicate.name", dto.getProjectTypeName()));
        }

        ProjectType entity = projectTypeMapper.toEntity(dto);

        // Audit fields - backend set
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        entity.setIsDeleted(Boolean.FALSE);
        entity.setIsActive(Boolean.TRUE.equals(dto.getIsActive()));

        // Ensure client cannot override
        entity.setProjectTypeName(dto.getProjectTypeName() != null ? dto.getProjectTypeName().trim() : null);

        ProjectType saved = projectTypeRepository.save(entity);
        return projectTypeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProjectTypeDto update(Long id, ProjectTypeDto dto) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("projectType.id.null"));
        }
        validatePayload(dto);

        ProjectType entity = projectTypeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("projectType.notFound", id)));

        String nameNorm = normalizeName(dto.getProjectTypeName());
        if (projectTypeRepository.existsDuplicateNameExcludeId(nameNorm, id)) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("projectType.duplicate.name", dto.getProjectTypeName()));
        }

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // Update fields from dto (ignore audit in mapper)
        projectTypeMapper.updateEntityFromDto(dto, entity);

        entity.setProjectTypeName(dto.getProjectTypeName() != null ? dto.getProjectTypeName().trim() : null);
        entity.setIsActive(Boolean.FALSE.equals(dto.getIsActive()));

        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(now);

        ProjectType saved = projectTypeRepository.save(entity);
        return projectTypeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(List<Long> projectTypeIds) throws CustomException {
        if (projectTypeIds == null || projectTypeIds.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("projectType.id.list.null"));
        }

        List<ProjectType> entities = projectTypeRepository.findAllByIdInAndIsDeletedFalse(projectTypeIds);
        if (entities == null || entities.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), msg("projectType.notFound", projectTypeIds));
        }
        if (entities.size() != projectTypeIds.size()) {
            List<Long> notFoundIds = projectTypeIds.stream()
                    .filter(id -> entities.stream().noneMatch(e -> Objects.equals(e.getId(), id)))
                    .toList();
            throw new CustomException(HttpStatus.NOT_FOUND.value(), msg("projectType.notFound", notFoundIds));
        }

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        entities.forEach(e -> {
            e.setIsDeleted(true);
            e.setUpdatedBy(userId);
            e.setUpdatedAt(now);
        });

        projectTypeRepository.saveAll(entities);
    }

    @Override
    public ProjectTypeDto getDetail(Long id) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("projectType.id.null"));
        }
        ProjectType entity = projectTypeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("projectType.notFound", id)));
        return projectTypeMapper.toDto(entity);
    }

    @Override
    public Page<ProjectTypeDto> search(ProjectTypeSearchRequestDto request) throws CustomException {
        if (request == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("projectType.search.payload.null"));
        }

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        // --- Validate & normalize sortBy ---
        String sortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = ALLOWED_SORT_FIELDS.get(sortBy);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    msg("projectType.search.sortBy.invalid", ALLOWED_SORT_FIELDS.keySet())
            );
        }

        // --- Validate & normalize sortDirection ---
        String sortDirectionRaw = StringUtils.defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("projectType.search.sortDirection.invalid"));
        }

        Sort sort = Sort.by(direction, sortProperty);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        var specification = buildSpecification(request);

        Page<ProjectType> resultPage = projectTypeRepository.findAll(specification, pageRequest);

        List<ProjectTypeDto> dtoList = resultPage.getContent().stream()
                .map(projectTypeMapper::toDto)
                .toList();

        return new PageImpl<>(dtoList, pageRequest, resultPage.getTotalElements());
    }

    private void validatePayload(ProjectTypeDto dto) throws CustomException {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("projectType.payload.null"));
        }
        if (StringUtils.isBlank(dto.getProjectTypeName())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("projectType.name.required"));
        }
        if (dto.getProjectTypeName().trim().length() > 250) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("projectType.name.length"));
        }

    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    /**
     * Lấy current user theo đúng cách RecommendationServiceImpl.
     */
    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof SysUser user) {
                return user;
            }
            // dự phòng nếu principal là String username
            return userRepository.findByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }

    private Long getCurrentUserId() {
        SysUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
    }

    private String msg(String key, Object... params) {
        return translator.getMessage(key, params);
    }

    @Override
    public List<ProjectTypeDto> getAllProjectType() {
        List<ProjectType> projectTypes = projectTypeRepository.findAllByIsDeletedIsFalse();
        return projectTypes.stream()
                .map(projectTypeMapper::toDto)
                .toList();
    }
}
