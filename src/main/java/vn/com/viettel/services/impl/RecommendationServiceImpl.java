package vn.com.viettel.services.impl;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.RecommendationDto;
import vn.com.viettel.dto.RecommendationPriorityEnum;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.RecommendationMapper;
import vn.com.viettel.minio.dto.ObjectFileDTO;
import vn.com.viettel.minio.services.FileService;
import vn.com.viettel.repositories.jpa.*;
import vn.com.viettel.services.RecommendationService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.*;

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
    private MessageSource messageSource;
    @Autowired
    private RecommendationWorkItemRepository recommendationWorkItemRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private FileService fileService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "deadline",
            "recommendationTitle",
            "priority",
            "status"
    );

    @Override
    @Transactional
    public RecommendationDto createRecommendation(RecommendationDto dto, MultipartFile[] files) throws CustomException {
        SysUser currentUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Nếu Principal là SysUser:
            if (authentication.getPrincipal() instanceof SysUser) {
                currentUser = (SysUser) authentication.getPrincipal();
            } else {
                currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            }
        }
        validate(dto);
        Recommendation entity = recommendationMapper.toEntity(dto, currentUser);
        recommendationRepository.save(entity);
        List<RecommendationWorkItem> recommendationWorkItemList = recommendationMapper.mapToDtoList(dto.getWorkItems(), entity.getId(), currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
        recommendationWorkItemRepository.saveAll(recommendationWorkItemList);

        if (files != null) {
            List<ObjectFileDTO> objectFileDTOList = fileService.uploadFiles(bucketName, "", files);

            for (ObjectFileDTO file : objectFileDTOList) {
                // Lưu thông tin file vào bảng Attachment (tùy thuộc vào cấu trúc entity của bạn)
                Attachment attachment = getAttachment(file, entity);
                attachmentRepository.save(attachment);
            }
        }

        return recommendationMapper.mapToDtoList(List.of(entity)).getFirst();
    }

    @NotNull
    private static Attachment getAttachment(ObjectFileDTO file, Recommendation entity) {
        Attachment attachment = new Attachment();
        attachment.setFileName(file.getFileName());
        attachment.setFileSize(file.getFileSize());
        attachment.setFilePath(file.getFilePath());
        attachment.setFileUrl(file.getLinkUrlPublic()); // Đường dẫn/tên file trên MinIO
        attachment.setReferenceId(entity.getId());
        attachment.setReferenceType("RECOMMENDATION");
        attachment.setUploadedAt(LocalDateTime.now());
        attachment.setUploadedBy(Constants.DEFAULT_USER_ID);
        return attachment;
    }

    @Override
    public Page<RecommendationDto> searchRecommendations(RecommendationSearchRequestDto request) throws CustomException {
        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        // --- Validate & chuẩn hóa sortBy ---
        String sortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            // Có thể dùng message bundle nếu muốn i18n
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    "sortBy không hợp lệ. Các giá trị được phép: " + ALLOWED_SORT_FIELDS
            );
        }

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

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Build specification theo điều kiện
        var spec = buildSpecification(request);

        Page<Recommendation> resultPage = recommendationRepository.findAll(spec, pageable);

        // Map sang DTO (tái sử dụng mapper hiện tại để enrich project, user, workItems...)
        List<RecommendationDto> dtoList = recommendationMapper.mapToDtoList(resultPage.getContent());

        return new PageImpl<>(dtoList, pageable, resultPage.getTotalElements());
    }

    private String msg(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, locale);
    }

    public void validate(RecommendationDto dto) throws CustomException {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.payload.null"));
        }
        if (StringUtils.isBlank(dto.getRecommendationTitle())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.title.required"));
        } else if (dto.getRecommendationTitle().length() > 250) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.title.length"));
        } else {
            Optional<Recommendation> recommendation = recommendationRepository.findByRecommendationTitle(dto.getRecommendationTitle());
            if (recommendation.isPresent()) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.title.duplicate", dto.getRecommendationTitle()));
            }
        }

        if (StringUtils.isBlank(dto.getContent())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.content.required"));
        } else if (dto.getContent().length() > 500) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.content.length"));
        }
        Long recTypeId = dto.getRecommendationType() == null ? null : dto.getRecommendationType().getId();
        if (recTypeId == null || !recommendationTypeRepo.existsById(recTypeId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.type.notfound"));
        }
        if (StringUtils.isBlank(dto.getPriority())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.priority.required"));
        } else {
            try {
                // Nếu muốn case-insensitive thì có thể dùng toUpperCase() trước
                RecommendationPriorityEnum.valueOf(dto.getPriority().toUpperCase());
            } catch (IllegalArgumentException ex) {
                // Không khớp với bất kỳ giá trị nào trong PriorityEnum
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.priority.invalid"));
            }
        }

        Long projectId = dto.getProject() == null ? null : dto.getProject().getId();
        if (projectId != null) {
            if (!projectRepo.existsById(projectId)) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.project.notfound", projectId));
            }

            Long itemId = dto.getItem() == null ? null : dto.getItem().getId();
            if (itemId == null) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.item.required"));
            }

            ProjectItem item = projectItemRepo.findById(itemId).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.item.notfound", itemId)));
            if (!Objects.equals(item.getProjectId(), projectId)) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.item.not_belong_to_project", itemId, projectId));
            }

            Long phaseId = dto.getPhase() == null ? null : dto.getPhase().getId();
            if (phaseId != null) {
                CatProjectPhase phase = phaseRepo.findById(phaseId).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.phase.notfound", phaseId)));
                if (!Objects.equals(phase.getProjectId(), projectId)) {
                    throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.phase.not_belong_to_project", phaseId, projectId));
                }
            }

            if (dto.getWorkItems() != null) {
                for (WorkItemDto wiDto : dto.getWorkItems()) {
                    Long wiId = wiDto == null ? null : wiDto.getId();
                    if (wiId == null) {
                        throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.workitem.id_required"));
                    }
                    WorkItem wi = workItemRepo.findById(wiId).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.workitem.notfound", wiId)));
                    if (!Objects.equals(wi.getItemId(), itemId)) {
                        throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("recommendation.workitem.not_belong_to_item", wiId, itemId));
                    }
                }
            }
        }

    }
}
