package vn.com.viettel.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.dto.OutstandingItemDto;
import vn.com.viettel.dto.OutstandingStatusEnum;
import vn.com.viettel.dto.PriorityEnum;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.OutstandingItemMapper;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.OutstandingItemService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OutstandingItemServiceImpl implements OutstandingItemService {

    @Autowired
    private OutstandingItemRepository outstandingItemRepository;
    @Autowired
    private OutstandingAlertConfigRepository outstandingAlertConfigRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectItemRepository projectItemRepository;

    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private CatOutstandingTypeRepository outstandingTypeRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private OutstandingAcceptanceRepository acceptanceRepository;

    @Autowired
    private OutstandingProcessLogRepository processLogRepository;

    @Autowired
    private Translator translator;

    @Autowired
    private OutstandingItemMapper outstandingItemMapper;

    /**
     * Validate dữ liệu khi tạo / cập nhật OutstandingItem
     *
     * @param dto      payload gửi lên
     * @param isUpdate true nếu là update, false nếu là create
     */
    private void validate(OutstandingItemDto dto, boolean isUpdate) {
        if (dto == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.payload.null")
            );
        }

        // 1. Validate tiêu đề tồn tại
        if (StringUtils.isBlank(dto.getOutstandingTitle())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.title.required")
            );
        } else if (dto.getOutstandingTitle().length() > 250) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.title.length")
            );
        } else {
            Optional<OutstandingItem> existing =
                    outstandingItemRepository.findByOutstandingTitleAndIsDeletedFalse(dto.getOutstandingTitle());

            if (existing.isPresent()) {
                // Nếu update thì cho phép trùng tên với chính nó
                if (isUpdate && !existing.get().getId().equals(dto.getId())) {
                    throw new CustomException(
                            HttpStatus.CONFLICT.value(),
                            translator.getMessage("outstanding.title.duplicate", dto.getOutstandingTitle())
                    );
                }
                // Nếu create thì không cho phép trùng
                if (!isUpdate) {
                    throw new CustomException(
                            HttpStatus.CONFLICT.value(),
                            translator.getMessage("outstanding.title.duplicate", dto.getOutstandingTitle())
                    );
                }
            }
        }

        // 2. Validate loại tồn tại
        if (dto.getOutstandingType() == null || dto.getOutstandingType().getId() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.type.required")
            );
        } else {
            if (!outstandingTypeRepository.existsById(dto.getOutstandingType().getId())) {
                throw new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("outstanding.type.notfound", dto.getOutstandingType().getId())
                );
            }
        }

        // 3. Validate priority
        if (dto.getPriority() == null || StringUtils.isBlank(dto.getPriority().getCode())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.priority.required")
            );
        } else {
            try {
                PriorityEnum.valueOf(dto.getPriority().getCode().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("outstanding.priority.invalid")
                );
            }
        }
        // 4. Validate project & projectItem & workItems thuộc đúng projectItem
        Long projectId = dto.getProject() == null ? null : dto.getProject().getId();
        if (projectId == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.project.required")
            );
        }

        if (!projectRepository.existsById(projectId)) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("outstanding.project.notfound", projectId)
            );
        }

        Long itemId = dto.getProjectItem() == null ? null : dto.getProjectItem().getId();
        if (itemId == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.item.required")
            );
        }

        ProjectItem projectItem = projectItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("outstanding.item.notfound", itemId)
                ));

        if (!Objects.equals(projectItem.getProjectId(), projectId)) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.item.not_belong_to_project", itemId, projectId)
            );
        }

        // 5. Validate danh sách công việc
        if (dto.getWorkItem() == null || dto.getWorkItem().getId() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.workitem.required")
            );
        }
        WorkItem wi = workItemRepository.findById(dto.getWorkItem().getId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("outstanding.workitem.notfound", dto.getWorkItem().getId())
                ));
        if (!Objects.equals(wi.getItemId(), itemId)) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.workitem.not_belong_to_item", dto.getWorkItem().getId(), itemId)
            );
        }

        // 6. Validate người/đơn vị xử lý
        if (dto.getAssignedUser() == null || dto.getAssignedUser().getId() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.assigned_user.required")
            );
        }
        if (dto.getAssignedOrg() == null || dto.getAssignedOrg().getId() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.assigned_org.required")
            );
        }
        // Có thể bổ sung check tồn tại user/org nếu có repository tương ứng

        // 7. Validate deadline
        LocalDate deadline = dto.getDeadline();
        if (deadline == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.deadline.required")
            );
        }
        if (deadline.isBefore(LocalDate.now())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.deadline.past")
            );
        }

        // 8. Một số validate phụ cho nghiệm thu điện tử (optional)
        if (StringUtils.isNotBlank(dto.getAcceptanceFileUrl())
                && dto.getAcceptanceFileUrl().length() > 1000) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.acceptance.fileurl.length")
            );
        }
        if (StringUtils.isNotBlank(dto.getAcceptanceRefId())
                && dto.getAcceptanceRefId().length() > 50) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.acceptance.refid.length")
            );
        }

        if (dto.getOutstandingAlertConfigs() != null && !dto.getOutstandingAlertConfigs().isEmpty()) {
            boolean invalidate = dto.getOutstandingAlertConfigs().stream().anyMatch(config -> config.getAlertDate() == null || config.getAlertDate().isBefore(deadline));
            if (invalidate) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("outstanding.alert.date.invalid"));
            }
        }
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

    @Transactional
    public OutstandingItemDto createOutstandingItem(OutstandingItemDto dto) {
        validate(dto, false);
        SysUser currentUser = getCurrentUser();
        OutstandingItem outstandingItem = outstandingItemMapper.toEntity(dto, currentUser);
        outstandingItem = outstandingItemRepository.save(outstandingItem);

        if (dto.getOutstandingAlertConfigs() != null && !dto.getOutstandingAlertConfigs().isEmpty()) {
            List<OutstandingAlertConfig> outstandingAlertConfigs = outstandingItemMapper.mapToOutstandingAlertConfig(dto.getOutstandingAlertConfigs(), outstandingItem.getId());
            outstandingAlertConfigRepository.saveAll(outstandingAlertConfigs);
        }
        return outstandingItemMapper.mapToOutstandingItemDto(List.of(outstandingItem)).getFirst();
    }

    @Transactional
    public OutstandingItemDto updateOutstandingItem(Long id, OutstandingItemDto dto) {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("outstandingitem.id.null"));
        }
        validate(dto, true);
        OutstandingItem outstandingItem = outstandingItemRepository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (outstandingItem == null) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("outstandingitem.notFound", id));
        }
        SysUser currentUser = getCurrentUser();
        outstandingItemMapper.updateEntityFromDto(dto, outstandingItem, currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
        outstandingItemRepository.save(outstandingItem);

        // Fetch existing alert configs
        List<OutstandingAlertConfig> existingConfigs = outstandingAlertConfigRepository.findByOutstandingIdAndIsDeletedFalse(id);
        Map<Integer, OutstandingAlertConfig> existingConfigMap = existingConfigs.stream()
                .collect(Collectors.toMap(OutstandingAlertConfig::getLevelNo, config -> config));

        if (dto.getOutstandingAlertConfigs() != null && !dto.getOutstandingAlertConfigs().isEmpty()) {
            List<OutstandingAlertConfig> configsToSave = outstandingItemMapper.mapToOutstandingAlertConfig(dto.getOutstandingAlertConfigs(), id);

            for (OutstandingAlertConfig newConfig : configsToSave) {
                OutstandingAlertConfig existingConfig = existingConfigMap.get(newConfig.getLevelNo());
                if (existingConfig != null) {
                    // Update existing config
                    existingConfig.setPercentTime(newConfig.getPercentTime());
                    existingConfig.setAlertDate(newConfig.getAlertDate());
                    existingConfig.setIsActive(newConfig.getIsActive());
                    existingConfig.setUpdatedBy(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
                    existingConfig.setUpdatedAt(LocalDateTime.now());
                    outstandingAlertConfigRepository.save(existingConfig);
                    existingConfigMap.remove(newConfig.getLevelNo());
                } else {
                    // Insert new config
                    outstandingAlertConfigRepository.save(newConfig);
                }
            }
        }

        // Delete configs not present in the incoming array
        if (!existingConfigMap.isEmpty()) {
            outstandingAlertConfigRepository.deleteAll(existingConfigMap.values());
        }

        return outstandingItemMapper.mapToOutstandingItemDto(List.of(outstandingItem)).getFirst();
    }

    @Transactional
    public void deleteOutstandingItem(List<Long> ids) {
        if (ids == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("outstandingitem.id.null"));
        }
        List<OutstandingItem> outstandingItems = outstandingItemRepository.findAllByIdInAndIsDeletedFalse(ids);
        if (outstandingItems == null || outstandingItems.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("outstandingitem.notFound", ids));
        }
        if (outstandingItems.size() != ids.size()) {
            List<Long> notFoundIds = ids.stream().filter(i -> outstandingItems.stream().noneMatch(e -> e.getId().equals(i))).toList();
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("outstandingitem.notFound", notFoundIds));
        }

        boolean invalidateStatus = outstandingItems.stream().anyMatch(item -> OutstandingStatusEnum.CLOSED.name().equals(item.getStatus()) || OutstandingStatusEnum.DONE.name().equals(item.getStatus()));
        if (invalidateStatus) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("outstandingitem.status.invalid"));
        }

        outstandingItems.forEach(item -> {
            item.setIsDeleted(true);
            item.setLastUpdateAt(LocalDateTime.now());
            item.setLastUpdateBy(getCurrentUser() != null ? getCurrentUser().getId() : Constants.DEFAULT_USER_ID);
        });
        outstandingItemRepository.saveAll(outstandingItems);

        List<OutstandingAlertConfig> outstandingAlertConfigs = outstandingAlertConfigRepository.findAllByOutstandingIdInAndIsDeletedFalse(ids);
        if (outstandingAlertConfigs != null) {
            outstandingAlertConfigs.forEach(config -> {
                config.setIsDeleted(true);
                config.setUpdatedBy(getCurrentUser() != null ? getCurrentUser().getId() : Constants.DEFAULT_USER_ID);
                config.setUpdatedAt(LocalDateTime.now());
            });
            outstandingAlertConfigRepository.saveAll(outstandingAlertConfigs);
        }

        List<Attachment> attachments = new ArrayList<>();

        List<OutstandingProcessLog> processLogs = processLogRepository.findAllByOutstandingIdInAndIsDeletedFalse(ids);
        if (processLogs != null && !processLogs.isEmpty()) {
            processLogs.forEach(log -> {
                log.setIsDeleted(true);
                log.setUpdatedBy(getCurrentUser() != null ? getCurrentUser().getId() : Constants.DEFAULT_USER_ID);
                log.setUpdatedAt(LocalDateTime.now());
            });
            processLogRepository.saveAll(processLogs);

            attachments.addAll(attachmentRepository.findAllByReferenceIdInAndReferenceTypeAndIsDeletedFalse(ids, Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE));
        }

        List<OutstandingAcceptance> acceptances = acceptanceRepository.findAllByOutstandingIdInAndIsDeletedFalse(ids);
        if (acceptances != null && !acceptances.isEmpty()) {
            acceptances.forEach(acceptance -> {
                acceptance.setIsDeleted(true);
                acceptance.setUpdatedBy(getCurrentUser() != null ? getCurrentUser().getId() : Constants.DEFAULT_USER_ID);
                acceptance.setUpdatedAt(LocalDateTime.now());
            });
            acceptanceRepository.saveAll(acceptances);
            attachments.addAll(attachmentRepository.findAllByReferenceIdInAndReferenceTypeAndIsDeletedFalse(acceptances.stream().map(OutstandingAcceptance::getId).collect(Collectors.toList()), Constants.OUTSTANDING_ACCEPTANCE_REFERENCE_TYPE));
        }

        attachments.addAll(attachmentRepository.findAllByReferenceIdInAndReferenceTypeAndIsDeletedFalse(ids, Constants.OUTSTANDING_REFERENCE_TYPE));
        if (!attachments.isEmpty()) {
            attachments.forEach(attachment -> {
                attachment.setIsDeleted(true);
                attachment.setUpdatedBy(getCurrentUser() != null ? getCurrentUser().getId() : Constants.DEFAULT_USER_ID);
                attachment.setUpdatedAt(LocalDateTime.now());
            });
            attachmentRepository.saveAll(attachments);
        }
    }

    @Override
    public OutstandingItemDto getOutstandingItemById(Long id) {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("outstandingitem.id.null"));
        }
        OutstandingItem outstandingItem = outstandingItemRepository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (outstandingItem == null) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("outstandingitem.notFound", id));
        }
        return outstandingItemMapper.mapToOutstandingItemDto(List.of(outstandingItem)).getFirst();
    }

    List<OutstandingItemDto> getOutstanding() {
        return List.of();
    }
}
