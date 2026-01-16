package vn.com.viettel.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.dto.CategoryWorkItemDto;
import vn.com.viettel.dto.CategoryWorkItemSearchRequest;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.CategoryWorkItemMapper;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.CategoryWorkItemService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CategoryWorkItemServiceImpl implements CategoryWorkItemService {

    @Autowired
    private CategoryWorkItemMapper categoryWorkItemMapper;
    @Autowired
    private CategoryWorkItemRepository categoryWorkItemRepository;
    @Autowired
    private ProjectTypeRepository projectTypeRepository;
    @Autowired
    private CatProjectPhaseRepository catProjectPhaseRepository;
    @Autowired
    private ProjectItemRepository projectItemRepository;
    @Autowired
    private CatUnitRepository catUnitRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private Translator translator;
    @Autowired
    private WorkItemRepository workItemRepository;

    private static final Map<String, String> CATEGORY_WORK_ITEM_SORT_FIELDS = Map.ofEntries(
            Map.entry("createdAt", "createdAt"),
            Map.entry("categoryWorkItemCode", "categoryWorkItemCode"),
            Map.entry("categoryWorkItemName", "categoryWorkItemName"),
            Map.entry("isActive", "isActive"),
            Map.entry("projectType", "catProjectType.projectTypeName"),
            Map.entry("projectPhase", "catProjectPhase.phaseName"),
            Map.entry("projectItem", "catProjectItem.itemName"),
            Map.entry("unit", "catUnit.unitName")
    );

    @Transactional(readOnly = true)
    @Override
    public Page<CategoryWorkItemDto> searchCategoryWorkItem(CategoryWorkItemSearchRequest request) {
        if (request == null) {
            request = new CategoryWorkItemSearchRequest();
        }

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        String requestSortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = CATEGORY_WORK_ITEM_SORT_FIELDS.get(requestSortBy);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("common.sortType.invalid") + CATEGORY_WORK_ITEM_SORT_FIELDS.keySet()
            );
        }

        String sortDirectionRaw = StringUtils.defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("common.sortDirection.invalid")
            );
        }

        var specification = CategoryWorkItemSpecifications.buildSpecification(request);

        Sort sort = Sort.by(direction, sortProperty);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<CategoryWorkItem> entityPage = categoryWorkItemRepository.findAll(specification, pageRequest);

        List<CategoryWorkItemDto> dtoList = categoryWorkItemMapper.mapToDtos(entityPage.getContent());

        return new PageImpl<>(dtoList, pageRequest, entityPage.getTotalElements());
    }

    @Transactional
    @Override
    public CategoryWorkItemDto createCategoryWorkItem(CategoryWorkItemDto dto) {
        validate(dto, false);

        SysUser currentUser = getCurrentUser();

        CategoryWorkItem entity = categoryWorkItemMapper.toEntity(dto, currentUser);
        entity = categoryWorkItemRepository.save(entity);

        ProjectType projectType = projectTypeRepository.findByIdAndIsDeletedFalse(entity.getProjectTypeId()).orElse(null);
        CatProjectPhase phase = catProjectPhaseRepository.findByIdAndIsDeletedFalse(entity.getProjectPhaseId()).orElse(null);
        ProjectItem projectItem = projectItemRepository.findByIdAndIsDeletedFalse(entity.getProjectItemId()).orElse(null);
        CatUnit unit = catUnitRepository.findByIdAndIsDeletedFalse(entity.getUnitId()).orElse(null);

        return categoryWorkItemMapper.toDto(
                entity,
                projectItem,
                phase,
                unit,
                projectType,
                currentUser,
                null
        );
    }

    @Transactional
    @Override
    public CategoryWorkItemDto updateCategoryWorkItem(Long id, CategoryWorkItemDto dto) {
        if (id == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.id.null")
            );
        }
        dto.setId(id);

        CategoryWorkItem entity = categoryWorkItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("categoryWorkItem.notFound", id)
                ));

        validate(dto, true);

        SysUser currentUser = getCurrentUser();

        categoryWorkItemMapper.updateEntityFromDto(dto, entity, currentUser);
        entity = categoryWorkItemRepository.save(entity);

        ProjectType projectType = projectTypeRepository.findByIdAndIsDeletedFalse(entity.getProjectTypeId()).orElse(null);
        CatProjectPhase phase = catProjectPhaseRepository.findByIdAndIsDeletedFalse(entity.getProjectPhaseId()).orElse(null);
        ProjectItem projectItem = projectItemRepository.findByIdAndIsDeletedFalse(entity.getProjectItemId()).orElse(null);
        CatUnit unit = catUnitRepository.findByIdAndIsDeletedFalse(entity.getUnitId()).orElse(null);

        return categoryWorkItemMapper.toDto(
                entity,
                projectItem,
                phase,
                unit,
                projectType,
                entity.getCreatedByUserId() != null
                        ? sysUserRepository.findByIdAndIsDeletedFalse(entity.getCreatedByUserId()).orElse(null)
                        : null,
                currentUser
        );
    }

    /**
     * Validate dữ liệu khi tạo / cập nhật CategoryWorkItem.
     *
     * @param dto      payload gửi lên
     * @param isUpdate true nếu là update, false nếu là create
     */
    private void validate(CategoryWorkItemDto dto, boolean isUpdate) {
        if (dto == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.payload.null")
            );
        }

        // Loại dự án*
        if (dto.getProjectType() == null || dto.getProjectType().getId() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.projectType.required")
            );
        }
        ProjectType projectType = projectTypeRepository.findByIdAndIsDeletedFalse(dto.getProjectType().getId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("categoryWorkItem.projectType.notFound", dto.getProjectType().getId())
                ));

        // Giai đoạn*
        if (dto.getProjectPhase() == null || dto.getProjectPhase().getId() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.phase.required")
            );
        }
        CatProjectPhase phase = catProjectPhaseRepository.findByIdAndIsDeletedFalse(dto.getProjectPhase().getId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("categoryWorkItem.phase.notFound", dto.getProjectPhase().getId())
                ));

        // Hạng mục dự án*
        if (dto.getProjectItem() == null || dto.getProjectItem().getId() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.projectItem.required")
            );
        }
        ProjectItem projectItem = projectItemRepository.findByIdAndIsDeletedFalse(dto.getProjectItem().getId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("categoryWorkItem.projectItem.notFound", dto.getProjectItem().getId())
                ));

        // Đơn vị tính (không bắt buộc) – nếu gửi ID thì kiểm tra tồn tại
        if (dto.getUnit() != null && dto.getUnit().getId() != null) {
            Optional<CatUnit> unitOpt = catUnitRepository.findByIdAndIsDeletedFalse(dto.getUnit().getId());
            if (unitOpt.isEmpty()) {
                throw new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("categoryWorkItem.unit.notFound", dto.getUnit().getId())
                );
            }
        }

        // Mã hạng mục công việc*
        String code = dto.getCategoryWorkItemCode();
        if (StringUtils.isBlank(code)) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.code.required")
            );
        }
        code = code.trim();
        if (code.length() > 250) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.code.length", 250)
            );
        }

        Optional<CategoryWorkItem> codeExisting =
                categoryWorkItemRepository
                        .findFirstByProjectTypeIdAndProjectPhaseIdAndProjectItemIdAndCategoryWorkItemCodeIgnoreCaseAndIsDeletedFalse(
                                projectType.getId(),
                                phase.getId(),
                                projectItem.getId(),
                                code
                        );

        if (codeExisting.isPresent()) {
            if (!isUpdate || !codeExisting.get().getId().equals(dto.getId())) {
                throw new CustomException(
                        HttpStatus.CONFLICT.value(),
                        translator.getMessage("categoryWorkItem.code.duplicate")
                );
            }
        }

        // Tên hạng mục công việc*
        String name = dto.getCategoryWorkItemName();
        if (StringUtils.isBlank(name)) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.name.required")
            );
        }
        name = name.trim();
        if (name.length() > 250) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.name.length", 250)
            );
        }

        Optional<CategoryWorkItem> nameExisting =
                categoryWorkItemRepository
                        .findFirstByProjectTypeIdAndProjectPhaseIdAndProjectItemIdAndCategoryWorkItemNameIgnoreCaseAndIsDeletedFalse(
                                projectType.getId(),
                                phase.getId(),
                                projectItem.getId(),
                                name
                        );

        if (nameExisting.isPresent()) {
            if (!isUpdate || !nameExisting.get().getId().equals(dto.getId())) {
                throw new CustomException(
                        HttpStatus.CONFLICT.value(),
                        translator.getMessage("categoryWorkItem.name.duplicate")
                );
            }
        }

        // Ghi chú – không bắt buộc, <= 500
        if (dto.getNote() != null && dto.getNote().trim().length() > 500) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.note.length", 500)
            );
        }

        // Trạng thái hiệu lực* – bắt buộc
        if (dto.getIsActive() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.isActive.required")
            );
        }
    }

    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof SysUser user) {
                return user;
            }
            return sysUserRepository.findByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }

    @Transactional
    @Override
    public void deleteCategoryWorkItems(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.id.null")
            );
        }

        java.util.List<CategoryWorkItem> entities =
                categoryWorkItemRepository.findAllById(ids).stream()
                        .filter(e -> Boolean.FALSE.equals(e.getIsDeleted()))
                        .toList();

        if (entities.isEmpty()) {
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("categoryWorkItem.notFound", ids)
            );
        }

        // Nếu có id không tồn tại (hoặc đã deleted) thì báo riêng
        if (entities.size() != ids.size()) {
            java.util.List<Long> notFoundIds = ids.stream()
                    .filter(id -> entities.stream().noneMatch(e -> e.getId().equals(id)))
                    .toList();
            throw new CustomException(
                    HttpStatus.NOT_FOUND.value(),
                    translator.getMessage("categoryWorkItem.notFound", notFoundIds)
            );
        }

        // 1. Chỉ bản ghi hết hiệu lực (isActive = false) mới được xoá
        boolean hasActive = entities.stream()
                .anyMatch(e -> Boolean.TRUE.equals(e.getIsActive()));
        if (hasActive) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.delete.onlyInactive")
            );
        }

        // 2. Chỉ xoá khi tất cả WorkItem trong hạng mục đều hết hiệu lực
        java.util.List<Long> catIds = entities.stream()
                .map(CategoryWorkItem::getId)
                .toList();

        java.util.List<Long> catHasActiveWorkItem = catIds.stream()
                .filter(catId -> workItemRepository
                        .existsByCatWorkItemIdAndIsDeletedFalseAndIsActiveTrue(catId))
                .toList();

        if (!catHasActiveWorkItem.isEmpty()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.delete.workItem.notInactive")
            );
        }

        SysUser currentUser = getCurrentUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        entities.forEach(e -> {
            e.setIsDeleted(true);
            e.setUpdatedByUserId(currentUserId);
            e.setUpdatedAt(now);
        });

        categoryWorkItemRepository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryWorkItemDto getById(Long id) {
        CategoryWorkItem categoryWorkItem = categoryWorkItemRepository.findById(id).orElseThrow(() -> new CustomException(
                HttpStatus.NOT_FOUND.value(),
                translator.getMessage("categoryWorkItem.notFound")
        ));

        ProjectType projectType = projectTypeRepository.findByIdAndIsDeletedFalse(categoryWorkItem.getProjectTypeId()).orElse(null);
        CatProjectPhase phase = catProjectPhaseRepository.findByIdAndIsDeletedFalse(categoryWorkItem.getProjectPhaseId()).orElse(null);
        ProjectItem projectItem = projectItemRepository.findByIdAndIsDeletedFalse(categoryWorkItem.getProjectItemId()).orElse(null);
        CatUnit unit = catUnitRepository.findByIdAndIsDeletedFalse(categoryWorkItem.getUnitId()).orElse(null);

        return categoryWorkItemMapper.toDto(
                categoryWorkItem,
                projectItem,
                phase,
                unit,
                projectType,
                categoryWorkItem.getCreatedByUserId() != null
                        ? sysUserRepository.findByIdAndIsDeletedFalse(categoryWorkItem.getCreatedByUserId()).orElse(null)
                        : null,
                categoryWorkItem.getUpdatedByUserId() != null
                        ? sysUserRepository.findByIdAndIsDeletedFalse(categoryWorkItem.getUpdatedByUserId()).orElse(null)
                        : null
        );
    }
}
