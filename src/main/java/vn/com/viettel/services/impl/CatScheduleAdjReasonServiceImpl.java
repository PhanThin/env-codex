package vn.com.viettel.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.dto.CatScheduleAdjReasonDto;
import vn.com.viettel.dto.CatScheduleAdjReasonSearchRequestDto;
import vn.com.viettel.entities.CatScheduleAdjReason;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.CatScheduleAdjReasonMapper;
import vn.com.viettel.repositories.jpa.CatScheduleAdjReasonRepository;
import vn.com.viettel.repositories.jpa.CatScheduleAdjReasonSpecifications;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.CatScheduleAdjReasonService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CatScheduleAdjReasonServiceImpl implements CatScheduleAdjReasonService {

    private static final Pattern REASON_CODE_PATTERN = Pattern.compile("^TB-[A-Za-z0-9_-]+$");

    private static final Map<String, String> ALLOWED_SORT_FIELDS = new HashMap<>();

    static {
        ALLOWED_SORT_FIELDS.put("reasonCode", "reasonCode");
        ALLOWED_SORT_FIELDS.put("reasonName", "reasonName");
        ALLOWED_SORT_FIELDS.put("createdAt", "createdAt");
        ALLOWED_SORT_FIELDS.put("updatedAt", "updatedAt");
        ALLOWED_SORT_FIELDS.put("isActive", "isActive");
    }

    @Autowired
    private CatScheduleAdjReasonRepository repository;

    @Autowired
    private CatScheduleAdjReasonMapper mapper;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private Translator translator;

    @Override
    @Transactional
    public CatScheduleAdjReasonDto create(CatScheduleAdjReasonDto dto) throws CustomException {
        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        normalize(dto);
//        validateReasonCodeFormat(dto.getReasonCode());

        String reasonCode = dto.getReasonCode();
        String reasonName = dto.getReasonName();

        // Default isActive
        Boolean isActive = dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE;

        if (repository.existsByReasonCodeAndIsDeletedFalse(reasonCode)) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("catScheduleAdjReason.duplicate.reasonCode"));
        }
        if (repository.existsByReasonNameIgnoreCaseAndTrimAndIsDeletedFalse(reasonName)) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("catScheduleAdjReason.duplicate.reasonName"));
        }

        CatScheduleAdjReason entity = mapper.toEntity(dto);
        entity.setReasonId(null); // id is DB generated
        entity.setIsActive(isActive);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setCreatedBy(userId);
        entity.setCreatedAt(now);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(now);

        CatScheduleAdjReason saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public CatScheduleAdjReasonDto update(Long id, CatScheduleAdjReasonDto dto) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("catScheduleAdjReason.notFound"));
        }

        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        normalize(dto);
//        validateReasonCodeFormat(dto.getReasonCode());

        CatScheduleAdjReason entity = repository.findByReasonIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("catScheduleAdjReason.notFound")));

        String reasonCode = dto.getReasonCode();
        String reasonName = dto.getReasonName();

        if (repository.existsByReasonCodeAndIsDeletedFalseAndReasonIdNot(reasonCode, id)) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("catScheduleAdjReason.duplicate.reasonCode"));
        }
        if (repository.existsByReasonNameIgnoreCaseAndTrimAndIsDeletedFalseAndReasonIdNot(reasonName, id)) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("catScheduleAdjReason.duplicate.reasonName"));
        }

        // do not update createdBy/createdAt
        mapper.updateEntityFromDto(dto, entity);

        if (dto.getIsActive() == null) {
            entity.setIsActive(Boolean.TRUE);
        }

        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(now);

        CatScheduleAdjReason saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(List<Long> reasonIds) throws CustomException {
        if (reasonIds == null || reasonIds.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("catScheduleAdjReason.invalid.reasonIds"));
        }

        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        for (Long id : reasonIds) {
            if (id == null) {
                continue;
            }
            CatScheduleAdjReason entity = repository.findByReasonIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("catScheduleAdjReason.notFound")));
            entity.setIsDeleted(Boolean.TRUE);
            entity.setUpdatedBy(userId);
            entity.setUpdatedAt(now);
            repository.save(entity);
        }
    }

    @Override
    public CatScheduleAdjReasonDto getDetail(Long id) throws CustomException {
        CatScheduleAdjReason entity = repository.findByReasonIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("catScheduleAdjReason.notFound")));
        return mapper.toDto(entity);
    }

    @Override
    public Page<CatScheduleAdjReasonDto> search(CatScheduleAdjReasonSearchRequestDto request) throws CustomException {
        if (request == null) {
            request = new CatScheduleAdjReasonSearchRequestDto();
        }

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        String sortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = ALLOWED_SORT_FIELDS.get(sortBy);
        if (sortProperty == null) {
            sortProperty = "createdAt";
        }

        String sortDirectionRaw = StringUtils.defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirectionRaw) ? Sort.Direction.ASC : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        var specification = CatScheduleAdjReasonSpecifications.buildSpecification(request);

        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    private void normalize(CatScheduleAdjReasonDto dto) {
        if (dto == null) {
            return;
        }
        dto.setReasonCode(StringUtils.defaultString(dto.getReasonCode()));
        dto.setReasonCode(StringUtils.trim(dto.getReasonCode()));

        if (dto.getReasonName() != null) {
            dto.setReasonName(StringUtils.trim(dto.getReasonName()));
        }
        if (dto.getNote() != null) {
            dto.setNote(StringUtils.trim(dto.getNote()));
        }
    }

    private void validateReasonCodeFormat(String reasonCode) throws CustomException {
        if (StringUtils.isBlank(reasonCode)) {
            return; // let @NotBlank handle
        }
        if (!REASON_CODE_PATTERN.matcher(reasonCode).matches()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("catScheduleAdjReason.invalid.reasonCodeFormat"));
        }
    }

    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof SysUser user) {
                return user;
            }
            return userRepository.findByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }

    private String msg(String key, Object... params) {
        return translator.getMessage(key, params);
    }
}
