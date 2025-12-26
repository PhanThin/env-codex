package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatOutstandingTypeDto;
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

        CatOutstandingType entity = mapper.toEntity(request);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setCreatedAt(LocalDateTime.now());

        CatOutstandingType saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public OutstandingTypeDto update(Long id, OutstandingTypeDto request) {
        validateRequest(request);

        CatOutstandingType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catOutstandingType.notFound", id)
                ));

        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

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

        if (!StringUtils.hasText(request.getTypeCode())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.typeCode.required")
            );
        }

        if (!StringUtils.hasText(request.getTypeName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catOutstandingType.typeName.required")
            );
        }
    }
}
