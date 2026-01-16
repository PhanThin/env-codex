package vn.com.viettel.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import vn.com.viettel.dto.CatRecommendationTypeDto;
import vn.com.viettel.dto.RecommendationTypeSearchRequestDto;
import vn.com.viettel.entities.CatRecommendationType;
import vn.com.viettel.mapper.CatRecommendationTypeMapper;
import vn.com.viettel.repositories.jpa.CatRecommendationTypeRepository;
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

        // --- Normalize date range (Word: mặc định trong năm hiện tại + tối đa 1 năm) ---
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
            // Lùi 1 năm (đúng yêu cầu range <= 1 năm)
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

        // Build specification
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

            // Always filter IS_DELETED = 'N'  (Boolean false)
            predicates.add(cb.isFalse(root.get("isDeleted")));

            // Keyword -> search by TYPE_NAME only (không dùng TYPE_CODE)
            String kw = safeTrim(request.getKeyword());
            if (kw != null) {
                String like = "%" + kw.toUpperCase() + "%";
                Expression<String> typeNameExp = cb.upper(cb.trim(root.get("typeName")));
                predicates.add(cb.like(typeNameExp, like));
            }

            // Status filter: ALL/ACTIVE/INACTIVE
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

            // CreatedAt range (inclusive)
            if (fromDate != null && toDate != null) {
                LocalDateTime from = fromDate.atStartOfDay();
                // inclusive end -> dùng < (to + 1 day) để tránh lệch time
                LocalDateTime toExclusive = toDate.plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
                predicates.add(cb.lessThan(root.get("createdAt"), toExclusive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public CatRecommendationTypeDto create(CatRecommendationTypeDto request) {
        validateRequest(request);

        // Validate UNIQUE INDEX:
        // UX_CAT_REC_TYPE_NAME_ACT: CASE WHEN IS_DELETED='N' THEN UPPER(TRIM(TYPE_NAME)) END
        validateUniqueTypeName(null, request.getTypeName());

        // Sanitize value before save (match index definition TRIM + UPPER for compare)
        String typeCode = safeTrim(request.getTypeCode());
        String typeName = safeTrim(request.getTypeName());

        CatRecommendationType entity = mapper.toEntity(request);

        // Do NOT allow client to set audit fields (backend owns)
        entity.setTypeCode(typeCode);
        entity.setTypeName(typeName);

        entity.setIsDeleted(Boolean.FALSE);                 // IS_DELETED NOT NULL
        entity.setIsActive(request.getIsActive());          // IS_ACTIVE NOT NULL
        entity.setCreatedAt(LocalDateTime.now());           // CREATED_AT NOT NULL
        entity.setCreatedBy(getCurrentUserIdOrNull());      // backend set (nullable in DB)
        entity.setUpdatedAt(null);
        entity.setUpdatedBy(null);

        try {
            CatRecommendationType saved = repository.save(entity);
            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            // If race condition happens, still map to friendly message
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
        // Validate UNIQUE INDEX (exclude current record)
        validateUniqueTypeName(id, request.getTypeName());

        // Sanitize
        String typeCode = safeTrim(request.getTypeCode());
        String typeName = safeTrim(request.getTypeName());

        // Map allowed fields only (mapper already ignores id/createdAt/createdBy/isDeleted)
        mapper.updateEntity(entity, request);

        // Enforce audit fields are backend-owned
        entity.setTypeCode(typeCode);
        entity.setTypeName(typeName);

        // Do NOT allow client to flip isDeleted via update
        entity.setIsDeleted(Boolean.FALSE);                 // keep NOT DELETED
        entity.setIsActive(request.getIsActive());          // IS_ACTIVE NOT NULL

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUserIdOrNull());      // backend set (nullable in DB)

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

    @Override
    public void delete(Long id) {
        CatRecommendationType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catRecommendationType.notFound", id)
                ));

        // Soft delete
        entity.setIsDeleted(Boolean.TRUE);                  // IS_DELETED NOT NULL
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUserIdOrNull());

        repository.save(entity);
    }

    private void validateRequest(CatRecommendationTypeDto request) {
        if (request == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.payload.null")
            );
        }

        // TYPE_NAME NOT NULL
        if (!StringUtils.hasText(request.getTypeName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.typeName.required")
            );
        }

        // IS_ACTIVE NOT NULL (DB constraint SYS_C008677 + CK in ('N','Y'))
        // In code: Boolean <-> 'Y'/'N', so just ensure not null
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
            // already handled by required validation, but keep safe
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationType.typeName.required")
            );
        }

        // Reuse existing repository method (avoid changing repository now)
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
        // Match DB unique index behavior: UPPER(TRIM(TYPE_NAME))
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
        // In this module we mainly care about UX_CAT_REC_TYPE_NAME_ACT
        // Return friendly duplicate message (safe even if DB throws for other reasons).
        return new CustomException(
                HttpStatus.BAD_REQUEST.value(),
                translator.getMessage("catRecommendationType.typeName.duplicate", safeTrim(typeName))
        );
    }

    private String defaultIfBlank(String s, String defaultVal) {
        return StringUtils.hasText(s) ? s.trim() : defaultVal;
    }

    /**
     * TODO: Replace by real current user id from SecurityContext or your auth mechanism.
     * Requirement: client cannot send createdBy/updatedBy -> backend owns these.
     */
    private Long getCurrentUserIdOrNull() {
        return null;
    }
}
