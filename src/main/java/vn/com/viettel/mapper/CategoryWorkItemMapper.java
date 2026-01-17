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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CategoryWorkItemMapper {

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
    private SysUserRepository sysUserRepository;

//    @PostConstruct
//    private void configure() {
//        var dtoToEntity = modelMapper.createTypeMap(CategoryWorkItemDto.class, CategoryWorkItem.class);
//        dtoToEntity.addMappings(mapper -> {
//            mapper.skip(CategoryWorkItem::setCatProjectItem);
//            mapper.skip(CategoryWorkItem::setCatProjectPhase);
//            mapper.skip(CategoryWorkItem::setCatProjectType);
//            mapper.skip(CategoryWorkItem::setCatUnit);
//        });
//
//        var entityToDto = modelMapper.createTypeMap(CategoryWorkItem.class, CategoryWorkItemDto.class);
//
//        entityToDto.addMappings(mapper -> {
//            mapper.skip(CategoryWorkItemDto::setProjectItem);
//            mapper.skip(CategoryWorkItemDto::setProjectPhase);
//            mapper.skip(CategoryWorkItemDto::setProjectType);
//            mapper.skip(CategoryWorkItemDto::setUnit);
//        });
//
//    }

    public List<CategoryWorkItemDto> mapToDtos(List<CategoryWorkItem> entities) {
        Map<Long, ProjectType> projectTypeMap = projectTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ProjectType::getId, Function.identity()));
        Map<Long, SysUser> sysUserMap = sysUserRepository.findAll().stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, CatUnit> catUnitMap = catUnitRepository.findAll().stream()
                .collect(Collectors.toMap(CatUnit::getId, Function.identity()));
        Map<Long, CatProjectPhase> catProjectPhaseMap = catProjectPhaseRepository.findAll().stream()
                .collect(Collectors.toMap(CatProjectPhase::getId, Function.identity()));
        Map<Long, ProjectItem> projectItemMap = projectItemRepository.findAll().stream()
                .collect(Collectors.toMap(ProjectItem::getId, Function.identity()));

        return entities.stream()
                .map(entity -> toDto(entity, projectItemMap.get(entity.getProjectItemId()), catProjectPhaseMap.get(entity.getProjectPhaseId()), catUnitMap.get(entity.getUnitId()), projectTypeMap.get(entity.getProjectTypeId()), sysUserMap.get(entity.getCreatedByUserId()), sysUserMap.get(entity.getUpdatedByUserId())))
                .collect(Collectors.toList());
    }

    /**
     * Entity -> DTO
     * Các object con (ProjectItem, Phase, Unit, ProjectType, User) được truyền từ service
     * để tránh query trong mapper.
     */
    public CategoryWorkItemDto toDto(CategoryWorkItem entity,
                                     ProjectItem projectItem,
                                     CatProjectPhase projectPhase,
                                     CatUnit unit,
                                     ProjectType projectType,
                                     SysUser createdBy,
                                     SysUser updatedBy) {
        if (entity == null) {
            return null;
        }

        CategoryWorkItemDto dto = modelMapper.map(entity, CategoryWorkItemDto.class);

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
        if (createdBy != null) {
            dto.setCreatedBy(modelMapper.map(createdBy, UserDto.class));
        }
        if (updatedBy != null) {
            dto.setUpdatedBy(modelMapper.map(updatedBy, UserDto.class));
        }

        return dto;
    }

    /**
     * DTO -> Entity (tạo mới)
     */
    public CategoryWorkItem toEntity(CategoryWorkItemDto dto, SysUser currentUser) {
        if (dto == null) {
            return null;
        }

        CategoryWorkItem entity = modelMapper.map(dto, CategoryWorkItem.class);
        setSelectFields(dto, entity);

        // Thiết lập giá trị mặc định cho bản ghi mới
        entity.setIsDeleted(Boolean.FALSE);
        if (entity.getIsActive() == null) {
            entity.setIsActive(Boolean.TRUE);
        }
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedByUserId(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);

        return entity;
    }

    /**
     * Cập nhật Entity từ DTO (chỉnh sửa)
     * Giữ lại các trường audit quan trọng.
     */
    public void updateEntityFromDto(CategoryWorkItemDto dto, CategoryWorkItem entity, SysUser currentUser) {
        if (dto == null || entity == null) {
            return;
        }

        // Lưu lại các giá trị không được phép thay đổi
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        Long originalCreatedByUserId = entity.getCreatedByUserId();
        Boolean originalIsDeleted = entity.getIsDeleted();

        // Map các trường cơ bản
        modelMapper.map(dto, entity);

        // Khôi phục các trường audit
        entity.setCreatedAt(originalCreatedAt);
        entity.setCreatedByUserId(originalCreatedByUserId);
        entity.setIsDeleted(originalIsDeleted);

        // Set lại các field dạng ID từ DTO
        setSelectFields(dto, entity);

        // Cập nhật audit update
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedByUserId(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
    }

    /**
     * Gán các field ID từ DTO con sang Entity
     */
    private void setSelectFields(CategoryWorkItemDto dto, CategoryWorkItem entity) {
        if (dto.getProjectItem() != null) {
            entity.setProjectItemId(dto.getProjectItem().getId());
        } else {
            entity.setProjectItemId(null);
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
    }
}
