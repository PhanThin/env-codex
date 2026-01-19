package vn.com.viettel.services.impl;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.viettel.dto.CatOutstandingTypeDto;
import vn.com.viettel.dto.OrgDto;
import vn.com.viettel.dto.OutstandingTypeSearchRequestDto;
import vn.com.viettel.dto.UserDto;
import vn.com.viettel.entities.CatOutstandingType;
import vn.com.viettel.entities.SysOrg;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.CatOutstandingTypeMapper;
import vn.com.viettel.repositories.jpa.CatOutstandingTypeRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.CatOutstandingTypeService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Service implementation for CAT_OUTSTANDING_TYPE CRUD operations.
 * CRUD only, no business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CatOutstandingTypeServiceImpl implements CatOutstandingTypeService {

    private final CatOutstandingTypeRepository repository;
    private final CatOutstandingTypeMapper mapper;
    private final Translator translator;
    private final SysUserRepository userRepository;
    private final org.modelmapper.ModelMapper modelMapper;
    private final vn.com.viettel.repositories.jpa.SysOrgRepository sysOrgRepo;


    // -------------------- Current user (same style as RecommendationServiceImpl) --------------------

    /**
     * Try to resolve current SysUser from Spring Security context.
     * - If principal is SysUser -> return.
     * - Else if principal is username (String) -> load from DB.
     */
    private SysUser getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SysUser user) {
            return user;
        }

        // Fallback: principal may be username
        String username = authentication.getName();
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }

    private Long getCurrentUserId() {
        SysUser u = getCurrentUserOrNull();
        if (u == null || u.getId() == null) {
//            throw new CustomException(
//                    HttpStatus.UNAUTHORIZED.value(),
//                    translator.getMessage("auth.unauthorized")
//            );
            return Constants.DEFAULT_USER_ID;
        }
        return u.getId();
    }

    // -------------------- CRUD --------------------

    @Override
    public CatOutstandingTypeDto create(CatOutstandingTypeDto request) {
        validateRequest(request);
        validateAuditFieldsNotAllowed(request);
        validateNotNullConstraintsOnCreate(request);
        validateUniqueTypeName(null, request.getTypeName());

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        CatOutstandingType entity = mapper.toEntity(request);

        // Backend owns these fields
        entity.setIsDeleted(Boolean.FALSE);
        if (entity.getIsActive() == null) {
            entity.setIsActive(Boolean.TRUE);
        }

        // (1) create: set created_by, updated_by = userId; created_at, updated_at = now
        entity.setCreatedAt(now);
        entity.setCreatedBy(userId);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(userId);

        CatOutstandingType saved = repository.save(entity);
        CatOutstandingTypeDto dto = mapper.toDto(saved);
        enrichCreatedUpdatedUsers(List.of(saved), List.of(dto));
        return dto;

    }

    @Override
    public CatOutstandingTypeDto update(Long id, CatOutstandingTypeDto request) {
        CatOutstandingType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catOutstandingType.notFound", id)
                ));

        validateRequest(request);
        validateAuditFieldsNotAllowed(request);
        validateNotNullConstraintsOnUpdate(request);
        validateUniqueTypeName(id, request.getTypeName());

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // Prevent mapper overwriting create audit
        LocalDateTime createdAt = entity.getCreatedAt();
        Long createdBy = entity.getCreatedBy();

        mapper.updateEntity(entity, request);

        entity.setCreatedAt(createdAt);
        entity.setCreatedBy(createdBy);

        // (2) update: set updated_by = userId; updated_at = now
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(userId);

        CatOutstandingType saved = repository.save(entity);
        CatOutstandingTypeDto dto = mapper.toDto(saved);
        enrichCreatedUpdatedUsers(List.of(saved), List.of(dto));
        return dto;

    }

    @Override
    @Transactional(readOnly = true)
    public CatOutstandingTypeDto getById(Long id) {
        CatOutstandingType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catOutstandingType.notFound", id)
                ));
        CatOutstandingTypeDto dto = mapper.toDto(entity);
        enrichCreatedUpdatedUsers(List.of(entity), List.of(dto));
        return dto;

    }

    @Override
    @Transactional(readOnly = true)
    public List<CatOutstandingTypeDto> getAll() {
        List<CatOutstandingType> entities = repository.findAllByIsDeletedFalse();
        List<CatOutstandingTypeDto> dtos = entities.stream().map(mapper::toDto).toList();
        enrichCreatedUpdatedUsers(entities, dtos);
        return dtos;

    }

    /**
     * Soft delete multiple items.
     */
    @Override
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.delete.ids.required")
            );
        }

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        List<CatOutstandingType> entities = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> repository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new CustomException(
                                HttpStatus.NOT_FOUND.value(),
                                translator.getMessage("catOutstandingType.notFound", id)
                        )))
                .collect(Collectors.toList());

        // (3) delete: set updated_by = userId; updated_at = now
        // (4) delete multiple: done (ids list)
        for (CatOutstandingType e : entities) {
            e.setIsDeleted(Boolean.TRUE);
            e.setUpdatedAt(now);
            e.setUpdatedBy(userId);
        }
        repository.saveAll(entities);
    }

    // -------------------- Validation --------------------

    private void validateRequest(CatOutstandingTypeDto request) {
        if (request == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.payload.null")
            );
        }
        if (!StringUtils.hasText(request.getTypeName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.typeName.required")
            );
        }
    }

    private void validateNotNullConstraintsOnCreate(CatOutstandingTypeDto request) {
        if (hasProperty(request, "isActive")) {
            Object val = getProperty(request, "isActive");
            if (val == null) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catOutstandingType.isActive.required")
                );
            }
        }

        if (hasProperty(request, "isDeleted")) {
            Object val = getProperty(request, "isDeleted");
            if (val != null) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catOutstandingType.isDeleted.notAllowed")
                );
            }
        }
    }

    private void validateNotNullConstraintsOnUpdate(CatOutstandingTypeDto request) {
        if (hasProperty(request, "isActive")) {
            Object val = getProperty(request, "isActive");
            if (val == null) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catOutstandingType.isActive.required")
                );
            }
        }

        if (hasProperty(request, "isDeleted")) {
            Object val = getProperty(request, "isDeleted");
            if (val != null) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catOutstandingType.isDeleted.notAllowed")
                );
            }
        }
    }

    private void validateUniqueTypeName(Long currentId, String typeName) {
        String normalized = normalizeTypeName(typeName);

        List<CatOutstandingType> existing = repository.findAllByIsDeletedFalse();
        boolean duplicated = existing.stream()
                .filter(e -> e.getTypeName() != null)
                .anyMatch(e -> {
                    if (currentId != null && Objects.equals(e.getId(), currentId)) {
                        return false;
                    }
                    return normalizeTypeName(e.getTypeName()).equals(normalized);
                });

        if (duplicated) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.typeName.duplicate", typeName)
            );
        }
    }

    private String normalizeTypeName(String typeName) {
        return StringUtils.trimWhitespace(typeName).toUpperCase(Locale.ROOT);
    }

    private void validateAuditFieldsNotAllowed(Object request) {
        rejectIfProvided(request, "createdAt", "catOutstandingType.audit.createdAt.notAllowed");
        rejectIfProvided(request, "createdBy", "catOutstandingType.audit.createdBy.notAllowed");
        rejectIfProvided(request, "updatedAt", "catOutstandingType.audit.updatedAt.notAllowed");
        rejectIfProvided(request, "updatedBy", "catOutstandingType.audit.updatedBy.notAllowed");

        // fallback naming
        rejectIfProvided(request, "createAt", "catOutstandingType.audit.createdAt.notAllowed");
        rejectIfProvided(request, "createBy", "catOutstandingType.audit.createdBy.notAllowed");
        rejectIfProvided(request, "updateAt", "catOutstandingType.audit.updatedAt.notAllowed");
        rejectIfProvided(request, "updateBy", "catOutstandingType.audit.updatedBy.notAllowed");
    }

    private void rejectIfProvided(Object bean, String property, String messageKey) {
        if (!hasProperty(bean, property)) {
            return;
        }
        Object val = getProperty(bean, property);
        if (val != null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage(messageKey)
            );
        }
    }

    private boolean hasProperty(Object bean, String property) {
        try {
            BeanWrapper bw = new BeanWrapperImpl(bean);
            bw.getPropertyDescriptor(property);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Object getProperty(Object bean, String property) {
        BeanWrapper bw = new BeanWrapperImpl(bean);
        return bw.getPropertyValue(property);
    }

    // -------------------- Search --------------------

    private static final Map<String, String> OUTSTANDING_TYPE_ALLOWED_SORT_FIELDS;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("createdAt", "createdAt");
        m.put("updatedAt", "updatedAt");
        m.put("typeName", "typeName");
        m.put("isActive", "isActive");
        m.put("id", "id");
        OUTSTANDING_TYPE_ALLOWED_SORT_FIELDS = Collections.unmodifiableMap(m);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatOutstandingTypeDto> search(OutstandingTypeSearchRequestDto request) {
        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        String sortByRaw = defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = OUTSTANDING_TYPE_ALLOWED_SORT_FIELDS.get(sortByRaw);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.search.sortBy.invalid", OUTSTANDING_TYPE_ALLOWED_SORT_FIELDS.keySet())
            );
        }

        String sortDirectionRaw = defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.search.sortDirection.invalid")
            );
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate fromDate = request.getCreatedFrom();
        LocalDate toDate = request.getCreatedTo();

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.search.createdTime.range.invalid")
            );
        }


        Specification<CatOutstandingType> specification = buildOutstandingTypeSearchSpecification(request, fromDate, toDate);

        Sort sort = Sort.by(direction, sortProperty);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<CatOutstandingType> resultPage = repository.findAll(specification, pageRequest);

        List<CatOutstandingType> entities = resultPage.getContent();
        List<CatOutstandingTypeDto> dtoList = entities.stream()
                .map(mapper::toDto)
                .toList();

        enrichCreatedUpdatedUsers(entities, dtoList);

        return new PageImpl<>(dtoList, pageRequest, resultPage.getTotalElements());


    }

    private Specification<CatOutstandingType> buildOutstandingTypeSearchSpecification(
            OutstandingTypeSearchRequestDto request,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("isDeleted")));

            String kw = safeTrim(request.getKeyword());
            if (kw != null) {
                String like = "%" + kw.toUpperCase(Locale.ROOT) + "%";
                Expression<String> typeNameExp = cb.upper(cb.trim(root.get("typeName")));
                predicates.add(cb.like(typeNameExp, like));
            }

//            String status = defaultIfBlank(request.getStatus(), "ALL").toUpperCase(Locale.ROOT);
//            if ("ACTIVE".equals(status)) {
//                predicates.add(cb.isTrue(root.get("isActive")));
//            } else if ("INACTIVE".equals(status)) {
//                predicates.add(cb.isFalse(root.get("isActive")));
//            } else if (!"ALL".equals(status)) {
//                throw new CustomException(
//                        HttpStatus.BAD_REQUEST.value(),
//                        translator.getMessage("catOutstandingType.search.status.invalid")
//                );
//            }
            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }

            if (fromDate != null) {
                LocalDateTime from = fromDate.atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (toDate != null) {
                LocalDateTime toExclusive = toDate.atTime(23, 59, 59);
                predicates.add(cb.lessThan(root.get("createdAt"), toExclusive));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String safeTrim(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private String defaultIfBlank(String s, String defaultVal) {
        return StringUtils.hasText(s) ? s.trim() : defaultVal;
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

    private void enrichCreatedUpdatedUsers(List<CatOutstandingType> entities, List<CatOutstandingTypeDto> dtos) {
        if (entities == null || entities.isEmpty() || dtos == null || dtos.isEmpty()) return;

        // lấy danh sách userId cần dùng (tránh N+1)
        Set<Long> userIds = new HashSet<>();
        for (CatOutstandingType e : entities) {
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
            CatOutstandingType e = entities.get(i);
            CatOutstandingTypeDto dto = dtos.get(i);

            if (e.getCreatedBy() != null && userMap.containsKey(e.getCreatedBy())) {
                dto.setCreatedByUser(mapUserDto(userMap.get(e.getCreatedBy()), orgMap));
            }
            if (e.getUpdatedBy() != null && userMap.containsKey(e.getUpdatedBy())) {
                dto.setUpdatedByUser(mapUserDto(userMap.get(e.getUpdatedBy()), orgMap));
            }
        }
    }

}
