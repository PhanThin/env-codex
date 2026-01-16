package vn.com.viettel.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.*;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.utils.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WorkItemMapper {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ProjectItemRepository projectItemRepository;
    @Autowired
    private CatProjectPhaseRepository catProjectPhaseRepository;
    @Autowired
    private CatUnitRepository catUnitRepository;
    @Autowired
    private ProjectTypeRepository projectTypeRepository;
    @Autowired
    private CategoryWorkItemRepository categoryWorkItemRepository;
    @Autowired
    private SysUserRepository sysUserRepository;

//    @PostConstruct
//    private void configure() {
//        modelMapper.getConfiguration()
//                .setImplicitMappingEnabled(false);
//        var dtoToEntity = modelMapper.getTypeMap(WorkItemDto.class, WorkItem.class);
//        if (dtoToEntity == null) {
//            dtoToEntity = modelMapper.createTypeMap(WorkItemDto.class, WorkItem.class);
//
//        } else {
//            dtoToEntity.getMappings().clear();
//        }
//        dtoToEntity.addMappings(mapper -> {
//            mapper.skip(WorkItem::setCreatedBy);
//            mapper.skip(WorkItem::setProjectItem);
//            mapper.skip(WorkItem::setProjectPhase);
//            mapper.skip(WorkItem::setProjectType);
//            mapper.skip(WorkItem::setUnit);
//        });
//        var entityToDto = modelMapper.getTypeMap(WorkItem.class, WorkItemDto.class);
//        if (entityToDto == null) {
//            entityToDto = modelMapper.createTypeMap(WorkItem.class, WorkItemDto.class);
//        } else {
//            entityToDto.getMappings().clear();
//        }
//        entityToDto.addMappings(mapper -> {
//            mapper.skip(WorkItemDto::setCreatedBy);
//            mapper.skip(WorkItemDto::setProjectItem);
//            mapper.skip(WorkItemDto::setProjectPhase);
//            mapper.skip(WorkItemDto::setProjectType);
//            mapper.skip(WorkItemDto::setUnit);
//        });
//    }

    /**
     * Map list WorkItem -> list WorkItemDto, enrich data từ các bảng liên quan.
     */
    public List<WorkItemDto> mapToDtos(List<WorkItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        List<Long> projectItemIds = entities.stream()
                .map(WorkItem::getItemId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> phaseIds = entities.stream()
                .map(WorkItem::getProjectPhaseId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> unitIds = entities.stream()
                .map(WorkItem::getUnitId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> projectTypeIds = entities.stream()
                .map(WorkItem::getProjectTypeId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> catWorkItemIds = entities.stream()
                .map(WorkItem::getCatWorkItemId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> createdByIds = entities.stream()
                .map(WorkItem::getCreatedByUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Long> updatedByIds = entities.stream()
                .map(WorkItem::getUpdatedByUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Long> userIds = java.util.stream.Stream.concat(createdByIds.stream(), updatedByIds.stream())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, ProjectItem> projectItemMap = projectItemRepository.findAllByIdInAndIsDeletedFalse(projectItemIds).stream()
                .collect(Collectors.toMap(ProjectItem::getId, Function.identity()));

        Map<Long, CatProjectPhase> phaseMap = catProjectPhaseRepository.findAllByIdInAndIsDeletedFalse(phaseIds).stream()
                .collect(Collectors.toMap(CatProjectPhase::getId, Function.identity()));

        Map<Long, CatUnit> unitMap = catUnitRepository.findAllByIdInAndIsDeletedFalse(unitIds).stream()
                .collect(Collectors.toMap(CatUnit::getId, Function.identity()));

        Map<Long, ProjectType> projectTypeMap = projectTypeRepository.findAllByIdInAndIsDeletedFalse(projectTypeIds).stream()
                .collect(Collectors.toMap(ProjectType::getId, Function.identity()));

        Map<Long, CategoryWorkItem> categoryWorkItemMap = categoryWorkItemRepository.findAllByIdInAndIsDeletedFalse(catWorkItemIds).stream()
                .collect(Collectors.toMap(CategoryWorkItem::getId, Function.identity()));

        Map<Long, SysUser> userMap = userIds.isEmpty()
                ? Map.of()
                : sysUserRepository.findAllByIdInAndIsDeletedFalse(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        return entities.stream()
                .map(entity -> toDto(
                        entity,
                        projectItemMap.get(entity.getItemId()),
                        phaseMap.get(entity.getProjectPhaseId()),
                        unitMap.get(entity.getUnitId()),
                        projectTypeMap.get(entity.getProjectTypeId()),
                        categoryWorkItemMap.get(entity.getCatWorkItemId()),
                        userMap.get(entity.getCreatedByUserId()),
                        userMap.get(entity.getUpdatedByUserId())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Entity -> DTO cho 1 bản ghi WorkItem.
     */
    public WorkItemDto toDto(WorkItem entity,
                             ProjectItem projectItem,
                             CatProjectPhase projectPhase,
                             CatUnit unit,
                             ProjectType projectType,
                             CategoryWorkItem categoryWorkItem,
                             SysUser createdBy,
                             SysUser updatedBy) {
        if (entity == null) {
            return null;
        }

        WorkItemDto dto = modelMapper.map(entity, WorkItemDto.class);

        if (projectItem != null) {
            dto.setProjectItem(modelMapper.map(projectItem, ProjectItemDto.class));
        }
        if (projectPhase != null) {
            dto.setProjectPhase(modelMapper.map(projectPhase, CatProjectPhaseDto.class));
        }
        if (unit != null) {
            dto.setUnit(modelMapper.map(unit, CatUnitDto.class));
        }
        if (projectType != null) {
            dto.setProjectType(modelMapper.map(projectType, ProjectTypeDto.class));
        }
        if (categoryWorkItem != null) {
            dto.setCategoryWorkItem(modelMapper.map(categoryWorkItem, CategoryWorkItemDto.class));
        }
        if (createdBy != null) {
            dto.setCreatedBy(modelMapper.map(createdBy, UserDto.class));
        }
        if (updatedBy != null) {
            dto.setUpdatedBy(modelMapper.map(updatedBy, UserDto.class));
        }

        return dto;
    }

    /**
     * DTO -> Entity (tạo mới WorkItem).
     */
    public WorkItem toEntity(WorkItemDto dto, SysUser currentUser) {
        if (dto == null) {
            return null;
        }

        WorkItem entity = modelMapper.map(dto, WorkItem.class);
        setSelectFields(dto, entity);

        entity.setIsDeleted(Boolean.FALSE);
        if (entity.getIsActive() == null) {
            entity.setIsActive(Boolean.TRUE);
        }
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedByUserId(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);

        return entity;
    }

    /**
     * Cập nhật Entity từ DTO (chỉnh sửa).
     * Giữ lại các trường audit quan trọng.
     */
    public void updateEntityFromDto(WorkItemDto dto, WorkItem entity, SysUser currentUser) {
        if (dto == null || entity == null) {
            return;
        }

        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        Long originalCreatedBy = entity.getCreatedByUserId();
        Boolean originalIsDeleted = entity.getIsDeleted();

        modelMapper.map(dto, entity);

        entity.setCreatedAt(originalCreatedAt);
        entity.setCreatedByUserId(originalCreatedBy);
        entity.setIsDeleted(originalIsDeleted);

        setSelectFields(dto, entity);

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedByUserId(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
    }

    /**
     * Gán các field ID từ DTO con sang Entity.
     */
    private void setSelectFields(WorkItemDto dto, WorkItem entity) {
        if (dto.getProjectItem() != null) {
            entity.setItemId(dto.getProjectItem().getId());
        } else {
            entity.setItemId(null);
        }

        if (dto.getProjectPhase() != null) {
            entity.setProjectPhaseId(dto.getProjectPhase().getId());
        } else {
            entity.setProjectPhaseId(null);
        }

        if (dto.getUnit() != null) {
            entity.setUnitId(dto.getUnit().getId());
        } else {
            entity.setUnitId(null);
        }

        if (dto.getProjectType() != null) {
            entity.setProjectTypeId(dto.getProjectType().getId());
        } else {
            entity.setProjectTypeId(null);
        }

        if (dto.getCategoryWorkItem() != null) {
            entity.setCatWorkItemId(dto.getCategoryWorkItem().getId());
        } else {
            entity.setCatWorkItemId(null);
        }
    }
}
