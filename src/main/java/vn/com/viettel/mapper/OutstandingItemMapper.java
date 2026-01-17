package vn.com.viettel.mapper;

import jakarta.annotation.PostConstruct;
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

@Component
public class OutstandingItemMapper {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private SysUserRepository sysUserRepo;
    @Autowired
    private CatOutstandingTypeRepository outstandingTypeRepository;
    @Autowired
    private WorkItemRepository workItemRepo;
    @Autowired
    private ProjectItemRepository projectItemRepo;
    @Autowired
    private AttachmentRepository attachmentRepo;
    @Autowired
    private SysOrgRepository sysOrgRepo;
    @Autowired
    private CatProjectPhaseRepository phaseRepo;

    @PostConstruct
    private void configure() {
        // Cấu hình mapping từ DTO -> Entity
        if (modelMapper.getTypeMap(OutstandingItemDto.class, OutstandingItem.class) == null) {
            modelMapper.typeMap(OutstandingItemDto.class, OutstandingItem.class)
                    .addMappings(mapper -> {
                        // Bỏ qua các field dạng code/id sẽ được set thủ công
                        mapper.skip(OutstandingItem::setAcceptanceType);
                        mapper.skip(OutstandingItem::setOutstandingTypeId);
                        mapper.skip(OutstandingItem::setPriority);
                        mapper.skip(OutstandingItem::setCreatedBy);
                        mapper.skip(OutstandingItem::setCreatedById);
                        mapper.skip(OutstandingItem::setCreatedOrgId);
                        mapper.skip(OutstandingItem::setAssignedUserId);
                        mapper.skip(OutstandingItem::setAssignedOrgId);
                        mapper.skip(OutstandingItem::setStatus);
                        mapper.skip(OutstandingItem::setLastUpdateBy);
                        mapper.skip(OutstandingItem::setProjectItem);
                        mapper.skip(OutstandingItem::setProject);
                        mapper.skip(OutstandingItem::setPhase);
                        mapper.skip(OutstandingItem::setWorkItem);
                        mapper.skip(OutstandingItem::setOutstandingType);
                    });
        }

        // Cấu hình mapping từ Entity -> DTO
        if (modelMapper.getTypeMap(OutstandingItem.class, OutstandingItemDto.class) == null) {
            modelMapper.typeMap(OutstandingItem.class, OutstandingItemDto.class)
                    .addMappings(mapper -> {
                        // Bỏ qua các field dạng object sẽ được set thủ công
                        mapper.skip(OutstandingItemDto::setAcceptanceType);
                        mapper.skip(OutstandingItemDto::setPriorityDto);
                        mapper.skip(OutstandingItemDto::setCreatedByUserDto);
                        mapper.skip(OutstandingItemDto::setCreatedOrgDto);
                        mapper.skip(OutstandingItemDto::setStatusDto);
                        mapper.skip(OutstandingItemDto::setAssignedUserDto);
                        mapper.skip(OutstandingItemDto::setAssignedOrgDto);
                        mapper.skip(OutstandingItemDto::setProjectItemDto);
                        mapper.skip(OutstandingItemDto::setProjectDto);
                        mapper.skip(OutstandingItemDto::setPhaseDto);
                        mapper.skip(OutstandingItemDto::setWorkItemDto);
                        mapper.skip(OutstandingItemDto::setOutstandingTypeDto);

                    });
        }
    }

    public List<OutstandingItemDto> mapToOutstandingItemDto(List<OutstandingItem> outstandingItems) {
        if (outstandingItems == null || outstandingItems.isEmpty()) return new ArrayList<>();
        List<Long> phaseId = outstandingItems.stream().map(OutstandingItem::getPhaseId).distinct().filter(Objects::nonNull).toList();
        List<Long> projectIds = outstandingItems.stream().map(OutstandingItem::getProjectId).distinct().filter(Objects::nonNull).toList();
        List<Long> itemIds = outstandingItems.stream().map(OutstandingItem::getItemId).distinct().filter(Objects::nonNull).toList();
        List<Long> recommendationTypeIds = outstandingItems.stream().map(OutstandingItem::getOutstandingTypeId).distinct().filter(Objects::nonNull).toList();
        List<Long> workItemIds = outstandingItems.stream().map(OutstandingItem::getWorkItemId).distinct().filter(Objects::nonNull).toList();

        Map<Long, Project> projectMap = !projectIds.isEmpty() ? projectRepo.findAllByIdInAndIsDeletedFalse(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity())) : null;
        Map<Long, CatProjectPhase> phaseMap = !phaseId.isEmpty() ? phaseRepo.findAllByIdInAndIsDeletedFalse(phaseId).stream()
                .collect(Collectors.toMap(CatProjectPhase::getId, Function.identity())) : null;
        Map<Long, CatOutstandingType> outstandingTypeMap = !recommendationTypeIds.isEmpty() ? outstandingTypeRepository.findAllByIdInAndIsDeletedFalse(recommendationTypeIds).stream()
                .collect(Collectors.toMap(CatOutstandingType::getId, Function.identity())) : null;

        Map<Long, WorkItem> workItemMap = !workItemIds.isEmpty() ? workItemRepo.findAllByIdInAndIsDeletedFalse(workItemIds).stream().collect(Collectors.toMap(WorkItem::getId, Function.identity())) : null;

        Map<Long, ProjectItem> projectItemMap = !itemIds.isEmpty() ? projectItemRepo.findAllByIdInAndIsDeletedFalse(itemIds).stream().collect(Collectors.toMap(ProjectItem::getId, Function.identity())) : null;

        Map<Long, SysUser> sysUserMap = sysUserRepo.findAllByIsDeletedFalse().stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));

        Map<Long, SysOrg> sysOrgMap = sysOrgRepo.findAllByIsDeletedFalse().stream().collect(Collectors.toMap(SysOrg::getId, Function.identity()));

        Map<Long, List<AttachmentDto>> attachmentMap;
        if (outstandingItems.size() == 1) { // chỉ lấy attachment khi chỉ có 1 outstandingItem để tối ưu
            List<Attachment> attachments = attachmentRepo.findAllByReferenceIdAndReferenceTypeInAndIsDeletedFalse(outstandingItems.get(0).getId(), List.of(Constants.OUTSTANDING_REFERENCE_TYPE, Constants.OUTSTANDING_DOCUMENT_REFERENCE_TYPE_ACCEPTANCE, Constants.OUTSTANDING_DOCUMENT_REFERENCE_TYPE_DOCUMENT));
            attachmentMap = attachments.stream().map((element) -> modelMapper.map(element, AttachmentDto.class)).collect(Collectors.groupingBy(AttachmentDto::getReferenceId));
        } else {
            attachmentMap = null;
        }

        return outstandingItems.stream().map(item -> toDto(item, projectMap, phaseMap, projectItemMap, outstandingTypeMap, workItemMap, sysUserMap, sysOrgMap, attachmentMap)).toList();
    }

    /**
     * Entity -> DTO
     */
    public OutstandingItemDto toDto(OutstandingItem entity, Map<Long, Project> projectMap, Map<Long, CatProjectPhase> phaseMap, Map<Long, ProjectItem> projectItemMap, Map<Long, CatOutstandingType> outstandingTypeMap, Map<Long, WorkItem> workItemMap, Map<Long, SysUser> userMap, Map<Long, SysOrg> orgMap, Map<Long, List<AttachmentDto>> attachmentMap) {
        if (entity == null) return null;

        OutstandingItemDto dto = modelMapper.map(entity, OutstandingItemDto.class);
        if (projectMap != null && entity.getProjectId() != null && projectMap.containsKey(entity.getProjectId())) {
            dto.setProjectDto(modelMapper.map(projectMap.get(entity.getProjectId()), ProjectDto.class));
        }
        if (projectItemMap != null && entity.getItemId() != null && projectItemMap.containsKey(entity.getItemId())) {
            dto.setProjectItemDto(modelMapper.map(projectItemMap.get(entity.getItemId()), ProjectItemDto.class));
        }
        if (phaseMap != null && entity.getPhaseId() != null && phaseMap.containsKey(entity.getPhaseId())) {
            dto.setPhaseDto(modelMapper.map(phaseMap.get(entity.getPhaseId()), CatProjectPhaseDto.class));
        }
        if (workItemMap != null && entity.getWorkItemId() != null && workItemMap.containsKey(entity.getWorkItemId())) {
            dto.setWorkItemDto(modelMapper.map(workItemMap.get(entity.getWorkItemId()), WorkItemDto.class));
        }

        // acceptanceType: String code -> AcceptanceTypeDto
        if (entity.getAcceptanceType() != null) {
            AcceptanceTypeDto acceptanceTypeDto = new AcceptanceTypeDto();
            acceptanceTypeDto.setCode(entity.getAcceptanceType());
            acceptanceTypeDto.setName(OutstandingAcceptanceTypeEnum.valueOf(entity.getAcceptanceType()).getVietnameseName());
            dto.setAcceptanceType(acceptanceTypeDto);
        }
        // status: String code -> StatusDto
        if (entity.getStatus() != null) {
            StatusDto statusDto = new StatusDto();
            statusDto.setCode(entity.getStatus());
            statusDto.setName(OutstandingStatusEnum.valueOf(entity.getStatus()).getVietnameseName());
            dto.setStatusDto(statusDto);
        }
        // priority: String code -> PriorityDto
        if (entity.getPriority() != null) {
            PriorityDto priorityDto = new PriorityDto();
            priorityDto.setCode(entity.getPriority());
            priorityDto.setName(PriorityEnum.valueOf(entity.getPriority()).getVietnameseName());
            dto.setPriorityDto(priorityDto);
        }

        // outstandingTypeId -> OutstandingTypeDto
        if (entity.getOutstandingTypeId() != null && outstandingTypeMap != null && outstandingTypeMap.containsKey(entity.getOutstandingTypeId())) {
            CatOutstandingTypeDto outstandingTypeDto = modelMapper.map(outstandingTypeMap.get(entity.getOutstandingTypeId()), CatOutstandingTypeDto.class);
            dto.setOutstandingTypeDto(outstandingTypeDto);
        }

        if (entity.getCreatedById() != null && userMap != null && userMap.containsKey(entity.getCreatedById())) {
            UserDto createdByUser = modelMapper.map(userMap.get(entity.getCreatedById()), UserDto.class);
            dto.setCreatedByUserDto(createdByUser);
        }

        if (entity.getCreatedOrgId() != null && orgMap != null && orgMap.containsKey(entity.getCreatedOrgId())) {
            OrgDto createdOrg = modelMapper.map(orgMap.get(entity.getCreatedOrgId()), OrgDto.class);
            dto.setCreatedOrgDto(createdOrg);
        }

        // assignedUserId: Long -> UserDto
        if (entity.getAssignedUserId() != null && userMap != null && userMap.containsKey(entity.getAssignedUserId())) {
            UserDto assignedUser = modelMapper.map(userMap.get(entity.getAssignedUserId()), UserDto.class);
            dto.setAssignedUserDto(assignedUser);
        }

        // assignedOrgId: Long -> OrgDto
        if (entity.getAssignedOrgId() != null && orgMap != null && orgMap.containsKey(entity.getAssignedOrgId())) {
            OrgDto assignedOrg = modelMapper.map(orgMap.get(entity.getAssignedOrgId()), OrgDto.class);
            dto.setAssignedOrgDto(assignedOrg);
        }

        if (entity.getLastUpdateBy() != null && userMap != null && userMap.containsKey(entity.getLastUpdateBy())) {
            UserDto lastUpdateBy = modelMapper.map(userMap.get(entity.getLastUpdateBy()), UserDto.class);
            dto.setLastUpdateByDto(lastUpdateBy);
        }

        if (attachmentMap != null) {
            List<AttachmentDto> attachments = attachmentMap.get(entity.getId());
            if (attachments != null) {
                dto.setAttachments(attachments);
            }
        }

        return dto;
    }

    /**
     * DTO -> Entity (tạo mới)
     */
    public OutstandingItem toEntity(OutstandingItemDto dto, SysUser user) {
        if (dto == null) return null;

        OutstandingItem entity = modelMapper.map(dto, OutstandingItem.class);
        setSelectFields(dto, entity);

        // Thiết lập các giá trị mặc định cho bản ghi mới
        entity.setCreatedAt(LocalDateTime.now());
        entity.setIsLocked(Boolean.FALSE);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setCreatedById(user != null ? user.getId() : Constants.DEFAULT_USER_ID);
        entity.setCreatedOrgId(user != null ? user.getOrgId() : Constants.DEFAULT_USER_ID);
        entity.setStatus(RecommendationStatusEnum.NEW.name());
        return entity;
    }

    /**
     * Gán các field được chọn (code/id) từ DTO sang Entity
     * tương tự cách RecommendationMapper.setSelectFields(...)
     */
    private void setSelectFields(OutstandingItemDto dto, OutstandingItem entity) {
        if (dto.getProjectDto() != null) {
            entity.setProjectId(dto.getProjectDto().getId());
        }
        if (dto.getProjectItemDto() != null) {
            entity.setItemId(dto.getProjectItemDto().getId());
        }
        if (dto.getWorkItemDto() != null) {
            entity.setWorkItemId(dto.getWorkItemDto().getId());
        }
        if (dto.getPhaseDto() != null) {
            entity.setPhaseId(dto.getPhaseDto().getId());
        }
        // acceptanceType
        if (dto.getAcceptanceType() != null) {
            entity.setAcceptanceType(dto.getAcceptanceType().getCode());
        } else {
            entity.setAcceptanceType(null);
        }

        // outstandingType
        if (dto.getOutstandingTypeDto() != null) {
            entity.setOutstandingTypeId(dto.getOutstandingTypeDto().getId());
        } else {
            entity.setOutstandingTypeId(null);
        }

        // priority
        if (dto.getPriorityDto() != null) {
            entity.setPriority(dto.getPriorityDto().getCode());
        } else {
            entity.setPriority(null);
        }

        // createdByUser
        if (dto.getCreatedByUserDto() != null) {
            entity.setCreatedById(dto.getCreatedByUserDto().getId());
        }

        // createdOrg
        if (dto.getCreatedOrgDto() != null) {
            entity.setCreatedOrgId(dto.getCreatedOrgDto().getId());
        }

        // assignedUser
        if (dto.getAssignedUserDto() != null) {
            entity.setAssignedUserId(dto.getAssignedUserDto().getId());
        }

        // assignedOrg
        if (dto.getAssignedOrgDto() != null) {
            entity.setAssignedOrgId(dto.getAssignedOrgDto().getId());
        }

        // status
        if (dto.getStatusDto() != null) {
            entity.setStatus(dto.getStatusDto().getCode());
        }

    }

    /**
     * Cập nhật Entity từ DTO (chỉnh sửa)
     * Giữ lại các trường audit quan trọng tương tự RecommendationMapper.updateEntityFromDto(...)
     */
    public void updateEntityFromDto(OutstandingItemDto dto, OutstandingItem entity, Long currentUserId) {
        if (dto == null || entity == null) return;

        // Lưu lại các giá trị không được phép thay đổi
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        Long originalCreatedBy = entity.getCreatedById();
        Long originalCreatedOrgId = entity.getCreatedOrgId();
        String originalCode = entity.getOutstandingCode();
        Long originalProjectId = entity.getProjectId();
        Long originalItemId = entity.getItemId();

        // Map các trường cơ bản
        modelMapper.map(dto, entity);

        // Khôi phục lại các giá trị không được phép đổi
        entity.setCreatedAt(originalCreatedAt);
        entity.setCreatedById(originalCreatedBy);
        entity.setCreatedOrgId(originalCreatedOrgId);
        entity.setOutstandingCode(originalCode);
        entity.setProjectId(originalProjectId);
        entity.setItemId(originalItemId);

        // Set lại các field dạng code/id từ DTO
        setSelectFields(dto, entity);

        // Cập nhật thông tin sửa
        entity.setLastUpdateAt(LocalDateTime.now());
        entity.setLastUpdateBy(currentUserId);
    }

    public List<OutstandingAlertConfig> mapToOutstandingAlertConfig(List<OutstandingAlertConfigDto> dtoList, Long outstandingId, Long userId) {
        return dtoList.stream().map(dto -> {
            OutstandingAlertConfig entity = modelMapper.map(dto, OutstandingAlertConfig.class);
            entity.setIsDeleted(false);
            entity.setOutstandingId(outstandingId);
            entity.setCreatedBy(userId);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        }).toList();
    }

    public List<OutstandingAlertConfig> mapToOutstandingAlertConfig(List<OutstandingAlertConfigDto> dtoList, Long currentUserId) {
        return dtoList.stream().map(dto -> {
            OutstandingAlertConfig config = modelMapper.map(dto, OutstandingAlertConfig.class);
            config.setCreatedBy(currentUserId);
            return config;
        }).toList();
    }
}
