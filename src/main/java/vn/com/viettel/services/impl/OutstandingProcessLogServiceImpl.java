package vn.com.viettel.services.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.OutstandingProcessLogDto;
import vn.com.viettel.entities.OutstandingProcessLog;
import vn.com.viettel.mapper.OutstandingProcessLogMapper;
import vn.com.viettel.repositories.jpa.OutstandingItemRepository;
import vn.com.viettel.repositories.jpa.OutstandingProcessLogRepository;
import vn.com.viettel.services.OutstandingProcessLogService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

/**
 * CRUD-only service implementation for OUTSTANDING_PROCESS_LOG.
 * No extra fields, no business logic beyond required validations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OutstandingProcessLogServiceImpl implements OutstandingProcessLogService {

    private static final Set<String> ALLOWED_ACTION_TYPES =
            new HashSet<>(Arrays.asList("SAVE_RESULT", "SEND_FOR_ACCEPTANCE"));

    private final OutstandingProcessLogRepository repository;
    private final OutstandingItemRepository outstandingItemRepository;
    private final OutstandingProcessLogMapper mapper;
    private Translator translator;

    @Override
    public OutstandingProcessLogDto create(Long outstandingId, OutstandingProcessLogDto request) {
        validateOutstandingExists(outstandingId);
        validateRequest(request);

        OutstandingProcessLog entity = mapper.toEntity(request);
        entity.setOutstandingId(outstandingId);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(entity));
    }

    @Override
    public OutstandingProcessLogDto update(Long outstandingId, Long processId, OutstandingProcessLogDto request) {
        validateOutstandingExists(outstandingId);
        validateRequest(request);

        OutstandingProcessLog entity = getOrThrow(outstandingId, processId);
        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingProcessLogDto getById(Long outstandingId, Long processId) {
        validateOutstandingExists(outstandingId);
        return mapper.toDto(getOrThrow(outstandingId, processId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutstandingProcessLogDto> getAll(Long outstandingId) {
        validateOutstandingExists(outstandingId);
        return repository.findAllByOutstandingIdAndIsDeletedFalseOrderByUpdatedAtDesc(outstandingId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long outstandingId, Long processId) {
        validateOutstandingExists(outstandingId);
        OutstandingProcessLog entity = getOrThrow(outstandingId, processId);
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

    private void validateRequest(OutstandingProcessLogDto request) {
        if (request == null
                || !StringUtils.hasText(request.getActionType())
                || !StringUtils.hasText(request.getProcessContent())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("invalid.request")
            );
        }

        if (!ALLOWED_ACTION_TYPES.contains(request.getActionType())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.process.action.invalid", request.getActionType())
            );
        }
    }

    private OutstandingProcessLog getOrThrow(Long outstandingId, Long processId) {
        OutstandingProcessLog entity = repository.findByIdAndIsDeletedFalse(processId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("outstanding.process.notfound", processId)
                ));

        if (!outstandingId.equals(entity.getOutstandingId())) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("outstanding.process.notfound", processId)
            );
        }
        return entity;
    }
}
