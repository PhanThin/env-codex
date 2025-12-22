package vn.com.viettel.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.*;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.utils.Constants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RecommendationMapper {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private SysUserRepository sysUserRepo;
    @Autowired
    private CatRecommendationTypeRepository recommendationTypeRepo;
    @Autowired
    private CatProjectPhaseRepository phaseRepo;
    @Autowired
    private WorkItemRepository workItemRepo;
    @Autowired
    private ProjectItemRepository projectItemRepo;
    @Autowired
    private RecommendationWorkItemRepository recommendationWorkItemRepo;
    @Autowired
    private RecommendationAssignmentRepository recommendationAssignmentRepo;

    public List<RecommendationDto> mapToRecommendationWorkItem(List<Recommendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) return new ArrayList<>();
        List<Long> recommendationIds = recommendations.stream().map(Recommendation::getId).toList();
        List<Long> projectIds = recommendations.stream().map(Recommendation::getProjectId).distinct().filter(Objects::nonNull).toList();
        List<Long> itemIds = recommendations.stream().map(Recommendation::getItemId).distinct().filter(Objects::nonNull).toList();
        List<Long> phaseIds = recommendations.stream().map(Recommendation::getPhaseId).distinct().filter(Objects::nonNull).toList();
        List<Long> recommendationTypeIds = recommendations.stream().map(Recommendation::getRecommendationTypeId).distinct().filter(Objects::nonNull).toList();
//        List<Long> createdByIds = recommendations.stream().map(Recommendation::getCreatedById).distinct().filter(Objects::nonNull).toList();
//        List<Long> closedByIds = recommendations.stream().map(Recommendation::getClosedById).distinct().filter(Objects::nonNull).toList();
//        List<Long> lastUpdateBys = recommendations.stream().map(Recommendation::getLastUpdateBy).distinct().filter(Objects::nonNull).toList();
//        List<Long> userIds = Stream.concat(createdByIds.stream(), Stream.concat(closedByIds.stream(), lastUpdateBys.stream())).distinct().toList();

        Map<Long, Project> projectMap = !projectIds.isEmpty() ? projectRepo.findAllByIdInAndIsDeletedFalse(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity())) : null;

        Map<Long, CatRecommendationType> recommendationTypeMap = !recommendationTypeIds.isEmpty() ? recommendationTypeRepo.findAllByIdInAndIsDeletedFalse(recommendationTypeIds).stream()
                .collect(Collectors.toMap(CatRecommendationType::getId, Function.identity())) : null;

        Map<Long, CatProjectPhase> phaseMap = !phaseIds.isEmpty() ? phaseRepo.findAllByIdInAndIsDeletedFalse(phaseIds).stream().collect(Collectors.toMap(CatProjectPhase::getId, Function.identity())) : null;

        Map<Long, WorkItem> workItemMap = !recommendationIds.isEmpty() ? workItemRepo.findAllByRecommendationIdInAndIsDeletedFalse(recommendationIds).stream().collect(Collectors.toMap(WorkItem::getId, Function.identity())) : null;


        Map<Long, List<RecommendationWorkItem>> recommendationWorkItemMap = !recommendationIds.isEmpty() ? recommendationWorkItemRepo.findAllByRecommendationIdInAndIsDeletedFalse(recommendationIds).stream().collect(Collectors.groupingBy(RecommendationWorkItem::getRecommendationId)) : null;

        Map<Long, ProjectItem> projectItemMap = !itemIds.isEmpty() ? projectItemRepo.findAllByIdInAndIsDeletedFalse(itemIds).stream().collect(Collectors.toMap(ProjectItem::getId, Function.identity())) : null;


        Map<Long, SysUser> sysUserMap = sysUserRepo.findAllByIsDeletedFalse().stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));
                //!userIds.isEmpty() ? sysUserRepo.findAllByIdInAndIsDeletedFalse(userIds).stream().collect(Collectors.toMap(SysUser::getId, Function.identity())) : null;

        Map<Long, List<RecommendationAssignment>> recommendationAssignmentMap = !recommendationIds.isEmpty() ? recommendationAssignmentRepo.findAllByRecommendationIdInAndIsDeletedFalse(recommendationIds).stream().collect(Collectors.groupingBy(RecommendationAssignment::getRecommendationId)) : null;

        return recommendations.stream()
                .map(recommendation -> toDto(recommendation, projectMap, recommendationTypeMap, phaseMap, workItemMap, recommendationWorkItemMap, projectItemMap, sysUserMap, recommendationAssignmentMap))
                .toList();
    }

    public RecommendationDto toDto(Recommendation recommendation, Map<Long, Project> projectMap,
                                   Map<Long, CatRecommendationType> recommendationTypeMap, Map<Long, CatProjectPhase> phaseMap,
                                   Map<Long, WorkItem> workItemMap, Map<Long, List<RecommendationWorkItem>> recommendationWorkItemMap,
                                   Map<Long, ProjectItem> projectItemMap, Map<Long, SysUser> sysUserMap, Map<Long, List<RecommendationAssignment>> recommendationAssignmentMap) {
        if (recommendation == null) return null;

        RecommendationDto dto = modelMapper.map(recommendation, RecommendationDto.class);
        if (projectMap != null && recommendation.getProjectId() != null && projectMap.containsKey(recommendation.getProjectId())) {
            dto.setProject(modelMapper.map(projectMap.get(recommendation.getProjectId()), ProjectDto.class));
        }
        if (recommendationTypeMap != null && recommendation.getRecommendationTypeId() != null && recommendationTypeMap.containsKey(recommendation.getRecommendationTypeId())) {
            dto.setRecommendationType(modelMapper.map(recommendationTypeMap.get(recommendation.getRecommendationTypeId()), CatRecommendationTypeDto.class));
        }
        if (phaseMap != null && recommendation.getPhaseId() != null && phaseMap.containsKey(recommendation.getPhaseId())) {
            dto.setPhase(modelMapper.map(phaseMap.get(recommendation.getPhaseId()), CatProjectPhaseDto.class));
        }
        if (recommendationWorkItemMap != null && workItemMap != null) {
            List<RecommendationWorkItem> recWorkItems = recommendationWorkItemMap.get(recommendation.getId());
            if (recWorkItems != null && !recWorkItems.isEmpty()) {
                // Lấy WorkItem từ map
                List<WorkItemDto> workItemDtos = recWorkItems.stream()
                        .map(rwi -> workItemMap.get(rwi.getWorkItemId()))
                        .filter(Objects::nonNull)
                        .map(wi -> modelMapper.map(wi, WorkItemDto.class))
                        .toList();
                dto.setWorkItems(workItemDtos);
            }

        }
        if (recommendationAssignmentMap != null && !recommendationAssignmentMap.isEmpty()) {
            List<RecommendationAssignment> assignments = recommendationAssignmentMap.get(recommendation.getId());
            if (assignments != null) {
                List<UserDto> assignedUsers = assignments.stream()
                        .map(ra -> sysUserMap.get(ra.getUserId()))
                        .filter(Objects::nonNull)
                        .map(sysUser -> modelMapper.map(sysUser, UserDto.class))
                        .toList();
                dto.setAssignedUsers(assignedUsers);
            }
        }
        if (projectItemMap != null && recommendation.getItemId() != null && projectItemMap.containsKey(recommendation.getItemId())) {
            dto.setItem(modelMapper.map(projectItemMap.get(recommendation.getItemId()), ProjectItemDto.class));
        }
        if (sysUserMap != null) {
            if (recommendation.getCreatedById() != null && sysUserMap.containsKey(recommendation.getCreatedById())) {
                dto.setCreatedByUser(modelMapper.map(sysUserMap.get(recommendation.getCreatedById()), UserDto.class));
            }
            if (recommendation.getClosedById() != null && sysUserMap.containsKey(recommendation.getClosedById())) {
                dto.setClosedByUser(modelMapper.map(sysUserMap.get(recommendation.getClosedById()), UserDto.class));
            }
            if (recommendation.getLastUpdateBy() != null && sysUserMap.containsKey(recommendation.getLastUpdateBy())) {
                dto.setLastUpdateByUser(modelMapper.map(sysUserMap.get(recommendation.getLastUpdateBy()), UserDto.class));
            }
        }
        return dto;
    }

    public Recommendation toEntity(RecommendationDto dto, SysUser user) {
        if (dto == null) return null;

        Recommendation entity = modelMapper.map(dto, Recommendation.class);
        setSelectFields(dto, entity);

        entity.setIsDeleted(false); // Default 'N' như entity
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedById(user != null ? user.getId() : Constants.DEFAULT_USER_ID);
        entity.setCreatedOrgId(user != null ? user.getOrgId() : Constants.DEFAULT_USER_ID);
        entity.setStatus(RecommendationStatusEnum.UN_PROCESS.getValue());
        return entity;
    }

    private void setSelectFields(RecommendationDto dto, Recommendation entity) {
        if (dto.getProject() != null) {
            entity.setProjectId(dto.getProject().getId());
        } else {
            entity.setProjectId(null);
        }

        if (dto.getItem() != null) {
            entity.setItemId(dto.getItem().getId());
        } else {
            entity.setItemId(null);
        }

        if (dto.getPhase() != null) {
            entity.setPhaseId(dto.getPhase().getId());
        } else {
            entity.setPhaseId(null);
        }

        if (dto.getRecommendationType() != null) {
            entity.setRecommendationTypeId(dto.getRecommendationType().getId());
        } else {
            entity.setRecommendationTypeId(null);
        }
    }

    public void updateEntityFromDto(RecommendationDto dto, Recommendation entity, Long currentUserId) {
        if (dto == null || entity == null) return;
        // Lưu lại các giá trị Audit quan trọng không được phép thay đổi
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        Long originalCreatedById = entity.getCreatedById();
        Long originalCreatedOrgId = entity.getCreatedOrgId();
        String originalCode = entity.getRecommendationCode();

        // Map các trường cơ bản từ DTO sang Entity
        modelMapper.map(dto, entity);

        // Khôi phục lại các giá trị Audit để đảm bảo không bị ghi đè bởi DTO
        entity.setCreatedAt(originalCreatedAt);
        entity.setCreatedById(originalCreatedById);
        entity.setCreatedOrgId(originalCreatedOrgId);
        entity.setRecommendationCode(originalCode);

        // Đảm bảo các trường ID quan trọng không bị ghi đè bởi null từ DTO nếu logic nghiệp vụ yêu cầu
        setSelectFields(dto, entity);
        entity.setLastUpdate(LocalDateTime.now());
        entity.setLastUpdateBy(currentUserId);
    }

    public List<RecommendationWorkItem> mapToRecommendationWorkItem(List<WorkItemDto> workItemDtos, Long recommendationId, Long userId) {
        return workItemDtos.stream().map(workItemDto -> {
            RecommendationWorkItem recommendationWorkItem = new RecommendationWorkItem();
            recommendationWorkItem.setRecommendationId(recommendationId);
            recommendationWorkItem.setWorkItemId(workItemDto.getId());
            recommendationWorkItem.setCreatedAt(LocalDateTime.now());
            recommendationWorkItem.setCreatedBy(userId);
            recommendationWorkItem.setIsDeleted(false);
            return recommendationWorkItem;
        }).collect(Collectors.toList());
    }

    public List<RecommendationAssignment> mapToRecommendationAssignment(List<UserDto> assignedUsers, Long recommendationId) {
        return assignedUsers.stream().map(assignedUser -> {
            RecommendationAssignment recommendationWorkItem = new RecommendationAssignment();
            recommendationWorkItem.setRecommendationId(recommendationId);
            recommendationWorkItem.setOrg(assignedUser.getOrg() != null ? assignedUser.getOrg().getId() : null);
            recommendationWorkItem.setAssignedAt(LocalDateTime.now());
            recommendationWorkItem.setUserId(assignedUser.getId());
            recommendationWorkItem.setIsDeleted(false);
            recommendationWorkItem.setIsPrimary(true);
            return recommendationWorkItem;
        }).collect(Collectors.toList());
    }
}
