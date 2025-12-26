package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatRecommendationSourceDto;
import vn.com.viettel.entities.CatRecommendationSource;
import vn.com.viettel.mapper.CatRecommendationSourceMapper;
import vn.com.viettel.repositories.jpa.CatRecommendationSourceRepository;
import vn.com.viettel.services.CatRecommendationSourceService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

/**
 * Service implementation for CAT_RECOMMENDATION_SOURCE CRUD operations.
 * CRUD only, no business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CatRecommendationSourceServiceImpl implements CatRecommendationSourceService {

    private final CatRecommendationSourceRepository repository;
    private final CatRecommendationSourceMapper mapper;
    private final Translator translator;
    @Override
    public CatRecommendationSourceDto create(CatRecommendationSourceDto request) {
        validateRequest(request);

        CatRecommendationSource entity = mapper.toEntity(request);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setCreatedAt(LocalDateTime.now());

        CatRecommendationSource saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public CatRecommendationSourceDto update(Long id, CatRecommendationSourceDto request) {
        validateRequest(request);

        CatRecommendationSource entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catRecommendationSource.notFound", id)
                ));

        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        CatRecommendationSource saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CatRecommendationSourceDto getById(Long id) {
        CatRecommendationSource entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catRecommendationSource.notFound", id)
                ));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatRecommendationSourceDto> getAll() {
        return repository.findAllByIsDeletedFalse()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        CatRecommendationSource entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("catRecommendationSource.notFound", id)
                ));

        entity.setIsDeleted(Boolean.TRUE);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateRequest(CatRecommendationSourceDto request) {

        if (request == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationSource.payload.null")
            );
        }

        if (!StringUtils.hasText(request.getSourceCode())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationSource.sourceCode.required")
            );
        }

        if (!StringUtils.hasText(request.getSourceName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationSource.sourceName.required")
            );
        }

        if (request.getIsActive() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("catRecommendationSource.isActive.required")
            );
        }
    }

}
