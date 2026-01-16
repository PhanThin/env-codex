package vn.com.viettel.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import vn.com.viettel.dto.CatRecommendationTypeDto;
import vn.com.viettel.dto.RecommendationTypeSearchRequestDto;
import vn.com.viettel.entities.CatRecommendationType;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.CatRecommendationTypeMapper;
import vn.com.viettel.repositories.jpa.CatRecommendationTypeRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.CatRecommendationTypeService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

/**
 * Service implementation for CAT_RECOMMENDATION_TYPE CRUD operations.
 * CRUD only, no business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CatRecommendationTypeServiceImpl implements CatRecommendationTypeService {

    private final CatRecommendationTypeRepository repository;
    private final CatRecommendationTypeMapper mapper;
    private final Translator translator;
    private final SysUserRepository userRepository;

    private static final Map<String, String> ALLOWED_SORT_FIELDS;
    static {
        Map<String, String> m = new HashMap<>();
        // FE sortBy -> entity field
        m.put("createdAt", "createdAt");
        m.put("updatedAt", "updatedAt");
        m.put("typeName", "typeName");
        m.put("isActive", "isActive");
        m.put("id", "id");
        ALLOWED_SORT_FIELDS = Collections.unmodifiableMap(m);
    }

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

        String username = authentication.getName();
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }

    private Long getCurrentUserId() {
        SysUser u = getCurrentUserOrNull();
        if (u == null || u.getId() == null) {
            throw new CustomException(
                    HttpStatus.UNAUTHORIZED.value(),
                    translator.getMessage("auth.unauthorized")
            );
        }
        return u.getId();
    }

    // -------------------- Search --------------------

    @Override
    @Transactional(readOnly = true)
    public Page<CatRecommendationTypeDto> search(RecommendationTypeSearchRequestDto request) {
        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        // --- sortBy whitelist ---
        String sortByRaw = defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = ALLOWED_SORT_FIELDS.get(sortByRaw);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.search.sortBy.invalid", ALLOWED_SORT_FIELDS.keySet())
            );
        }

        // --- sortDirection ---
        String sortDirectionRaw = defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.search.sortDirection.invalid")
            );
        }

        // --- Normalize date range (mặc định trong năm hiện tại + tối đa 1 năm) ---
        LocalDateTime now = LocalDateTime.now();
        LocalDate fromDate = request.getCreatedFrom();
        LocalDate toDate = request.getCreatedTo();

        // Nếu không truyền, default = từ đầu năm đến hiện tại
        if (fromDate == null && toDate == null) {
            fromDate = LocalDate.of(now.getYear(), 1, 1);
            toDate = now.toLocalDate();
        }

        // Nếu chỉ truyền 1 đầu, tự hoàn thiện đầu còn lại theo logic an toàn
        if (fromDate != null && toDate == null) {
            toDate = now.toLocalDate();
        }
        if (fromDate == null && toDate != null) {
            fromDate = toDate.minusYears(1);
        }

        // Validate from <= to
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.search.createdTime.range.invalid")
            );
        }

        // Validate <= 1 year
        if (fromDate != null && toDate != null) {
            long days = ChronoUnit.DAYS.between(fromDate, toDate);
            if (days > 366) { // cho phép năm nhuận
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catRecommendationType.search.createdTime.range.maxOneYear")
                );
            }
        }

        Specification<CatRecommendationType> specification = buildSearchSpecification(request, fromDate, toDate);

        Sort sort = Sort.by(direction, sortProperty);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<CatRecommendationType> resultPage = repository.findAll(specification, pageRequest);

        List<CatRecommendationTypeDto> dtoList = resultPage.getContent()
                .stream()
                .map(mapper::toDto)
                .toList();

        return new PageImpl<>(dtoList, pageRequest, resultPage.getTotalElements());
    }

    private Specification<CatRecommendationType> buildSearchSpecification(
            RecommendationTypeSearchRequestDto request,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("isDeleted")));

            String kw = safeTrim(request.getKeyword());
            if (kw != null) {
                String like = "%" + kw.toUpperCase() + "%";
                Expression<String> typeNameExp = cb.upper(cb.trim(root.get("typeName")));
                predicates.add(cb.like(typeNameExp, like));
            }

            String status = defaultIfBlank(request.getStatus(), "ALL").toUpperCase();
            if ("ACTIVE".equals(status)) {
                predicates.add(cb.isTrue(root.get("isActive")));
            } else if ("INACTIVE".equals(status)) {
                predicates.add(cb.isFalse(root.get("isActive")));
            } else if (!"ALL".equals(status)) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catRecommendationType.search.status.invalid")
                );
            }

            if (fromDate != null && toDate != null) {
                LocalDateTime from = fromDate.atStartOfDay();
                LocalDateTime toExclusive = toDate.plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
                predicates.add(cb.lessThan(root.get("createdAt"), toExclusive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // -------------------- CRUD --------------------

    @Override
    public CatRecommendationTypeDto create(CatRecommendationTypeDto request) {
        validateRequest(request);

        // Unique (exclude deleted)
        validateUniqueTypeName(null, request.getTypeName());

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // Sanitize
        String typeCode = safeTrim(request.getTypeCode());
        String typeName = safeTrim(request.getTypeName());

        CatRecommendationType entity = mapper.toEntity(request);

        entity.setTypeCode(typeCode);
        entity.setTypeName(typeName);

        entity.setIsDeleted(Boolean.FALSE);
        entity.setIsActive(request.getIsActive());

        // (1) create: set created_by, updated_by = userId; created_at, updated_at = now
        entity.setCreatedAt(now);
        entity.setCreatedBy(userId);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(userId);

        try {
            CatRecommendationType saved = repository.save(entity);
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw mapDataIntegrityViolation(ex, request.getTypeName());
        }
    }

    @Override
    public CatRecommendationTypeDto update(Long id, CatRecommendationTypeDto request) {
        CatRecommendationType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catRecommendationType.notFound", id)
                ));

        validateRequest(request);
        validateUniqueTypeName(id, request.getTypeName());

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // Sanitize
        String typeCode = safeTrim(request.getTypeCode());
        String typeName = safeTrim(request.getTypeName());

        // Map allowed fields only (mapper ignores id/createdAt/createdBy/isDeleted)
        mapper.updateEntity(entity, request);

        entity.setTypeCode(typeCode);
        entity.setTypeName(typeName);

        entity.setIsDeleted(Boolean.FALSE);
        entity.setIsActive(request.getIsActive());

        // (2) update: set updated_by = userId; updated_at = now
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(userId);

        try {
            CatRecommendationType saved = repository.save(entity);
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw mapDataIntegrityViolation(ex, request.getTypeName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CatRecommendationTypeDto getById(Long id) {
        CatRecommendationType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catRecommendationType.notFound", id)
                ));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatRecommendationTypeDto> getAll() {
        return repository.findAllByIsDeletedFalse()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Soft delete multiple items.
     */
    @Override
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.delete.ids.required")
            );
        }

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        List<CatRecommendationType> entities = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> repository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new CustomException(
                                HttpStatus.NOT_FOUND.value(),
                                translator.getMessage("catRecommendationType.notFound", id)
                        )))
                .collect(Collectors.toList());

        // (3) delete: set updated_by = userId; updated_at = now
        // (4) delete multiple: ids list
        for (CatRecommendationType e : entities) {
            e.setIsDeleted(Boolean.TRUE);
            e.setUpdatedAt(now);
            e.setUpdatedBy(userId);
        }
        repository.saveAll(entities);
    }

    // -------------------- Validation helpers --------------------

    private void validateRequest(CatRecommendationTypeDto request) {
        if (request == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.payload.null")
            );
        }

        if (!StringUtils.hasText(request.getTypeName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.typeName.required")
            );
        }

        if (request.getIsActive() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.isActive.required")
            );
        }
    }

    /**
     * Validate unique index:
     * UX_CAT_REC_TYPE_NAME_ACT on:
     *   CASE WHEN IS_DELETED='N' THEN UPPER(TRIM(TYPE_NAME)) END
     *
     * Because entity stores Boolean isDeleted, we check isDeleted=false only.
     */
    private void validateUniqueTypeName(Long excludeId, String inputTypeName) {
        String normalized = normalizeTypeName(inputTypeName);
        if (!StringUtils.hasText(normalized)) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.typeName.required")
            );
        }

        List<CatRecommendationType> existing = repository.findAllByIsDeletedFalse();

        boolean duplicated = existing.stream().anyMatch(e -> {
            if (excludeId != null && excludeId.equals(e.getId())) {
                return false;
            }
            String currentNorm = normalizeTypeName(e.getTypeName());
            return normalized.equals(currentNorm);
        });

        if (duplicated) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.typeName.duplicate", safeTrim(inputTypeName))
            );
        }
    }

    private String normalizeTypeName(String typeName) {
        String v = safeTrim(typeName);
        return v == null ? null : v.toUpperCase();
    }

    private String safeTrim(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private CustomException mapDataIntegrityViolation(DataIntegrityViolationException ex, String typeName) {
        return new CustomException(
                HttpStatus.BAD_REQUEST.value(),
                translator.getMessage("catRecommendationType.typeName.duplicate", safeTrim(typeName))
        );
    }

    private String defaultIfBlank(String s, String defaultVal) {
        return StringUtils.hasText(s) ? s.trim() : defaultVal;
    }
}
