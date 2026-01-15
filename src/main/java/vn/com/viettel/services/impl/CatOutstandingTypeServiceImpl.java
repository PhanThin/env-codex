package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.OutstandingTypeDto;
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
}
