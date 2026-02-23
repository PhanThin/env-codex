package vn.com.viettel.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.AttachmentMapper;
import vn.com.viettel.mapper.RecommendationMapper;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.RecommendationService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "deadline", "recommendationTitle", "priority", "status", "createdByUser", "recommendationType");

    private final RecommendationRepository recommendationRepository;
    private final RecommendationWorkItemRepository recommendationWorkItemRepository;
    private final RecommendationAssignmentRepository recommendationAssignmentRepository;
    private final RecommendationResponseRepository recommendationResponseRepository;
    private final CatRecommendationTypeRepository catRecommendationTypeRepository;
    private final ProjectRepository projectRepository;
    private final ProjectItemRepository projectItemRepository;
    private final CatProjectPhaseRepository catProjectPhaseRepository;
    private final WorkItemRepository workItemRepository;
    private final SysUserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentServiceImpl attachmentService;
    private final AttachmentMapper attachmentMapper;
    private final RecommendationMapper recommendationMapper;
    private final Translator translator;

    @Override
    @Transactional
    public RecommendationDto create(RecommendationDto dto, MultipartFile[] files) {
        SysUser currentUser = getCurrentUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        Long currentOrgId = currentUser != null ? currentUser.getOrgId() : null;

        validate(dto, false);
        Recommendation entity = recommendationMapper.toEntityForCreate(dto, currentUserId, currentOrgId);
        Recommendation saved = recommendationRepository.save(entity);

        syncWorkItems(saved.getId(), dto.getWorkItems(), currentUserId);
        createPrimaryAssignment(saved.getId(), dto.getCurrentProcessUser().getId(), currentOrgId, currentUserId);

        attachmentService.handleAttachment(files, saved.getId(), Constants.RECOMMENDATION_REFERENCE_TYPE, "recommendation");
        return getById(saved.getId());
    }

    @Override
    @Transactional
    public RecommendationDto update(Long id, RecommendationDto dto, MultipartFile[] files) {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.id.null"));
        }
        validate(dto, true);

        Recommendation existing = recommendationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", id)));

        if (!RecommendationStatusEnum.NEW.name().equals(existing.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.cannot_update", id, existing.getStatus()));
        }

        Long currentUserId = getCurrentUserIdOrDefault();
        Long oldHandler = existing.getCurrentProcessById();

        recommendationMapper.updateEntityFromDto(dto, existing, currentUserId);
        recommendationRepository.save(existing);

        syncWorkItems(existing.getId(), dto.getWorkItems(), currentUserId);

        Long newHandler = dto.getCurrentProcessUser() != null ? dto.getCurrentProcessUser().getId() : null;
        if (newHandler != null && !Objects.equals(oldHandler, newHandler)) {
            List<RecommendationAssignment> assignments = recommendationAssignmentRepository.findAllByRecommendationIdAndIsDeletedFalse(existing.getId());
            assignments.forEach(a -> {
                a.setIsDeleted(true);
                a.setUpdatedAt(LocalDateTime.now());
                a.setUpdatedBy(currentUserId);
            });
            recommendationAssignmentRepository.saveAll(assignments);
            createPrimaryAssignment(existing.getId(), newHandler, null, currentUserId);
        }

        attachmentService.handleAttachment(files, existing.getId(), Constants.RECOMMENDATION_REFERENCE_TYPE, "recommendation");
        if (dto.getDeletedAttachments() != null && !dto.getDeletedAttachments().isEmpty()) {
            attachmentService.deleteAttachmentsById(dto.getDeletedAttachments(), currentUserId);
        }
        return getById(existing.getId());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> existingIds = recommendationRepository.findExistingIds(ids);
        if (existingIds.size() != ids.size()) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", ids));
        }
        Long userId = getCurrentUserIdOrDefault();
        List<Recommendation> recommendations = recommendationRepository.findAllByIdInAndIsDeletedFalse(ids);
        recommendations.forEach(r -> {
            r.setIsDeleted(true);
            r.setDeletedAt(LocalDateTime.now());
            r.setDeletedBy(userId);
            r.setLastUpdateAt(LocalDateTime.now());
            r.setLastUpdateBy(userId);
        });
        recommendationRepository.saveAll(recommendations);

        List<RecommendationWorkItem> workItems = recommendationWorkItemRepository.findAllByRecommendationIdInAndIsDeletedFalse(ids);
        workItems.forEach(w -> {
            w.setIsDeleted(true);
            w.setUpdatedAt(LocalDateTime.now());
            w.setUpdatedBy(userId);
        });
        recommendationWorkItemRepository.saveAll(workItems);

        attachmentService.deleteAttachments(ids, Constants.RECOMMENDATION_REFERENCE_TYPE, userId);
    }

    @Override
    public RecommendationDto getById(Long id) {
        Recommendation recommendation = recommendationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", id)));
        return mapDetailDto(recommendation, true);
    }

    @Override
    public Page<RecommendationDto> search(RecommendationSearchRequestDto request) {
        String sortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.sortBy.invalid", ALLOWED_SORT_FIELDS));
        }
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int page = request.getPage() == null ? 0 : Math.max(request.getPage(), 0);
        int size = request.getSize() == null ? 20 : Math.max(request.getSize(), 1);

        Specification<Recommendation> spec = RecommendationSpecifications.buildSpecification(request);
        if ("status".equalsIgnoreCase(sortBy) || "priority".equalsIgnoreCase(sortBy)) {
            spec = spec.and(RecommendationSpecifications.withCustomSort(sortBy, direction));
        }

        Pageable pageable = PageRequest.of(page, size,
                ("status".equalsIgnoreCase(sortBy) || "priority".equalsIgnoreCase(sortBy))
                        ? Sort.unsorted()
                        : Sort.by(direction, mapSortBy(sortBy)));

        Page<Recommendation> result = recommendationRepository.findAll(spec, pageable);
        List<RecommendationDto> dtos = result.getContent().stream().map(r -> mapDetailDto(r, false)).toList();
        return new PageImpl<>(dtos, pageable, result.getTotalElements());
    }

    @Override
    @Transactional
    public RecommendationDto close(Long id) {
        Recommendation recommendation = findRecommendation(id);
        if (RecommendationStatusEnum.DONE.name().equals(recommendation.getStatus()) || RecommendationStatusEnum.CLOSED.name().equals(recommendation.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.already_done"));
        }
        Long userId = getCurrentUserIdOrDefault();
        recommendation.setStatus(RecommendationStatusEnum.CLOSED.name());
        recommendation.setClosedAt(LocalDateTime.now());
        recommendation.setClosedBy(userId);
        recommendation.setLastUpdateAt(LocalDateTime.now());
        recommendation.setLastUpdateBy(userId);
        recommendationRepository.save(recommendation);
        return mapDetailDto(recommendation, false);
    }

    @Override
    @Transactional
    public RecommendationDto accept(Long id) {
        return updateStatusFromNew(id, RecommendationStatusEnum.ACCEPTED.name());
    }

    @Override
    @Transactional
    public RecommendationDto reject(Long id) {
        return updateStatusFromNew(id, RecommendationStatusEnum.REJECTED.name());
    }

    @Override
    @Transactional
    public RecommendationResponseDto addResponse(Long id, RecommendationResponseDto dto, MultipartFile[] files) {
        Recommendation recommendation = findRecommendation(id);
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.response.null"));
        }
        if (RecommendationStatusEnum.DONE.name().equals(recommendation.getStatus()) || RecommendationStatusEnum.CLOSED.name().equals(recommendation.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.already_done"));
        }
        if (StringUtils.isBlank(dto.getResponseContent())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.response.content.required"));
        }
        if (dto.getResponseContent().trim().length() > 500) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.response.content.length"));
        }

        Long userId = getCurrentUserIdOrDefault();
        if (!Objects.equals(userId, recommendation.getCurrentProcessById())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.current_user.required"));
        }

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendationId(id);
        response.setRespondedBy(userId);
        response.setRespondedOrgId(getCurrentUser() != null ? getCurrentUser().getOrgId() : null);
        response.setRespondedAt(LocalDateTime.now());
        response.setResponseContent(dto.getResponseContent().trim());
        response.setIsDeleted(false);

        boolean isRedirect = Boolean.TRUE.equals(dto.getIsRedirect());
        response.setIsRedirect(isRedirect);
        if (isRedirect) {
            Long redirectUserId = dto.getRedirectToUser() != null ? dto.getRedirectToUser().getId() : null;
            if (redirectUserId == null || !userRepository.existsById(redirectUserId)) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.current_user.notfound", redirectUserId));
            }
            response.setRedirectTo(redirectUserId);
            recommendation.setStatus(RecommendationStatusEnum.IN_PROGRESS.name());
            recommendation.setCurrentProcessById(redirectUserId);
        } else {
            recommendation.setStatus(RecommendationStatusEnum.DONE.name());
        }
        recommendation.setLastUpdateBy(userId);
        recommendation.setLastUpdateAt(LocalDateTime.now());

        RecommendationResponse saved = recommendationResponseRepository.save(response);
        recommendationRepository.save(recommendation);
        attachmentService.handleAttachment(files, saved.getId(), Constants.RECOMMENDATION_RESPONSE_REFERENCE_TYPE, "recommendation-response");
        return mapResponseDto(saved);
    }

    @Override
    public List<RecommendationResponseDto> getResponses(Long id) {
        findRecommendation(id);
        return recommendationResponseRepository.findAllByRecommendationIdAndIsDeletedFalse(id)
                .stream().map(this::mapResponseDto).toList();
    }

    private RecommendationDto updateStatusFromNew(Long id, String newStatus) {
        Recommendation recommendation = findRecommendation(id);
        if (!RecommendationStatusEnum.NEW.name().equals(recommendation.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.cannot_update", id, recommendation.getStatus()));
        }
        recommendation.setStatus(newStatus);
        recommendation.setLastUpdateAt(LocalDateTime.now());
        recommendation.setLastUpdateBy(getCurrentUserIdOrDefault());
        recommendationRepository.save(recommendation);
        return mapDetailDto(recommendation, false);
    }

    private Recommendation findRecommendation(Long id) {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.id.null"));
        }
        return recommendationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("recommendation.notFound", id)));
    }

    private void validate(RecommendationDto dto, boolean isUpdate) {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.payload.null"));
        }
        if (StringUtils.isBlank(dto.getRecommendationTitle())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.title.required"));
        }
        if (dto.getRecommendationTitle().trim().length() > 250) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.title.length"));
        }
        String title = dto.getRecommendationTitle().trim();
        if (!isUpdate && recommendationRepository.existsByRecommendationTitleIgnoreCaseAndIsDeletedFalse(title)) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.title.duplicate", title));
        }

        if (StringUtils.isBlank(dto.getContent())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.content.required"));
        }
        if (dto.getContent().trim().length() > 500) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.content.length"));
        }

        if (dto.getRecommendationType() == null || dto.getRecommendationType().getId() == null || !catRecommendationTypeRepository.existsByIdAndIsDeletedFalse(dto.getRecommendationType().getId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.type.notfound"));
        }
        if (dto.getPriority() == null || StringUtils.isBlank(dto.getPriority().getCode())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.priority.required"));
        }
        try {
            PriorityEnum.valueOf(dto.getPriority().getCode().trim().toUpperCase());
        } catch (Exception ex) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.priority.invalid"));
        }

        if (dto.getProject() == null || dto.getProject().getId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.project.required"));
        }
        if (!projectRepository.existsByIdAndIsDeletedFalse(dto.getProject().getId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.project.notfound", dto.getProject().getId()));
        }

        if (dto.getProjectItem() != null && dto.getProjectItem().getId() != null) {
            ProjectItem item = projectItemRepository.findByIdAndIsDeletedFalse(dto.getProjectItem().getId())
                    .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.item.notfound", dto.getProjectItem().getId())));
            if (!Objects.equals(item.getProjectId(), dto.getProject().getId())) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.item.not_belong_to_project", item.getId(), dto.getProject().getId()));
            }
        }

        if (dto.getPhase() != null && dto.getPhase().getId() != null) {
            CatProjectPhase phase = catProjectPhaseRepository.findByIdAndIsDeletedFalse(dto.getPhase().getId())
                    .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.phase.notfound", dto.getPhase().getId())));
            if (!Objects.equals(phase.getProjectId(), dto.getProject().getId())) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.phase.not_belong_to_project", phase.getId(), dto.getProject().getId()));
            }
        }

        if (dto.getWorkItems() != null && !dto.getWorkItems().isEmpty()) {
            Long itemId = dto.getProjectItem() != null ? dto.getProjectItem().getId() : null;
            for (WorkItemDto workItem : dto.getWorkItems()) {
                if (workItem.getId() == null) {
                    throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.workitem.id_required"));
                }
                WorkItem w = workItemRepository.findByIdAndIsDeletedFalse(workItem.getId())
                        .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.workitem.notfound", workItem.getId())));
                if (itemId != null && !Objects.equals(w.getItemId(), itemId)) {
                    throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.workitem.not_belong_to_item", w.getId(), itemId));
                }
            }
        }

        if (dto.getDeadline() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.deadline.required"));
        }
        if (dto.getDeadline().isBefore(LocalDate.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.deadline.invalid"));
        }

        if (dto.getCurrentProcessUser() == null || dto.getCurrentProcessUser().getId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.current_user.required"));
        }
        if (!userRepository.existsById(dto.getCurrentProcessUser().getId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("recommendation.current_user.notfound", dto.getCurrentProcessUser().getId()));
        }
    }

    private void syncWorkItems(Long recommendationId, List<WorkItemDto> workItems, Long userId) {
        List<RecommendationWorkItem> current = recommendationWorkItemRepository.findAllByRecommendationIdAndIsDeletedFalse(recommendationId);
        Set<Long> newIds = workItems == null ? Collections.emptySet() : workItems.stream().map(WorkItemDto::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> currentIds = current.stream().map(RecommendationWorkItem::getWorkItemId).collect(Collectors.toSet());

        List<RecommendationWorkItem> toDelete = current.stream().filter(w -> !newIds.contains(w.getWorkItemId())).toList();
        toDelete.forEach(w -> {
            w.setIsDeleted(true);
            w.setUpdatedBy(userId);
            w.setUpdatedAt(LocalDateTime.now());
        });
        recommendationWorkItemRepository.saveAll(toDelete);

        List<RecommendationWorkItem> toAdd = newIds.stream().filter(id -> !currentIds.contains(id)).map(workItemId -> {
            RecommendationWorkItem row = new RecommendationWorkItem();
            row.setRecommendationId(recommendationId);
            row.setWorkItemId(workItemId);
            row.setCreatedAt(LocalDateTime.now());
            row.setCreatedBy(userId);
            row.setIsDeleted(false);
            return row;
        }).toList();
        recommendationWorkItemRepository.saveAll(toAdd);
    }

    private void createPrimaryAssignment(Long recommendationId, Long userId, Long orgId, Long currentUserId) {
        RecommendationAssignment assignment = new RecommendationAssignment();
        assignment.setRecommendationId(recommendationId);
        assignment.setUserId(userId);
        assignment.setOrg(orgId);
        assignment.setIsPrimary(true);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setIsDeleted(false);
        assignment.setUpdatedBy(currentUserId);
        assignment.setUpdatedAt(LocalDateTime.now());
        recommendationAssignmentRepository.save(assignment);
    }

    private RecommendationDto mapDetailDto(Recommendation recommendation, boolean includeAttachments) {
        RecommendationDto dto = recommendationMapper.toDto(recommendation);

        if (includeAttachments) {
            dto.setAttachments(attachmentMapper.mapToDtos(
                    attachmentRepository.findAllByReferenceIdAndReferenceTypeAndIsDeletedFalse(recommendation.getId(), Constants.RECOMMENDATION_REFERENCE_TYPE)
            ));
        }

        List<RecommendationWorkItem> links = recommendationWorkItemRepository.findAllByRecommendationIdAndIsDeletedFalse(recommendation.getId());
        Map<Long, WorkItem> workItemMap = workItemRepository.findAllById(links.stream().map(RecommendationWorkItem::getWorkItemId).toList())
                .stream().collect(Collectors.toMap(WorkItem::getId, Function.identity(), (a, b) -> a));
        dto.setWorkItems(links.stream().map(link -> {
            WorkItem w = workItemMap.get(link.getWorkItemId());
            WorkItemDto wDto = new WorkItemDto();
            wDto.setId(link.getWorkItemId());
            if (w != null) {
                wDto.setWorkItemCode(w.getWorkItemCode());
                wDto.setWorkItemName(w.getWorkItemName());
            }
            return wDto;
        }).toList());

        Set<Long> userIds = new HashSet<>();
        if (recommendation.getCreatedById() != null) userIds.add(recommendation.getCreatedById());
        if (recommendation.getLastUpdateBy() != null) userIds.add(recommendation.getLastUpdateBy());
        if (recommendation.getClosedBy() != null) userIds.add(recommendation.getClosedBy());
        if (recommendation.getCurrentProcessById() != null) userIds.add(recommendation.getCurrentProcessById());

        List<RecommendationAssignment> assignments = recommendationAssignmentRepository.findAllByRecommendationIdAndIsDeletedFalse(recommendation.getId());
        assignments.forEach(a -> userIds.add(a.getUserId()));

        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() : userRepository.findAllByIdInAndIsDeletedFalse(new ArrayList<>(userIds))
                .stream().collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));

        dto.setCreatedByUser(toUserDto(userMap.get(recommendation.getCreatedById())));
        dto.setLastUpdateByUser(toUserDto(userMap.get(recommendation.getLastUpdateBy())));
        dto.setClosedByUser(toUserDto(userMap.get(recommendation.getClosedBy())));
        dto.setCurrentProcessUser(toUserDto(userMap.get(recommendation.getCurrentProcessById())));
        dto.setAssignedUsers(assignments.stream().map(a -> toUserDto(userMap.get(a.getUserId()))).filter(Objects::nonNull).toList());

        return dto;
    }

    private RecommendationResponseDto mapResponseDto(RecommendationResponse entity) {
        RecommendationResponseDto dto = new RecommendationResponseDto();
        dto.setId(entity.getId());
        dto.setRecommendationId(entity.getRecommendationId());
        dto.setRespondedAt(entity.getRespondedAt());
        dto.setResponseContent(entity.getResponseContent());
        dto.setIsRedirect(entity.getIsRedirect());
        dto.setAttachments(attachmentMapper.mapToDtos(
                attachmentRepository.findAllByReferenceIdAndReferenceTypeAndIsDeletedFalse(entity.getId(), Constants.RECOMMENDATION_RESPONSE_REFERENCE_TYPE)
        ));
        SysUser responded = userRepository.findById(entity.getRespondedBy()).orElse(null);
        dto.setRespondedByUser(toUserDto(responded));
        if (entity.getRedirectTo() != null) {
            dto.setRedirectToUser(toUserDto(userRepository.findById(entity.getRedirectTo()).orElse(null)));
        }
        return dto;
    }

    private UserDto toUserDto(SysUser user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setOrgId(user.getOrgId());
        return dto;
    }

    private String mapSortBy(String sortBy) {
        if ("createdByUser".equals(sortBy)) {
            return "createdById";
        }
        if ("recommendationType".equals(sortBy)) {
            return "recommendationTypeId";
        }
        return sortBy;
    }

    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof SysUser user) {
                return user;
            }
            if (authentication.getPrincipal() instanceof String username) {
                return userRepository.findByUsername(username).orElse(null);
            }
        }
        return null;
    }

    private Long getCurrentUserIdOrDefault() {
        SysUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
    }
}
