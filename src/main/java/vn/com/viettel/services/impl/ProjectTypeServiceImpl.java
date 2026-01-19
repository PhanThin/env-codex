package vn.com.viettel.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import vn.com.viettel.dto.OrgDto;
import vn.com.viettel.dto.ProjectTypeDto;
import vn.com.viettel.dto.ProjectTypeSearchRequestDto;
import vn.com.viettel.dto.UserDto;
import vn.com.viettel.entities.ProjectType;
import vn.com.viettel.entities.SysOrg;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.ProjectTypeMapper;
import vn.com.viettel.repositories.jpa.ProjectTypeRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.ProjectTypeService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.viettel.repositories.jpa.ProjectTypeSpecifications.buildSpecification;


@Service
@RequiredArgsConstructor
public class ProjectTypeServiceImpl implements ProjectTypeService {

    @Autowired
    private ProjectTypeRepository projectTypeRepository;

    @Autowired
    private ProjectTypeMapper projectTypeMapper;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private Translator translator;

    private final org.modelmapper.ModelMapper modelMapper;
    private final vn.com.viettel.repositories.jpa.SysOrgRepository sysOrgRepo;

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
        ProjectTypeDto dto2 = projectTypeMapper.toDto(saved);
        enrichCreatedUpdatedUsers(List.of(saved), List.of(dto2));
        return dto2;

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
        ProjectTypeDto dto2 = projectTypeMapper.toDto(saved);
        enrichCreatedUpdatedUsers(List.of(saved), List.of(dto2));
        return dto2;
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
        ProjectTypeDto dto2 = projectTypeMapper.toDto(entity);
        enrichCreatedUpdatedUsers(List.of(entity), List.of(dto2));
        return dto2;
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
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    msg("projectType.search.sortDirection.invalid")
            );
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        var specification = buildSpecification(request);

        // 1) Query Page<Entity>
        Page<ProjectType> resultPage = projectTypeRepository.findAll(specification, pageRequest);

        // 2) Convert -> DTO list
        List<ProjectType> entities = resultPage.getContent();
        List<ProjectTypeDto> dtoList = entities.stream()
                .map(projectTypeMapper::toDto)
                .toList();

        // 3) Enrich (createdBy/updatedBy... hoặc field khác)
        enrichCreatedUpdatedUsers(entities, dtoList);

        // 4) Wrap lại Page<DTO>
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
        List<ProjectType> entities = projectTypeRepository.findAllByIsDeletedIsFalse();
        List<ProjectTypeDto> dtos = entities.stream().map(projectTypeMapper::toDto).toList();
        enrichCreatedUpdatedUsers(entities, dtos);
        return dtos;

    }
    private UserDto mapUserDto(SysUser sysUser, Map<Long, SysOrg> sysOrgMap) {
        if (sysUser == null) return null;
        UserDto userDto = modelMapper.map(sysUser, UserDto.class);

        // giống RecommendationMapper: gắn org vào user
        if (sysOrgMap != null && sysUser.getOrgId() != null && sysOrgMap.containsKey(sysUser.getOrgId())) {
            userDto.setOrg(modelMapper.map(sysOrgMap.get(sysUser.getOrgId()), OrgDto.class));
        }
        return userDto;
    }

    private void enrichCreatedUpdatedUsers(List<ProjectType> entities, List<ProjectTypeDto> dtos) {
        if (entities == null || entities.isEmpty() || dtos == null || dtos.isEmpty()) return;

        // lấy danh sách userId cần dùng (tránh N+1)
        Set<Long> userIds = new HashSet<>();
        for (ProjectType e : entities) {
            if (e.getCreatedBy() != null) userIds.add(e.getCreatedBy());
            if (e.getUpdatedBy() != null) userIds.add(e.getUpdatedBy());
        }

        // load user map
        Map<Long, SysUser> userMap;
        if (userIds.isEmpty()) {
            userMap = Collections.emptyMap();
        } else {
            // Nếu repo có findAllByIdInAndIsDeletedFalse thì dùng cái đó là tốt nhất
            // userMap = userRepository.findAllByIdInAndIsDeletedFalse(new ArrayList<>(userIds))...
            userMap = userRepository.findAllById(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        // load org map (nếu muốn giống Recommendation)
        Map<Long, SysOrg> orgMap = sysOrgRepo.findAllByIsDeletedFalse().stream()
                .collect(Collectors.toMap(SysOrg::getId, o -> o));

        // enrich theo đúng index (dtos tạo từ entities theo order)
        for (int i = 0; i < entities.size(); i++) {
            ProjectType e = entities.get(i);
            ProjectTypeDto dto = dtos.get(i);

            if (e.getCreatedBy() != null && userMap.containsKey(e.getCreatedBy())) {
                dto.setCreatedByUser(mapUserDto(userMap.get(e.getCreatedBy()), orgMap));
            }
            if (e.getUpdatedBy() != null && userMap.containsKey(e.getUpdatedBy())) {
                dto.setUpdatedByUser(mapUserDto(userMap.get(e.getUpdatedBy()), orgMap));
            }
        }
    }

}
