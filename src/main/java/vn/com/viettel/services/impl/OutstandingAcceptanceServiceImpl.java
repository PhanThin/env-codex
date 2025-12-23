
package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.OutstandingAcceptanceDto;
import vn.com.viettel.entities.OutstandingAcceptance;
import vn.com.viettel.mapper.OutstandingAcceptanceMapper;
import vn.com.viettel.repositories.jpa.OutstandingAcceptanceRepository;
import vn.com.viettel.repositories.jpa.OutstandingItemRepository;
import vn.com.viettel.services.OutstandingAcceptanceService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

/**
 * CRUD-only service for OUTSTANDING_ACCEPTANCE.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OutstandingAcceptanceServiceImpl implements OutstandingAcceptanceService {

    private static final Set<String> ALLOWED_RESULTS =
            new HashSet<>(Arrays.asList("PASS", "FAILED"));

    private final OutstandingAcceptanceRepository repository;
    private final OutstandingItemRepository outstandingItemRepository;
    private final OutstandingAcceptanceMapper mapper;
    private Translator translator;

    @Override
    public OutstandingAcceptanceDto create(Long outstandingId, OutstandingAcceptanceDto request) {
        validateOutstandingExists(outstandingId);

        if (repository.existsByOutstandingIdAndIsDeletedFalse(outstandingId)) {
            throw new CustomException(
                    HttpStatus.CONFLICT.value(),
                    translator.getMessage("outstanding.acceptance.exists", outstandingId)
            );
        }

        validateRequest(request);

        OutstandingAcceptance entity = mapper.toEntity(request);
        entity.setOutstandingId(outstandingId);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(entity));
    }

    @Override
    public OutstandingAcceptanceDto update(Long outstandingId, Long acceptanceId, OutstandingAcceptanceDto request) {
        validateOutstandingExists(outstandingId);
        validateRequest(request);

        OutstandingAcceptance entity = getOrThrow(outstandingId, acceptanceId);
        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingAcceptanceDto get(Long outstandingId) {
        validateOutstandingExists(outstandingId);

        OutstandingAcceptance entity = repository.findByOutstandingIdAndIsDeletedFalse(outstandingId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("outstanding.acceptance.notfound", outstandingId)
                ));

        return mapper.toDto(entity);
    }

    @Override
    public void delete(Long outstandingId, Long acceptanceId) {
        validateOutstandingExists(outstandingId);

        OutstandingAcceptance entity = getOrThrow(outstandingId, acceptanceId);
        entity.setIsDeleted(Boolean.TRUE);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateOutstandingExists(Long outstandingId) {
        if (outstandingId == null || !outstandingItemRepository.existsByIdAndIsDeletedFalse(outstandingId)) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("outstanding.notfound", outstandingId)
            );
        }
    }

    private void validateRequest(OutstandingAcceptanceDto request) {
        if (request == null
                || !StringUtils.hasText(request.getResult())
                || !StringUtils.hasText(request.getAcceptanceNote())
                || request.getAcceptedBy() == null
                || request.getAcceptedAt() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("invalid.request")
            );
        }

        if (!ALLOWED_RESULTS.contains(request.getResult())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.acceptance.result.invalid", request.getResult())
            );
        }
    }

    private OutstandingAcceptance getOrThrow(Long outstandingId, Long acceptanceId) {
        OutstandingAcceptance entity = repository.findByIdAndIsDeletedFalse(acceptanceId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("outstanding.acceptance.notfound", acceptanceId)
                ));

        if (!outstandingId.equals(entity.getOutstandingId())) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("outstanding.acceptance.notfound", acceptanceId)
            );
        }
        return entity;
    }
}
