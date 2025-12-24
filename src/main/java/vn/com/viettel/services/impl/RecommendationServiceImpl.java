package vn.com.viettel.services.impl;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.RecommendationMapper;
import vn.com.viettel.minio.dto.ObjectFileDTO;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.AttachmentService;
import vn.com.viettel.services.RecommendationService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.viettel.repositories.jpa.RecommendationSpecifications.buildSpecification;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Value("${minio.bucketName}")
    private String bucketName;

    @Autowired
    private RecommendationMapper recommendationMapper;
    @Autowired
    private RecommendationRepository recommendationRepository;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private ProjectItemRepository projectItemRepo;
    @Autowired
    private CatProjectPhaseRepository phaseRepo;
    @Autowired
    private WorkItemRepository workItemRepo;
    @Autowired
    private CatRecommendationTypeRepository recommendationTypeRepo;
    @Autowired
    private SysUserRepository userRepository;
    @Autowired
    private Translator translator;
    @Autowired
    private RecommendationWorkItemRepository recommendationWorkItemRepository;
    @Autowired
    private RecommendationAssignmentRepository recommendationAssignmentRepository;
    @Autowired
    private RecommendationResponseRepository recommendationResponseRepository;
    @Autowired
    private AttachmentService attachmentService;

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "deadline", "deadline",
            "recommendationTitle", "recommendationTitle",
            "priority", "priority",
            "status", "status",
            "createdByUser", "createdByUser.fullName",
            "recommendationType", "catRecommendationType.typeName"

    );

    @Override
    @Transactional
    public RecommendationDto createRecommendation(RecommendationDto dto, MultipartFile[] files) throws CustomException {
        SysUser currentUser = getCurrentUser();
        validate(dto, false);
        Recommendation entity = recommendationMapper.toEntity(dto, currentUser);
        recommendationRepository.save(entity);
        if (dto.getWorkItems() != null && !dto.getWorkItems().isEmpty()) {
            List<RecommendationWorkItem> recommendationWorkItemList = recommendationMapper.mapToRecommendationWorkItem(dto.getWorkItems(), entity.getId(), currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
            recommendationWorkItemRepository.saveAll(recommendationWorkItemList);
        }

        if (dto.getAssignedUsers() != null && !dto.getAssignedUsers().isEmpty()) {
            List<RecommendationAssignment> recommendationAssignmentList = recommendationMapper.mapToRecommendationAssignment(dto.getAssignedUsers(), entity.getId());
            recommendationAssignmentRepository.saveAll(recommendationAssignmentList);
        }

        attachmentService.handleAttachment(files, entity.getId(), Constants.RECOMMENDATION_REFERENCE_TYPE, Constants.RECOMMENDATION_REFERENCE_TYPE);

        return recommendationMapper.mapToRecommendationWorkItem(List.of(entity)).getFirst();
    }


    @Transactional
    @Override
    public RecommendationDto updateRecommendation(Long id, RecommendationDto dto, MultipartFile[] files) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.id.null"));
        }
        dto.setId(id);
        Recommendation entity = recommendationRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", id)));

        SysUser currentUser = getCurrentUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;

        validate(dto, true);

        // 4. Cập nhật thông tin cơ bản của Entity
        recommendationMapper.updateEntityFromDto(dto, entity, currentUserId);

        recommendationRepository.save(entity);

        // 5. Cập nhật Work Items (Xóa cũ - Thêm mới)
        updateWorkItem(dto, id, currentUserId);

        // 6. Xử lý Assignment
        updateAssigment(dto, id, currentUserId);

        // 7. Xử lý file đính kèm mới (nếu có)
        attachmentService.handleAttachment(files, entity.getId(), Constants.RECOMMENDATION_REFERENCE_TYPE, Constants.RECOMMENDATION_REFERENCE_TYPE);

        // 8. Xử lý file đính kèm đã xóa (nếu có)
        if (dto.getDeletedAttachments() != null && !dto.getDeletedAttachments().isEmpty()) {
            attachmentService.deleteAttachmentsById(dto.getDeletedAttachments().stream().map(AttachmentDto::getId).collect(Collectors.toList()), currentUserId);
        }

        return recommendationMapper.mapToRecommendationWorkItem(List.of(entity)).getFirst();
    }

    private void updateWorkItem(RecommendationDto dto, Long id, Long currentUserId) {
        List<RecommendationWorkItem> currentWorkItems = recommendationWorkItemRepository.findAllByRecommendationIdAndIsDeletedFalse(id);
        Set<Long> newItemIds = new HashSet<>();
        if (dto.getWorkItems() != null) {
            dto.getWorkItems().forEach(wi -> newItemIds.add(wi.getId()));
        }

        // Xử lý Xóa: Những cái đang có ở DB nhưng không có trong DTO gửi lên
        List<RecommendationWorkItem> toDelete = currentWorkItems.stream()
                .filter(wi -> !newItemIds.contains(wi.getWorkItemId()))
                .peek(wi -> {
                    wi.setIsDeleted(true);
                    wi.setUpdatedBy(currentUserId);
                    wi.setUpdatedAt(LocalDateTime.now());
                })
                .toList();
        if (!toDelete.isEmpty()) {
            recommendationWorkItemRepository.saveAll(toDelete);
        }

        // Xử lý Thêm mới: Những ID trong DTO mà chưa có trong DB
        Set<Long> existingWorkItemIds = currentWorkItems.stream()
                .map(RecommendationWorkItem::getWorkItemId)
                .collect(Collectors.toSet());

        if (dto.getWorkItems() != null) {
            List<WorkItemDto> newWorkItemsDto = dto.getWorkItems().stream()
                    .filter(wi -> !existingWorkItemIds.contains(wi.getId()))
                    .toList();

            if (!newWorkItemsDto.isEmpty()) {
                List<RecommendationWorkItem> newEntities = recommendationMapper.mapToRecommendationWorkItem(
                        newWorkItemsDto,
                        id,
                        currentUserId
                );
                recommendationWorkItemRepository.saveAll(newEntities);
            }
        }
    }

    private void updateAssigment(RecommendationDto dto, Long id, Long currentUserId) {
        List<RecommendationAssignment> currentAssignments = recommendationAssignmentRepository.findAllByRecommendationIdAndIsDeletedFalse(id);
        Set<Long> newItemIds = new HashSet<>();
        if (dto.getAssignedUsers() != null) {
            dto.getAssignedUsers().forEach(wi -> newItemIds.add(wi.getId()));
        }

        // Xử lý Xóa: Những cái đang có ở DB nhưng không có trong DTO gửi lên
        List<RecommendationAssignment> toDelete = currentAssignments.stream()
                .filter(wi -> !newItemIds.contains(wi.getUserId()))
                .peek(wi -> {
                    wi.setIsDeleted(true);
                    wi.setUpdatedBy(currentUserId);
                    wi.setUpdatedAt(LocalDateTime.now());
                })
                .toList();
        if (!toDelete.isEmpty()) {
            recommendationAssignmentRepository.saveAll(toDelete);
        }

        // Xử lý Thêm mới: Những ID trong DTO mà chưa có trong DB
        Set<Long> existingWorkItemIds = currentAssignments.stream()
                .map(RecommendationAssignment::getUserId)
                .collect(Collectors.toSet());

        if (dto.getAssignedUsers() != null) {
            List<UserDto> newWorkItemsDto = dto.getAssignedUsers().stream()
                    .filter(wi -> !existingWorkItemIds.contains(wi.getId()))
                    .toList();

            if (!newWorkItemsDto.isEmpty()) {
                List<RecommendationAssignment> newEntities = recommendationMapper.mapToRecommendationAssignment(
                        newWorkItemsDto,
                        id
                );
                recommendationAssignmentRepository.saveAll(newEntities);
            }
        }
    }

    @Transactional
    @Override
    public void deleteRecommendations(List<Long> id) throws CustomException {
        if (id == null || id.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.id.null"));
        }
        List<Recommendation> entities = recommendationRepository.findAllByIdInAndIsDeletedFalse(id);
        if (entities == null || entities.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", id));
        }
        if (entities.size() != id.size()) {
            List<Long> notFoundIds = id.stream().filter(i -> entities.stream().noneMatch(e -> e.getId().equals(i))).toList();
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", notFoundIds));
        }
        SysUser currentUser = getCurrentUser();
        entities.forEach(entity -> {
            entity.setIsDeleted(true);
            entity.setDeletedAt(LocalDateTime.now());
            entity.setDeletedById(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
        });
        recommendationRepository.saveAll(entities);

        List<RecommendationWorkItem> recommendationWorkItemList = recommendationWorkItemRepository.findAllByRecommendationIdInAndIsDeletedFalse(id);
        if (recommendationWorkItemList != null && !recommendationWorkItemList.isEmpty()) {
            recommendationWorkItemList.forEach(wi -> {
                wi.setIsDeleted(true);
                wi.setUpdatedBy(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
                wi.setUpdatedAt(LocalDateTime.now());
            });
            recommendationWorkItemRepository.saveAll(recommendationWorkItemList);
        }

        // Delete attachment
        attachmentService.deleteAttachments(id, Constants.RECOMMENDATION_REFERENCE_TYPE, currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);

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

    @NotNull
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

    @Override
    public Page<RecommendationDto> searchRecommendations(RecommendationSearchRequestDto request) throws CustomException {
        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        // --- Validate & chuẩn hóa sortBy ---
        String sortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = ALLOWED_SORT_FIELDS.get(sortBy);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    "sortBy không hợp lệ. Các giá trị được phép: " + ALLOWED_SORT_FIELDS.keySet()
            );
        }
//        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
//            // Có thể dùng message bundle nếu muốn i18n
//            throw new CustomException(
//                    HttpStatus.BAD_REQUEST.value(),
//                    "sortBy không hợp lệ. Các giá trị được phép: " + ALLOWED_SORT_FIELDS
//            );
//        }

        // --- Validate & chuẩn hóa sortDirection ---
        String sortDirectionRaw = StringUtils.defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    "sortDirection không hợp lệ. Chỉ chấp nhận ASC hoặc DESC"
            );
        }
        // Build specification theo điều kiện
        var specification = buildSpecification(request);

        PageRequest pageRequest;

        // 2. Nếu sort theo status/priority/acceptanceType -> dùng CASE WHEN trong Specification
        if ("status".equalsIgnoreCase(sortBy)
                || "priority".equalsIgnoreCase(sortBy)
                || "acceptanceType".equalsIgnoreCase(sortBy)) {
            specification = specification.and(
                    RecommendationSpecifications.withCustomSort(sortBy, direction)
            );
            // Pageable KHÔNG cần sort, chỉ phân trang
            pageRequest = PageRequest.of(page, size);
        } else {
            // 3. Các field khác dùng Sort bình thường (dùng sortProperty đã map)
            Sort sort = Sort.by(direction, sortProperty);
            pageRequest = PageRequest.of(page, size, sort);
        }

        Page<Recommendation> resultPage = recommendationRepository.findAll(specification, pageRequest);

        // Map sang DTO (tái sử dụng mapper hiện tại để enrich project, user, workItems...)
        List<RecommendationDto> dtoList = recommendationMapper.mapToRecommendationWorkItem(resultPage.getContent());

        return new PageImpl<>(dtoList, pageRequest, resultPage.getTotalElements());
    }

    @Override
    public RecommendationDto getRecommendationById(Long id) {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.id.null"));
        }
        Recommendation recommendation = recommendationRepository.findById(id).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", id)));
        return recommendationMapper.mapToRecommendationWorkItem(List.of(recommendation)).getFirst();
    }

    @Override
    public RecommendationDto closeRecommendation(Long id) {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.id.null"));
        }
        Recommendation recommendation = recommendationRepository.findById(id).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", id)));
        if (RecommendationStatusEnum.DONE.name().equals(recommendation.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.already_done"));
        }
        SysUser currentUser = getCurrentUser();
        recommendation.setClosedById(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
        recommendation.setClosedAt(LocalDateTime.now());
        recommendation.setStatus(RecommendationStatusEnum.DONE.name());
        recommendationRepository.save(recommendation);
        return recommendationMapper.mapToRecommendationWorkItem(List.of(recommendation)).getFirst();
    }

    @Override
    public RecommendationResponseDto addRecommendationResponse(Long recommendationId, RecommendationResponseDto dto, MultipartFile[] files) {
        if (recommendationId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.id.null"));
        }
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.payload.null"));
        }
        Recommendation recommendation = recommendationRepository.findById(recommendationId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", recommendationId)));
        if (RecommendationStatusEnum.DONE.name().equals(recommendation.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.already_done"));
        }
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.payload.null"));
        }
        if (StringUtils.isBlank(dto.getResponseContent())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.response.content.required"));
        }
        if (dto.getResponseContent().trim().length() > 500) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.response.content.length"));
        }
        SysUser currentUser = getCurrentUser();
        RecommendationResponse recommendationResponse = recommendationMapper.mapToRecommendationResponse(dto, recommendationId, currentUser);
        recommendationResponseRepository.save(recommendationResponse);

        String channel = Constants.RECOMMENDATION_REFERENCE_TYPE + "/" + recommendationResponse.getId() + "/" + Constants.RECOMMENDATION_RESPONSE_REFERENCE_TYPE;
        List<Attachment> attachments = attachmentService.handleAttachment(files, recommendationResponse.getId(), Constants.RECOMMENDATION_RESPONSE_REFERENCE_TYPE, channel);


        return recommendationMapper.mapToRecommendationResponseDto(recommendationResponse, currentUser, attachments);
    }

    @Override
    public List<RecommendationResponseDto> getRecommendationResponses(Long recommendationId) {
        if (recommendationId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.id.null"));
        }
        recommendationRepository.findById(recommendationId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", recommendationId)));
        return recommendationMapper.mapToRecommendationResponseDto(recommendationResponseRepository.findAllByRecommendationIdAndIsDeletedFalse(recommendationId));
    }

    protected void validate(RecommendationDto dto, boolean isUpdate) throws CustomException {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.payload.null"));
        }
        if (StringUtils.isBlank(dto.getRecommendationTitle())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.title.required"));
        } else if (dto.getRecommendationTitle().length() > 250) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.title.length"));
        } else {
            Optional<Recommendation> recommendation = recommendationRepository.findByRecommendationTitle(dto.getRecommendationTitle());
            if (recommendation.isPresent()) {
                if (isUpdate && !recommendation.get().getId().equals(dto.getId())) {
                    throw new CustomException(HttpStatus.CONFLICT.value(), translator.getMessage("recommendation.title.duplicate", dto.getRecommendationTitle()));
                }
                if (!isUpdate) {
                    throw new CustomException(HttpStatus.CONFLICT.value(), translator.getMessage("recommendation.title.duplicate", dto.getRecommendationTitle()));
                }
            }
        }

        if (StringUtils.isBlank(dto.getContent())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.content.required"));
        } else if (dto.getContent().length() > 500) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.content.length"));
        }
        Long recTypeId = dto.getRecommendationType() == null ? null : dto.getRecommendationType().getId();
        if (recTypeId == null || !recommendationTypeRepo.existsById(recTypeId)) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.type.notfound"));
        }
        if (dto.getPriority() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.priority.required"));
        } else {
            try {
                // Nếu muốn case-insensitive thì có thể dùng toUpperCase() trước
                PriorityEnum.valueOf(dto.getPriority().getCode().toUpperCase());
            } catch (IllegalArgumentException ex) {
                // Không khớp với bất kỳ giá trị nào trong PriorityEnum
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.priority.invalid"));
            }
        }

        Long projectId = dto.getProject() == null ? null : dto.getProject().getId();
        if (projectId != null) {
            if (!projectRepo.existsById(projectId)) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.project.notfound", projectId));
            }

            Long itemId = dto.getProjectItem() == null ? null : dto.getProjectItem().getId();
            if (itemId == null) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.item.required"));
            }

            ProjectItem item = projectItemRepo.findById(itemId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.item.notfound", itemId)));
            if (!Objects.equals(item.getProjectId(), projectId)) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.item.not_belong_to_project", itemId, projectId));
            }

            Long phaseId = dto.getPhase() == null ? null : dto.getPhase().getId();
            if (phaseId != null) {
                CatProjectPhase phase = phaseRepo.findById(phaseId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.phase.notfound", phaseId)));
                if (!Objects.equals(phase.getProjectId(), projectId)) {
                    throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.phase.not_belong_to_project", phaseId, projectId));
                }
            }

            if (dto.getWorkItems() != null) {
                for (WorkItemDto wiDto : dto.getWorkItems()) {
                    Long wiId = wiDto == null ? null : wiDto.getId();
                    if (wiId == null) {
                        throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.workitem.id_required"));
                    }
                    WorkItem wi = workItemRepo.findById(wiId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.workitem.notfound", wiId)));
                    if (!Objects.equals(wi.getItemId(), itemId)) {
                        throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.workitem.not_belong_to_item", wiId, itemId));
                    }
                }
            }
        }

    }
}
