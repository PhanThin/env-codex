
package vn.com.viettel.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.dto.OutstandingAcceptanceDto;
import vn.com.viettel.dto.OutstandingAcceptanceResultEnum;
import vn.com.viettel.dto.OutstandingStatusEnum;
import vn.com.viettel.entities.OutstandingAcceptance;
import vn.com.viettel.entities.OutstandingItem;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.OutstandingAcceptanceMapper;
import vn.com.viettel.repositories.jpa.OutstandingAcceptanceRepository;
import vn.com.viettel.repositories.jpa.OutstandingItemRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.AttachmentService;
import vn.com.viettel.services.OutstandingAcceptanceService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CRUD-only service for OUTSTANDING_ACCEPTANCE.
 */
@Service
@Transactional
public class OutstandingAcceptanceServiceImpl implements OutstandingAcceptanceService {

    private static final Set<OutstandingAcceptanceResultEnum> ALLOWED_RESULTS =
            new HashSet<>(Arrays.asList(OutstandingAcceptanceResultEnum.ACCEPTED, OutstandingAcceptanceResultEnum.REJECTED));

    @Autowired
    private OutstandingAcceptanceRepository repository;
    @Autowired
    private OutstandingItemRepository outstandingItemRepository;
    @Qualifier("outstandingAcceptanceMapperDecorator")
    @Autowired
    private OutstandingAcceptanceMapper mapper;
    @Autowired
    private Translator translator;
    @Autowired
    private SysUserRepository userRepository;
    @Autowired
    private AttachmentService attachmentService;

    @Override
    @Transactional
    public OutstandingAcceptanceDto create(Long outstandingId, OutstandingAcceptanceDto request, MultipartFile[] files) {
        OutstandingItem outstandingItem = outstandingItemRepository.findByIdAndIsDeletedFalse(outstandingId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("outstandingitem.notFound", outstandingId)));

        validateRequest(request);

        Long currentUserId = getCurrentUserIdOrDefault();

        OutstandingAcceptance entity = mapper.toEntity(request);
        entity.setResult(request.getResult().name());
        entity.setOutstandingId(outstandingId);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setAcceptedAt(LocalDateTime.now());
        entity.setAcceptedBy(currentUserId);
        repository.save(entity);

        attachmentService.handleAttachment(files, entity.getId(), Constants.OUTSTANDING_ACCEPTANCE_REFERENCE_TYPE, Constants.OUTSTANDING_REFERENCE_TYPE + "/" + outstandingId + "/" + Constants.OUTSTANDING_ACCEPTANCE_REFERENCE_TYPE);

        if (OutstandingAcceptanceResultEnum.ACCEPTED.equals(request.getResult())) {
            outstandingItem.setStatus(OutstandingStatusEnum.CLOSED.name());
            outstandingItem.setLastUpdateAt(LocalDateTime.now());
            outstandingItem.setLastUpdateBy(currentUserId);
            outstandingItemRepository.save(outstandingItem);

        } else if (OutstandingAcceptanceResultEnum.REJECTED.equals(request.getResult())) {
            outstandingItem.setStatus(OutstandingStatusEnum.NEW.name());
            outstandingItem.setLastUpdateAt(LocalDateTime.now());
            outstandingItem.setLastUpdateBy(currentUserId);
            outstandingItemRepository.save(outstandingItem);

        }

        return mapper.toDtoList(List.of(entity)).getFirst();
    }

    @Override
    @Transactional
    public OutstandingAcceptanceDto update(Long outstandingId, Long acceptanceId, OutstandingAcceptanceDto request, MultipartFile[] files) {
        validateOutstandingExists(outstandingId);
        validateRequest(request);
        Long currentUserId = getCurrentUserIdOrDefault();
        OutstandingAcceptance entity = getOrThrow(outstandingId, acceptanceId);
        mapper.updateEntity(entity, request);
        entity.setResult(request.getResult().name());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(currentUserId);
        repository.save(entity);

        attachmentService.handleAttachment(files, entity.getId(), Constants.OUTSTANDING_ACCEPTANCE_REFERENCE_TYPE, Constants.OUTSTANDING_REFERENCE_TYPE + "/" + outstandingId + "/" + Constants.OUTSTANDING_ACCEPTANCE_REFERENCE_TYPE);

        if (request.getDeletedAttachments() != null && !request.getDeletedAttachments().isEmpty()) {
            attachmentService.deleteAttachmentsById(request.getDeletedAttachments().stream().map(AttachmentDto::getId).toList(), currentUserId);
        }

        return mapper.toDtoList(List.of(entity)).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingAcceptanceDto get(Long outstandingId, Long acceptanceId) {
        validateOutstandingExists(outstandingId);

        OutstandingAcceptance entity = repository.findByIdAndIsDeletedFalse(acceptanceId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("outstanding.acceptance.notfound", acceptanceId)
                ));

        return mapper.toDtoList(List.of(entity)).getFirst();
    }

    @Override
    public void delete(Long outstandingId, Long acceptanceId) {
        validateOutstandingExists(outstandingId);

        OutstandingAcceptance entity = getOrThrow(outstandingId, acceptanceId);
        entity.setIsDeleted(Boolean.TRUE);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUserIdOrDefault());
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OutstandingAcceptanceDto> getAll(Long outstandingId) {
        validateOutstandingExists(outstandingId);
        return mapper.toDtoList(repository.findAllByOutstandingIdAndIsDeletedFalse(outstandingId));
    }

    private void validateOutstandingExists(Long outstandingId) {
        if (outstandingId == null || !outstandingItemRepository.existsByIdAndIsDeletedFalse(outstandingId)) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("outstandingitem.notFound", outstandingId)
            );
        }
    }

    private void validateRequest(OutstandingAcceptanceDto request) {
        if (request == null
                || request.getResult() == null || request.getResult().name().isEmpty()
                || !StringUtils.hasText(request.getAcceptanceNote())) {
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

    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof SysUser user) {
                return user;
            }
            // Logic dự phòng nếu principal là String username
            return userRepository.findByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }

    private Long getCurrentUserIdOrDefault() {
        SysUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
    }
}
