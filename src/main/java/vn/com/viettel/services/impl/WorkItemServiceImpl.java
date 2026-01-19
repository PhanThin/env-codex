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
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.dto.WorkItemSearchRequest;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.WorkItemMapper;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.WorkItemService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class WorkItemServiceImpl implements WorkItemService {
    @Autowired
    private WorkItemRepository workItemRepository;
    @Autowired
    private WorkItemMapper workItemMapper;
    @Autowired
    private Translator translator;
    @Autowired
    private ProjectItemRepository projectItemRepository;
    @Autowired
    private CatProjectPhaseRepository catProjectPhaseRepository;
    @Autowired
    private CategoryWorkItemRepository categoryWorkItemRepository;
    @Autowired
    private CatUnitRepository catUnitRepository;
    @Autowired
    private ProjectTypeRepository projectTypeRepository;
    @Autowired
    private SysUserRepository sysUserRepository;

    private static final Map<String, String> WORK_ITEM_SORT_FIELDS = Map.ofEntries(
            Map.entry("createdAt", "createdAt"),
            Map.entry("categoryWorkItemCode", "categoryWorkItemCode"),
            Map.entry("categoryWorkItemName", "categoryWorkItemName"),
            Map.entry("isActive", "isActive"),
            Map.entry("projectType", "projectType.projectTypeName"),
            Map.entry("projectPhase", "projectPhase.phaseName"),
            Map.entry("projectItem", "projectItem.itemName"),
            Map.entry("categoryWorkItem", "categoryWorkItem.categoryWorkItemName"),
            Map.entry("unit", "unit.unitName")
    );

    @Transactional(readOnly = true)
    @Override
    public Page<WorkItemDto> search(WorkItemSearchRequest request) {
        if (request == null) {
            request = new WorkItemSearchRequest();
        }

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        String requestSortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = WORK_ITEM_SORT_FIELDS.get(requestSortBy);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("common.sortType.invalid") + WORK_ITEM_SORT_FIELDS.keySet()
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

        var specification = WorkItemSpecifications.buildSpecification(request);

        Sort sort = Sort.by(direction, sortProperty);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<WorkItem> entityPage = workItemRepository.findAll(specification, pageRequest);

        List<WorkItemDto> dtoList = workItemMapper.mapToDtos(entityPage.getContent());

        return new PageImpl<>(dtoList, pageRequest, entityPage.getTotalElements());
    }

    @Override
    public WorkItemDto create(WorkItemDto dto) {
        validate(dto, false);

        SysUser currentUser = getCurrentUser();

        WorkItem entity = workItemMapper.toEntity(dto, currentUser);
        entity = workItemRepository.save(entity);

        ProjectType projectType = projectTypeRepository.findByIdAndIsDeletedFalse(entity.getProjectTypeId()).orElse(null);
        CatProjectPhase phase = catProjectPhaseRepository.findByIdAndIsDeletedFalse(entity.getProjectPhaseId()).orElse(null);
        ProjectItem projectItem = projectItemRepository.findByIdAndIsDeletedFalse(entity.getItemId()).orElse(null);
        CatUnit unit = catUnitRepository.findByIdAndIsDeletedFalse(entity.getUnitId()).orElse(null);
        CategoryWorkItem categoryWorkItem = categoryWorkItemRepository.findByIdAndIsDeletedFalse(entity.getCatWorkItemId()).orElse(null);
        return workItemMapper.toDto(entity, projectItem, phase, unit, projectType, categoryWorkItem, currentUser, null);
    }

    @Override
    public WorkItemDto update(Long id, WorkItemDto dto) {
        if (id == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.id.null")
            );
        }
        dto.setId(id);

        WorkItem entity = getOrThrow(id);
        validate(dto, true);

        SysUser currentUser = getCurrentUser();

        workItemMapper.updateEntityFromDto(dto, entity, currentUser);
        entity = workItemRepository.save(entity);

        ProjectType projectType = projectTypeRepository.findByIdAndIsDeletedFalse(entity.getProjectTypeId()).orElse(null);
        CatProjectPhase phase = catProjectPhaseRepository.findByIdAndIsDeletedFalse(entity.getProjectPhaseId()).orElse(null);
        ProjectItem projectItem = projectItemRepository.findByIdAndIsDeletedFalse(entity.getItemId()).orElse(null);
        CatUnit unit = catUnitRepository.findByIdAndIsDeletedFalse(entity.getUnitId()).orElse(null);
        CategoryWorkItem categoryWorkItem = categoryWorkItemRepository.findByIdAndIsDeletedFalse(entity.getCatWorkItemId()).orElse(null);
        return workItemMapper.toDto(entity, projectItem, phase, unit, projectType, categoryWorkItem,
                entity.getCreatedByUserId() != null ? sysUserRepository.findByIdAndIsDeletedFalse(entity.getCreatedByUserId()).orElse(null) : null,
                currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkItemDto getById(Long id) {
        if (id == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.id.null")
            );
        }
        WorkItem entity = getOrThrow(id);
        ProjectType projectType = projectTypeRepository.findByIdAndIsDeletedFalse(entity.getProjectTypeId()).orElse(null);
        CatProjectPhase phase = catProjectPhaseRepository.findByIdAndIsDeletedFalse(entity.getProjectPhaseId()).orElse(null);
        ProjectItem projectItem = projectItemRepository.findByIdAndIsDeletedFalse(entity.getItemId()).orElse(null);
        CatUnit unit = catUnitRepository.findByIdAndIsDeletedFalse(entity.getUnitId()).orElse(null);
        CategoryWorkItem categoryWorkItem = categoryWorkItemRepository.findByIdAndIsDeletedFalse(entity.getCatWorkItemId()).orElse(null);

        return workItemMapper.toDto(entity, projectItem, phase, unit, projectType, categoryWorkItem,
                entity.getCreatedByUserId() != null ? sysUserRepository.findByIdAndIsDeletedFalse(entity.getCreatedByUserId()).orElse(null) : null,
                entity.getUpdatedByUserId() != null ? sysUserRepository.findByIdAndIsDeletedFalse(entity.getUpdatedByUserId()).orElse(null) : null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkItemDto> getAllByItemId(Long itemId) {
        return workItemRepository.findAllByItemIdAndIsDeletedFalse(itemId)
                .stream().map(entity -> workItemMapper.toDto(entity, null, null, null, null, null, null, null)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.id.null")
            );
        }

        List<WorkItem> entities =
                workItemRepository.findAllByIdInAndIsDeletedFalse(ids);

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

        SysUser currentUser = getCurrentUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        entities.forEach(e -> {
            e.setIsDeleted(true);
            e.setUpdatedByUserId(currentUserId);
            e.setUpdatedAt(now);
        });

        workItemRepository.saveAll(entities);
    }

    private void validate(WorkItemDto dto, boolean isUpdate) {
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
//        if (dto.getProjectItem() == null || dto.getProjectItem().getId() == null) {
//            throw new CustomException(
//                    HttpStatus.BAD_REQUEST.value(),
//                    translator.getMessage("categoryWorkItem.projectItem.required")
//            );
//        }
        if (dto.getProjectItem() != null && dto.getProjectItem().getId() != null) {
            ProjectItem projectItem = projectItemRepository.findByIdAndIsDeletedFalse(dto.getProjectItem().getId())
                    .orElseThrow(() -> new CustomException(
                            HttpStatus.NOT_FOUND.value(),
                            translator.getMessage("categoryWorkItem.projectItem.notFound", dto.getProjectItem().getId())
                    ));
        }
        //Hạng mục công việc*
        if (dto.getCategoryWorkItem() == null || dto.getCategoryWorkItem().getId() == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("categoryWorkItem.categoryWorkItem.required")
            );
        }
        CategoryWorkItem categoryWorkItem = categoryWorkItemRepository.findByIdAndIsDeletedFalse(dto.getCategoryWorkItem().getId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("categoryWorkItem.categoryWorkItem.notFound", dto.getCategoryWorkItem().getId())
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

        // Mã công việc*
        String code = dto.getWorkItemCode();
        if (org.apache.commons.lang3.StringUtils.isBlank(code)) {
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

        Optional<WorkItem> codeExisting =
                workItemRepository
                        .findFirstByProjectTypeIdAndProjectPhaseIdAndCatWorkItemIdAndWorkItemCodeIgnoreCaseAndIsDeletedFalse(
                                projectType.getId(),
                                phase.getId(),
                                categoryWorkItem.getId(),
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
        String name = dto.getWorkItemName();
        if (org.apache.commons.lang3.StringUtils.isBlank(name)) {
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

        Optional<WorkItem> nameExisting =
                workItemRepository
                        .findFirstByProjectTypeIdAndProjectPhaseIdAndCatWorkItemIdAndWorkItemNameIgnoreCaseAndIsDeletedFalse(
                                projectType.getId(),
                                phase.getId(),
                                categoryWorkItem.getId(),
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

    private WorkItem getOrThrow(Long id) {
        return workItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("workitem.notfound", id)
                ));
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
}
