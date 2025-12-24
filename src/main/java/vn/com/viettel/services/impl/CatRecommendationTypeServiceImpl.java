package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatRecommendationTypeDto;
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

    @Override
    public CatRecommendationTypeDto create(CatRecommendationTypeDto request) {
        validateRequest(request);

        CatRecommendationType entity = mapper.toEntity(request);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setCreatedAt(LocalDateTime.now());

        CatRecommendationType saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public CatRecommendationTypeDto update(Long id, CatRecommendationTypeDto request) {
        validateRequest(request);

        CatRecommendationType entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catRecommendationType.notFound", id)
                ));

        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        CatRecommendationType saved = repository.save(entity);
        return mapper.toDto(saved);
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

        entity.setIsDeleted(Boolean.TRUE);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateRequest(CatRecommendationTypeDto request) {
        if (request == null
                || !StringUtils.hasText(request.getTypeCode())
                || !StringUtils.hasText(request.getTypeName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("invalid.request")
            );
        }
    }
}
