package vn.com.viettel.services.impl;

import jakarta.transaction.Transactional;
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
import vn.com.viettel.dto.CatUnitDto;
import vn.com.viettel.dto.CatUnitSearchRequestDto;
import vn.com.viettel.entities.CatUnit;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.CatUnitMapper;
import vn.com.viettel.repositories.jpa.CatUnitRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.CatUnitService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static vn.com.viettel.repositories.jpa.CatUnitSpecifications.buildSpecification;

@Service
public class CatUnitServiceImpl implements CatUnitService {

    @Autowired
    private CatUnitRepository catUnitRepository;

    @Autowired
    private CatUnitMapper catUnitMapper;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private Translator translator;

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "unitName", "unitName",
            "unitType", "unitType",
            "isActive", "isActive"
    );

    @Override
    @Transactional
    public CatUnitDto create(CatUnitDto dto) throws CustomException {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("catUnit.invalidRequest"));
        }

        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        String unitName = normalize(dto.getUnitName());
        if (StringUtils.isNotBlank(unitName)) {
            boolean exists = catUnitRepository.existsByUnitNameIgnoreCaseAndIsDeletedFalse(unitName);
            if (exists) {
                throw new CustomException(HttpStatus.CONFLICT.value(), translator.getMessage("catUnit.duplicate.unitName", unitName));
            }
        }

        CatUnit entity = catUnitMapper.toEntity(dto);
        entity.setUnitName(unitName);
        entity.setUnitType(normalize(dto.getUnitType()));

        entity.setCreatedBy(userId);
        entity.setCreatedAt(now);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(now);

        entity.setIsDeleted(false);
        entity.setIsActive(Boolean.TRUE.equals(dto.getIsActive()));

        CatUnit saved = catUnitRepository.save(entity);
        return catUnitMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CatUnitDto update(Long id, CatUnitDto dto) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("catUnit.invalidRequest"));
        }
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("catUnit.invalidRequest"));
        }

        CatUnit entity = catUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("catUnit.notFound", id)));

        String unitName = normalize(dto.getUnitName());
        if (StringUtils.isNotBlank(unitName)) {
            boolean exists = catUnitRepository.existsByUnitNameIgnoreCaseAndIdNotAndIsDeletedFalse(unitName, id);
            if (exists) {
                throw new CustomException(HttpStatus.CONFLICT.value(), translator.getMessage("catUnit.duplicate.unitName", unitName));
            }
        }

        catUnitMapper.updateEntityFromDto(dto, entity);
        entity.setUnitName(unitName);
        entity.setUnitType(normalize(dto.getUnitType()));
        entity.setIsActive(Boolean.TRUE.equals(dto.getIsActive()));

        SysUser currentUser = getCurrentUser();
        entity.setUpdatedBy(currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID);
        entity.setUpdatedAt(LocalDateTime.now());

        CatUnit saved = catUnitRepository.save(entity);
        return catUnitMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(List<Long> unitIds) throws CustomException {
        if (unitIds == null || unitIds.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("catUnit.invalidRequest"));
        }

        List<CatUnit> entities = catUnitRepository.findAllByIdInAndIsDeletedFalse(unitIds);
        if (entities == null || entities.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("catUnit.notFound", unitIds));
        }
        if (entities.size() != unitIds.size()) {
            List<Long> notFoundIds = unitIds.stream()
                    .filter(i -> entities.stream().noneMatch(e -> e.getId().equals(i)))
                    .toList();
            throw new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("catUnit.notFound", notFoundIds));
        }

        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        entities.forEach(e -> {
            e.setIsDeleted(true);
            e.setUpdatedBy(userId);
            e.setUpdatedAt(now);
        });
        catUnitRepository.saveAll(entities);
    }

    @Override
    public CatUnitDto getDetail(Long id) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("catUnit.invalidRequest"));
        }
        CatUnit entity = catUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("catUnit.notFound", id)));
        return catUnitMapper.toDto(entity);
    }

    @Override
    public Page<CatUnitDto> search(CatUnitSearchRequestDto request) throws CustomException {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        String sortBy = request != null ? StringUtils.defaultIfBlank(request.getSortBy(), "createdAt") : "createdAt";
        String sortProperty = ALLOWED_SORT_FIELDS.get(sortBy);
        if (sortProperty == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    "sortBy không hợp lệ. Các giá trị được phép: " + ALLOWED_SORT_FIELDS.keySet()
            );
        }

        String sortDirectionRaw = request != null ? StringUtils.defaultIfBlank(request.getSortDirection(), "DESC") : "DESC";
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

        var specification = buildSpecification(request);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        Page<CatUnit> resultPage = catUnitRepository.findAll(specification, pageRequest);
        List<CatUnitDto> dtoList = resultPage.getContent().stream().map(catUnitMapper::toDto).toList();

        return new PageImpl<>(dtoList, pageRequest, resultPage.getTotalElements());
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

    private String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return StringUtils.trim(value);
    }

    public List<CatUnitDto> findAllUnitType(String type) throws CustomException {
        List<CatUnit> entities = catUnitRepository.findAllByUnitTypeAndIsDeletedFalse(type);
        return entities.stream()
                .map(catUnitMapper::toDto)
                .toList();
    }
}
