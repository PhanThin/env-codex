package vn.com.viettel.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.com.viettel.dto.OutstandingItemDto;
import vn.com.viettel.dto.PriorityEnum;
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.entities.OutstandingItem;
import vn.com.viettel.entities.ProjectItem;
import vn.com.viettel.entities.WorkItem;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.OutstandingItemService;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OutstandingItemServiceImpl implements OutstandingItemService {

    @Autowired
    private OutstandingItemRepository outstandingItemRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectItemRepository projectItemRepository;

    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private CatOutstandingTypeRepository outstandingTypeRepository;

    @Autowired
    private Translator translator;

    @Autowired
    private ModelMapper modelMapper;

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
        List<WorkItemDto> workItems = dto.getWorkItems();
        if (workItems == null || workItems.isEmpty()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("outstanding.workitem.required")
            );
        }

        for (WorkItemDto wiDto : workItems) {
            Long wiId = wiDto == null ? null : wiDto.getId();
            if (wiId == null) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("outstanding.workitem.id_required")
                );
            }

            WorkItem wi = workItemRepository.findById(wiId)
                    .orElseThrow(() -> new CustomException(
                            HttpStatus.NOT_FOUND.value(),
                            translator.getMessage("outstanding.workitem.notfound", wiId)
                    ));

            if (!Objects.equals(wi.getItemId(), itemId)) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST.value(),
                        translator.getMessage("outstanding.workitem.not_belong_to_item", wiId, itemId)
                );
            }
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
    }

    @Override
    public OutstandingItemDto getOutstandingItemById(Long id) {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("outstandingitem.id.null"));
        }
        OutstandingItem outstandingItem = outstandingItemRepository.findById(id).orElse(null);
        if (outstandingItem == null) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("outstandingitem.notFound", id));
        }
        // TODO mapper out standing
        return modelMapper.map(outstandingItem, OutstandingItemDto.class);
    }
}
