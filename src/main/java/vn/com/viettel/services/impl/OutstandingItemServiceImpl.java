package vn.com.viettel.services.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.OutstandingItemDto;
import vn.com.viettel.dto.OutstandingStatusEnum;
import vn.com.viettel.dto.PriorityEnum;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.OutstandingItemMapper;
import vn.com.viettel.minio.dto.ObjectFileDTO;
import vn.com.viettel.minio.services.FileService;
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
@Log4j2
public class OutstandingItemServiceImpl implements OutstandingItemService {
    @Value("${minio.bucketName}")
    private String bucketName;
    private static final Map<String, String> SORT_FIELD_MAPPING = Map.ofEntries(
            Map.entry("createdAt", "createdAt"),
            Map.entry("deadline", "deadline"),
            Map.entry("outstandingTitle", "outstandingTitle"),
            Map.entry("priority", "priority"),
            Map.entry("status", "status"),
            Map.entry("acceptanceType", "acceptanceType"),
            Map.entry("projectName", "project.projectName"),
            Map.entry("phaseName", "phase.phaseName"),
            Map.entry("itemName", "projectItem.itemName"),
            Map.entry("workItemName", "workItem.workItemName"),
            Map.entry("createdByUser", "createdByUser.fullName"),
            Map.entry("assignedUser", "assignedUser.fullName"),
            Map.entry("assignedOrg", "assignedOrg.orgName")
    );

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
    private FileService fileService;

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
            for (OutstandingAlertConfigDto config : dto.getOutstandingAlertConfigs()) {
                if (config.getLevelNo() == null) {
                    throw new CustomException(
                            HttpStatus.BAD_REQUEST.value(),
                            translator.getMessage("outstanding.alert.level.no.null")
                    );
                }
                if (config.getAlertDate() == null) {
                    throw new CustomException(
                            HttpStatus.BAD_REQUEST.value(),
                            translator.getMessage("outstanding.alert.date.null", config.getLevelNo())
                    );
                }
            }
            boolean invalidate = dto.getOutstandingAlertConfigs().stream().anyMatch(config -> config.getAlertDate() == null || config.getAlertDate().isBefore(deadline));
            if (invalidate) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("outstanding.alert.date.invalid"));
            }
            // Validate: level cao hơn thì alertDate phải lớn hơn level thấp hơn
            // Sắp xếp theo levelNo rồi so sánh lần lượt
            List<OutstandingAlertConfigDto> sortedConfigs = dto.getOutstandingAlertConfigs().stream()
                    .sorted(Comparator.comparing(OutstandingAlertConfigDto::getLevelNo))
                    .toList();

            for (int i = 0; i < sortedConfigs.size() - 1; i++) {
                OutstandingAlertConfigDto lowerLevel = sortedConfigs.get(i);
                OutstandingAlertConfigDto higherLevel = sortedConfigs.get(i + 1);

                if (!higherLevel.getAlertDate().isAfter(lowerLevel.getAlertDate())) {
                    // Vi phạm: level cao hơn nhưng alertDate <= level thấp hơn
                    throw new CustomException(
                            HttpStatus.BAD_REQUEST.value(),
                            translator.getMessage("outstanding.alert.date.level.invalid",
                                    lowerLevel.getLevelNo(), higherLevel.getLevelNo())
                    );
                }
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
    @Override
    public OutstandingItemDto createOutstandingItem(OutstandingItemDto dto, MultipartFile[] acceptanceFiles, MultipartFile[] documents) {
        validate(dto, false);
        SysUser currentUser = getCurrentUser();
        OutstandingItem outstandingItem = outstandingItemMapper.toEntity(dto, currentUser);
        outstandingItem = outstandingItemRepository.save(outstandingItem);

        if (dto.getOutstandingAlertConfigs() != null && !dto.getOutstandingAlertConfigs().isEmpty()) {
            List<OutstandingAlertConfig> outstandingAlertConfigs = outstandingItemMapper.mapToOutstandingAlertConfig(dto.getOutstandingAlertConfigs(), outstandingItem.getId());
            outstandingAlertConfigRepository.saveAll(outstandingAlertConfigs);
        }

        List<Attachment> attachments = new ArrayList<>();
        // Xử lý file nghiệm thu
        if (acceptanceFiles != null && acceptanceFiles.length > 0) {
            String channel = Constants.OUTSTANDING_REFERENCE_TYPE + "/" + outstandingItem.getId() + "/" + Constants.OUTSTANDING_DOCUMENT_REFERENCE_TYPE_ACCEPTANCE;
            List<ObjectFileDTO> objectFileDTOList = fileService.uploadFiles(bucketName, channel, acceptanceFiles);

            for (ObjectFileDTO file : objectFileDTOList) {
                // Lưu thông tin file vào bảng Attachment (tùy thuộc vào cấu trúc entity của bạn)
                Attachment attachment = getAttachment(file, outstandingItem.getId(), Constants.OUTSTANDING_DOCUMENT_REFERENCE_TYPE_ACCEPTANCE, currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
                attachments.add(attachment);
            }
        }
        // Xử lý file tài liệu
        if (documents != null && documents.length > 0) {
            String channel = Constants.OUTSTANDING_REFERENCE_TYPE + "/" + outstandingItem.getId() + "/" + Constants.OUTSTANDING_DOCUMENT_REFERENCE_TYPE_DOCUMENT;
            List<ObjectFileDTO> objectFileDTOList = fileService.uploadFiles(bucketName, channel, documents);

            for (ObjectFileDTO file : objectFileDTOList) {
                // Lưu thông tin file vào bảng Attachment (tùy thuộc vào cấu trúc entity của bạn)
                Attachment attachment = getAttachment(file, outstandingItem.getId(), Constants.OUTSTANDING_DOCUMENT_REFERENCE_TYPE_DOCUMENT, currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
                attachments.add(attachment);
            }
        }
        if (!attachments.isEmpty()) {
            attachmentRepository.saveAll(attachments);
        }

        return outstandingItemMapper.mapToOutstandingItemDto(List.of(outstandingItem)).getFirst();
    }

    @Transactional
    @Override
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
    @Override
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

    @Transactional(readOnly = true)
    @Override
    public Page<OutstandingItemDto> searchOutstandingByRecommendation(RecommendationSearchRequestDto request) {
        if (request == null) {
            request = new RecommendationSearchRequestDto();
        }

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        String requestSortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = SORT_FIELD_MAPPING.get(requestSortBy);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    "sortBy không hợp lệ. Các giá trị được phép: " + SORT_FIELD_MAPPING.keySet()
            );
        }

        String sortDirection = StringUtils.isNotBlank(request.getSortDirection()) ? request.getSortDirection() : "DESC";
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    "sortDirection không hợp lệ. Chỉ chấp nhận ASC hoặc DESC"
            );
        }

        Specification<OutstandingItem> specification =
                OutstandingItemSpecifications.fromRecommendationSearch(request);

        PageRequest pageRequest;

        // 2. Nếu sort theo status/priority -> dùng CASE WHEN trong Specification
        if ("status".equalsIgnoreCase(requestSortBy) || "priority".equalsIgnoreCase(requestSortBy) || "acceptanceType".equalsIgnoreCase(requestSortBy)) {
            specification = specification.and(
                    OutstandingItemSpecifications.withCustomSort(requestSortBy, direction)
            );
            // Pageable KHÔNG cần sort, chỉ phân trang
            pageRequest = PageRequest.of(page, size);
        } else {
            // 3. Các field khác dùng Sort bình thường
            Sort sort = Sort.by(direction, requestSortBy);
            pageRequest = PageRequest.of(page, size, sort);
        }

        Page<OutstandingItem> entityPage = outstandingItemRepository.findAll(specification, pageRequest);

        List<OutstandingItemDto> dtoList =
                outstandingItemMapper.mapToOutstandingItemDto(entityPage.getContent());

        return new PageImpl<>(dtoList, pageRequest, entityPage.getTotalElements());
    }

    private Attachment getAttachment(ObjectFileDTO file, Long entityId, String referenceType, Long userId) {
        Attachment attachment = new Attachment();
        attachment.setFileName(file.getFileName());
        attachment.setFileSize(file.getFileSize());
        attachment.setFilePath(file.getFilePath());
        attachment.setFileUrl(file.getLinkUrlPublic()); // Đường dẫn/tên file trên MinIO
        attachment.setReferenceId(entityId);
        attachment.setReferenceType(referenceType);
        attachment.setUploadedAt(LocalDateTime.now());
        attachment.setUploadedBy(userId);
        attachment.setIsDeleted(false);
        return attachment;
    }
}
