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
                        mapper.skip(OutstandingItem::setCreatedOrgId);
                        mapper.skip(OutstandingItem::setAssignedUserId);
                        mapper.skip(OutstandingItem::setAssignedOrgId);
                        mapper.skip(OutstandingItem::setStatus);
                        mapper.skip(OutstandingItem::setLastUpdateBy);
                    });
        }

        // Cấu hình mapping từ Entity -> DTO
        if (modelMapper.getTypeMap(OutstandingItem.class, OutstandingItemDto.class) == null) {
            modelMapper.typeMap(OutstandingItem.class, OutstandingItemDto.class)
                    .addMappings(mapper -> {
                        // Bỏ qua các field dạng object sẽ được set thủ công
                        mapper.skip(OutstandingItemDto::setAcceptanceType);
                        mapper.skip(OutstandingItemDto::setOutstandingType);
                        mapper.skip(OutstandingItemDto::setPriority);
                        mapper.skip(OutstandingItemDto::setCreatedByUser);
                        mapper.skip(OutstandingItemDto::setCreatedOrg);
                        mapper.skip(OutstandingItemDto::setAssignedUser);
                        mapper.skip(OutstandingItemDto::setAssignedOrg);
                        mapper.skip(OutstandingItemDto::setStatus);
                        mapper.skip(OutstandingItemDto::setLastUpdateBy);
                    });
        }
    }

    public List<OutstandingItemDto> mapToOutstandingItemDto(List<OutstandingItem> outstandingItems) {
        if (outstandingItems == null || outstandingItems.isEmpty()) return new ArrayList<>();
        List<Long> outstandingIds = outstandingItems.stream().map(OutstandingItem::getId).distinct().toList();
        List<Long> projectIds = outstandingItems.stream().map(OutstandingItem::getProjectId).distinct().filter(Objects::nonNull).toList();
        List<Long> itemIds = outstandingItems.stream().map(OutstandingItem::getItemId).distinct().filter(Objects::nonNull).toList();
        List<Long> recommendationTypeIds = outstandingItems.stream().map(OutstandingItem::getOutstandingTypeId).distinct().filter(Objects::nonNull).toList();

        Map<Long, Project> projectMap = !projectIds.isEmpty() ? projectRepo.findAllByIdInAndIsDeletedFalse(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity())) : null;

        Map<Long, CatOutstandingType> outstandingTypeMap = !recommendationTypeIds.isEmpty() ? outstandingTypeRepository.findAllByIdInAndIsDeletedFalse(recommendationTypeIds).stream()
                .collect(Collectors.toMap(CatOutstandingType::getId, Function.identity())) : null;

        Map<Long, WorkItem> workItemMap = !outstandingIds.isEmpty() ? workItemRepo.findAllByOutstandingIdInAndIsDeletedFalse(outstandingIds).stream().collect(Collectors.toMap(WorkItem::getId, Function.identity())) : null;

        Map<Long, ProjectItem> projectItemMap = !itemIds.isEmpty() ? projectItemRepo.findAllByIdInAndIsDeletedFalse(itemIds).stream().collect(Collectors.toMap(ProjectItem::getId, Function.identity())) : null;

        Map<Long, SysUser> sysUserMap = sysUserRepo.findAllByIsDeletedFalse().stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));

        Map<Long, SysOrg> sysOrgMap = sysOrgRepo.findAllByIsDeletedFalse().stream().collect(Collectors.toMap(SysOrg::getId, Function.identity()));

        Map<Long, List<AttachmentDto>> attachmentMap;
        if (outstandingItems.size() == 1) { // chỉ lấy attachment khi chỉ có 1 outstandingItem để tối ưu
            List<Attachment> attachments = attachmentRepo.findAllByReferenceIdAndReferenceTypeAndIsDeletedFalse(outstandingItems.get(0).getId(), Constants.OUTSTANDING_REFERENCE_TYPE);
            attachmentMap = attachments.stream().map((element) -> modelMapper.map(element, AttachmentDto.class)).collect(Collectors.groupingBy(AttachmentDto::getReferenceId));
        } else {
            attachmentMap = null;
        }

        return outstandingItems.stream().map(item -> toDto(item, projectMap, projectItemMap, outstandingTypeMap, workItemMap, sysUserMap, sysOrgMap, attachmentMap)).toList();
    }

    /**
     * Entity -> DTO
     */
    public OutstandingItemDto toDto(OutstandingItem entity, Map<Long, Project> projectMap, Map<Long, ProjectItem> projectItemMap, Map<Long, CatOutstandingType> outstandingTypeMap, Map<Long, WorkItem> workItemMap, Map<Long, SysUser> userMap, Map<Long, SysOrg> orgMap, Map<Long, List<AttachmentDto>> attachmentMap) {
        if (entity == null) return null;

        OutstandingItemDto dto = modelMapper.map(entity, OutstandingItemDto.class);
        if (projectMap != null && entity.getProjectId() != null && projectMap.containsKey(entity.getProjectId())) {
            dto.setProject(modelMapper.map(projectMap.get(entity.getProjectId()), ProjectDto.class));
        }
        if (projectItemMap != null && entity.getItemId() != null && projectItemMap.containsKey(entity.getItemId())) {
            dto.setProjectItem(modelMapper.map(projectItemMap.get(entity.getItemId()), ProjectItemDto.class));
        }

        if (workItemMap != null && entity.getWorkItemId() != null) {
            dto.setWorkItem(modelMapper.map(workItemMap.get(entity.getWorkItemId()), WorkItemDto.class));
        }

        // acceptanceType: String code -> AcceptanceTypeDto
        if (entity.getAcceptanceType() != null) {
            AcceptanceTypeDto acceptanceTypeDto = new AcceptanceTypeDto();
            acceptanceTypeDto.setCode(entity.getAcceptanceType());
            acceptanceTypeDto.setName(OutstandingAcceptianceTypeEnum.valueOf(entity.getAcceptanceType()).getVietnameseName());
            dto.setAcceptanceType(acceptanceTypeDto);
        }
        // status: String code -> StatusDto
        if (entity.getStatus() != null) {
            StatusDto statusDto = new StatusDto();
            statusDto.setCode(entity.getStatus());
            statusDto.setName(OutstandingStatusEnum.valueOf(entity.getStatus()).getVietnameseName());
            dto.setStatus(statusDto);
        }
        // priority: String code -> PriorityDto
        if (entity.getPriority() != null) {
            PriorityDto priorityDto = new PriorityDto();
            priorityDto.setCode(entity.getPriority());
            priorityDto.setName(PriorityEnum.valueOf(entity.getPriority()).getVietnameseName());
            dto.setPriority(priorityDto);
        }

        // outstandingTypeId -> OutstandingTypeDto
        if (entity.getOutstandingTypeId() != null && outstandingTypeMap != null && outstandingTypeMap.containsKey(entity.getOutstandingTypeId())) {
            OutstandingTypeDto outstandingTypeDto = modelMapper.map(outstandingTypeMap.get(entity.getOutstandingTypeId()), OutstandingTypeDto.class);
            dto.setOutstandingType(outstandingTypeDto);
        }

        if (entity.getCreatedBy() != null && userMap != null && userMap.containsKey(entity.getCreatedBy())) {
            UserDto createdByUser = modelMapper.map(userMap.get(entity.getCreatedBy()), UserDto.class);
            dto.setCreatedByUser(createdByUser);
        }

        if (entity.getCreatedOrgId() != null && orgMap != null && orgMap.containsKey(entity.getCreatedOrgId())) {
            OrgDto createdOrg = modelMapper.map(orgMap.get(entity.getCreatedOrgId()), OrgDto.class);
            dto.setCreatedOrg(createdOrg);
        }

        // assignedUserId: Long -> UserDto
        if (entity.getAssignedUserId() != null && userMap != null && userMap.containsKey(entity.getAssignedUserId())) {
            UserDto assignedUser = modelMapper.map(userMap.get(entity.getAssignedUserId()), UserDto.class);
            dto.setAssignedUser(assignedUser);
        }

        // assignedOrgId: Long -> OrgDto
        if (entity.getAssignedOrgId() != null && orgMap != null && orgMap.containsKey(entity.getAssignedOrgId())) {
            OrgDto assignedOrg = modelMapper.map(orgMap.get(entity.getAssignedOrgId()), OrgDto.class);
            dto.setAssignedOrg(assignedOrg);
        }

        if (entity.getLastUpdateBy() != null && userMap != null && userMap.containsKey(entity.getLastUpdateBy())) {
            UserDto lastUpdateBy = modelMapper.map(userMap.get(entity.getLastUpdateBy()), UserDto.class);
            dto.setLastUpdateBy(lastUpdateBy);
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
        entity.setCreatedBy(user != null ? user.getId() : Constants.DEFAULT_USER_ID);
        entity.setCreatedOrgId(user != null ? user.getOrgId() : Constants.DEFAULT_USER_ID);
        entity.setStatus(RecommendationStatusEnum.NEW.name());
        return entity;
    }

    /**
     * Gán các field được chọn (code/id) từ DTO sang Entity
     * tương tự cách RecommendationMapper.setSelectFields(...)
     */
    private void setSelectFields(OutstandingItemDto dto, OutstandingItem entity) {
        // acceptanceType
        if (dto.getAcceptanceType() != null) {
            entity.setAcceptanceType(dto.getAcceptanceType().getCode());
        } else {
            entity.setAcceptanceType(null);
        }

        // outstandingType
        if (dto.getOutstandingType() != null) {
            entity.setOutstandingTypeId(dto.getOutstandingType().getId());
        } else {
            entity.setOutstandingTypeId(null);
        }

        // priority
        if (dto.getPriority() != null) {
            entity.setPriority(dto.getPriority().getCode());
        } else {
            entity.setPriority(null);
        }

        // createdByUser
        if (dto.getCreatedByUser() != null) {
            entity.setCreatedBy(dto.getCreatedByUser().getId());
        }

        // createdOrg
        if (dto.getCreatedOrg() != null) {
            entity.setCreatedOrgId(dto.getCreatedOrg().getId());
        }

        // assignedUser
        if (dto.getAssignedUser() != null) {
            entity.setAssignedUserId(dto.getAssignedUser().getId());
        }

        // assignedOrg
        if (dto.getAssignedOrg() != null) {
            entity.setAssignedOrgId(dto.getAssignedOrg().getId());
        }

        // status
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus().getCode());
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
        Long originalCreatedBy = entity.getCreatedBy();
        Long originalCreatedOrgId = entity.getCreatedOrgId();
        String originalCode = entity.getOutstandingCode();
        Long originalProjectId = entity.getProjectId();
        Long originalItemId = entity.getItemId();

        // Map các trường cơ bản
        modelMapper.map(dto, entity);

        // Khôi phục lại các giá trị không được phép đổi
        entity.setCreatedAt(originalCreatedAt);
        entity.setCreatedBy(originalCreatedBy);
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
