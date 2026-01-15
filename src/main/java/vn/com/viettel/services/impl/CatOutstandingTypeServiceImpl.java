package vn.com.viettel.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.OutstandingTypeDto;
import vn.com.viettel.dto.OutstandingTypeSearchRequestDto;
import vn.com.viettel.entities.CatOutstandingType;
import vn.com.viettel.mapper.CatOutstandingTypeMapper;
import vn.com.viettel.repositories.jpa.CatOutstandingTypeRepository;
import vn.com.viettel.services.CatOutstandingTypeService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

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

    @Override
    public OutstandingTypeDto create(OutstandingTypeDto request) {
        validateRequest(request);
        validateAuditFieldsNotAllowed(request);
        validateNotNullConstraintsOnCreate(request);
        validateUniqueTypeName(null, request.getTypeName());

        CatOutstandingType entity = mapper.toEntity(request);

        // Backend tự set theo yêu cầu
        entity.setIsDeleted(Boolean.FALSE);
        if (entity.getIsActive() == null) {
            entity.setIsActive(Boolean.TRUE);
        }
        entity.setCreatedAt(LocalDateTime.now());

        CatOutstandingType saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public OutstandingTypeDto update(Long id, OutstandingTypeDto request) {
        CatOutstandingType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catOutstandingType.notFound", id)
                ));
        validateRequest(request);
        validateAuditFieldsNotAllowed(request);
        validateNotNullConstraintsOnUpdate(request);



        // Validate unique type_name theo index function-based:
        // UNIQUE (CASE WHEN IS_DELETED='N' THEN UPPER(TRIM(TYPE_NAME)) END)
        validateUniqueTypeName(id, request.getTypeName());

        // Chống bị mapper overwrite audit create
        LocalDateTime createdAt = entity.getCreatedAt();
        Long createdBy = entity.getCreatedBy();

        mapper.updateEntity(entity, request);

        // Restore audit create fields (client không được phép set)
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy(createdBy);

        // Backend tự set audit update
        entity.setUpdatedAt(LocalDateTime.now());
        // updatedBy: backend tự set theo context user (nếu có). Ở đây không lấy được nên giữ nguyên / null.

        CatOutstandingType saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingTypeDto getById(Long id) {
        CatOutstandingType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catOutstandingType.notFound", id)
                ));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutstandingTypeDto> getAll() {
        return repository.findAllByIsDeletedFalse()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        CatOutstandingType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catOutstandingType.notFound", id)
                ));

        entity.setIsDeleted(Boolean.TRUE);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    /**
     * Minimal request validation (beyond bean validation) to follow enterprise style.
     */
    private void validateRequest(OutstandingTypeDto request) {
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

    /**
     * Validate constraint NOT NULL theo danh sách bạn đưa:
     * - TYPE_NAME NOT NULL
     * - IS_ACTIVE NOT NULL
     * - IS_DELETED NOT NULL
     * - CREATED_AT NOT NULL (backend set)
     */
    private void validateNotNullConstraintsOnCreate(OutstandingTypeDto request) {
        // TYPE_NAME đã check ở validateRequest()

        // IS_ACTIVE: nếu DTO có field mà client gửi null -> lỗi.
        // Nếu DTO không có field thì entity sẽ default TRUE.
        if (hasProperty(request, "isActive")) {
            Object val = getProperty(request, "isActive");
            if (val == null) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catOutstandingType.isActive.required")
                );
            }
        }

        // IS_DELETED: client không được gửi; backend set false.
        // Nếu DTO có field và client gửi lên (kể cả null / non-null) -> chặn bằng validateAuditFieldsNotAllowed + validate below
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

    private void validateNotNullConstraintsOnUpdate(OutstandingTypeDto request) {
        // TYPE_NAME đã check ở validateRequest()

        // IS_ACTIVE NOT NULL (nếu DTO có field mà mapper có thể set null -> chặn)
        if (hasProperty(request, "isActive")) {
            Object val = getProperty(request, "isActive");
            if (val == null) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catOutstandingType.isActive.required")
                );
            }
        }

        // IS_DELETED: update không cho client can thiệp
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

    /**
     * Validate UNIQUE index:
     * create unique index ... on (CASE WHEN IS_DELETED='N' THEN UPPER(TRIM(TYPE_NAME)) END)
     *
     * => Với các bản ghi IS_DELETED = false, TYPE_NAME sau khi TRIM + UPPER phải là duy nhất.
     */
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

    /**
     * (Yêu cầu #2) Chặn client gửi các trường audit:
     * createdAt, createdBy, updatedAt, updatedBy
     * và cả biến thể: createAt, createBy, updateAt, updateBy (nếu DTO đang dùng naming khác)
     */
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
    public Page<OutstandingTypeDto> search(OutstandingTypeSearchRequestDto request) {
        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        // --- Validate & chuẩn hóa sortBy ---
        String sortByRaw = defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = OUTSTANDING_TYPE_ALLOWED_SORT_FIELDS.get(sortByRaw);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.search.sortBy.invalid", OUTSTANDING_TYPE_ALLOWED_SORT_FIELDS.keySet())
            );
        }

        // --- Validate & chuẩn hóa sortDirection ---
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

        // --- Normalize date range: default trong năm hiện tại + tối đa 1 năm ---
        LocalDateTime now = LocalDateTime.now();
        LocalDate fromDate = request.getCreatedFrom();
        LocalDate toDate = request.getCreatedTo();

        // Nếu không truyền -> default: từ đầu năm hiện tại đến hôm nay
        if (fromDate == null && toDate == null) {
            fromDate = LocalDate.of(now.getYear(), 1, 1);
            toDate = now.toLocalDate();
        }

        // Nếu chỉ truyền 1 đầu -> tự hoàn thiện đầu còn lại
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
                    translator.getMessage("catOutstandingType.search.createdTime.range.invalid")
            );
        }

        // Validate <= 1 year
        if (fromDate != null && toDate != null) {
            long days = ChronoUnit.DAYS.between(fromDate, toDate);
            if (days > 366) { // cho phép năm nhuận
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catOutstandingType.search.createdTime.range.maxOneYear")
                );
            }
        }

        Specification<CatOutstandingType> specification = buildOutstandingTypeSearchSpecification(request, fromDate, toDate);

        Sort sort = Sort.by(direction, sortProperty);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<CatOutstandingType> resultPage = repository.findAll(specification, pageRequest);

        List<OutstandingTypeDto> dtoList = resultPage.getContent()
                .stream()
                .map(mapper::toDto)
                .toList();

        return new PageImpl<>(dtoList, pageRequest, resultPage.getTotalElements());
    }

    private Specification<CatOutstandingType> buildOutstandingTypeSearchSpecification(
            OutstandingTypeSearchRequestDto request,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter isDeleted = false
            predicates.add(cb.isFalse(root.get("isDeleted")));

            // Keyword: search by TYPE_NAME only (không dùng type_code)
            String kw = safeTrim(request.getKeyword());
            if (kw != null) {
                String like = "%" + kw.toUpperCase(Locale.ROOT) + "%";
                Expression<String> typeNameExp = cb.upper(cb.trim(root.get("typeName")));
                predicates.add(cb.like(typeNameExp, like));
            }

            // Status: ALL/ACTIVE/INACTIVE
            String status = defaultIfBlank(request.getStatus(), "ALL").toUpperCase(Locale.ROOT);
            if ("ACTIVE".equals(status)) {
                predicates.add(cb.isTrue(root.get("isActive")));
            } else if ("INACTIVE".equals(status)) {
                predicates.add(cb.isFalse(root.get("isActive")));
            } else if (!"ALL".equals(status)) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("catOutstandingType.search.status.invalid")
                );
            }

            // CreatedAt range (inclusive)
            if (fromDate != null && toDate != null) {
                LocalDateTime from = fromDate.atStartOfDay();
                LocalDateTime toExclusive = toDate.plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
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
}
