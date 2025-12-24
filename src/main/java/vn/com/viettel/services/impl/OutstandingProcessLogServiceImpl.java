package vn.com.viettel.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.OutstandingProcessLogDto;
import vn.com.viettel.entities.OutstandingProcessLog;
import vn.com.viettel.mapper.OutstandingProcessLogMapper;
import vn.com.viettel.minio.services.FileService;
import vn.com.viettel.repositories.jpa.OutstandingItemRepository;
import vn.com.viettel.repositories.jpa.OutstandingProcessLogRepository;
import vn.com.viettel.services.AttachmentService;
import vn.com.viettel.services.OutstandingProcessLogService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @Qualifier("outstandingProcessLogMapperDecorator")
    private final OutstandingProcessLogMapper mapper;
    private final Translator translator;
    private final AttachmentService attachmentService;
    private final FileService fileService;

    @Override
    public OutstandingProcessLogDto create(Long outstandingId, OutstandingProcessLogDto request, MultipartFile[] attachments) {
        validateOutstandingExists(outstandingId);
        validateRequest(request);

        OutstandingProcessLog entity = mapper.toEntity(request);
        entity.setOutstandingId(outstandingId);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
        String channel = Constants.OUTSTANDING_REFERENCE_TYPE + "/" + outstandingId + "/" + Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE;
        attachmentService.handleAttachment(attachments, entity.getId(), Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE, channel);
        return mapper.toDto(entity);
    }


    @Override
    public OutstandingProcessLogDto update(Long outstandingId, Long processId, OutstandingProcessLogDto request, MultipartFile[] attachments) {
        validateOutstandingExists(outstandingId);
        validateRequest(request);

        OutstandingProcessLog entity = getOrThrow(outstandingId, processId);
        mapper.updateEntity(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);

        String channel = Constants.OUTSTANDING_REFERENCE_TYPE + "/" + outstandingId + "/" + Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE;
        attachmentService.handleAttachment(attachments, entity.getId(), Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE, channel);

        return mapper.toDto(entity);
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
        return mapper.toDtoList(repository.findAllByOutstandingIdAndIsDeletedFalseOrderByUpdatedAtDesc(outstandingId));
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
