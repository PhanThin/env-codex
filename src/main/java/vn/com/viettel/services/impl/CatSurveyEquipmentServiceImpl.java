package vn.com.viettel.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.com.viettel.dto.CatSurveyEquipmentDto;
import vn.com.viettel.dto.CatSurveyEquipmentSearchRequestDto;
import vn.com.viettel.entities.CatSurveyEquipment;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.CatSurveyEquipmentMapper;
import vn.com.viettel.repositories.jpa.CatSurveyEquipmentRepository;
import vn.com.viettel.repositories.jpa.CatSurveyEquipmentSpecifications;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.CatSurveyEquipmentService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CatSurveyEquipmentServiceImpl implements CatSurveyEquipmentService {

    @Autowired
    private CatSurveyEquipmentRepository repository;

    @Autowired
    private CatSurveyEquipmentMapper mapper;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private Translator translator;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "equipmentCode", "equipmentCode",
            "equipmentName", "equipmentName",
            "modelCode", "modelCode",
            "manufactureYear", "manufactureYear",
            "manageUnitId", "manageUnitId",
            "isActive", "isActive"
    );

    @Override
    @Transactional
    public CatSurveyEquipmentDto create(CatSurveyEquipmentDto dto) throws CustomException {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.payload.null"));
        }

        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        validate(dto, null);

        CatSurveyEquipment entity = mapper.toEntity(dto);

        entity.setCreatedAt(now);
        entity.setCreatedBy(userId);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(userId);
        entity.setIsDeleted("N");

        // default IS_ACTIVE = 'Y' nếu client không gửi
        entity.setIsActive(StringUtils.defaultIfBlank(dto.getIsActive(), "Y").trim().toUpperCase());

        repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public CatSurveyEquipmentDto update(Long id, CatSurveyEquipmentDto dto) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.id.null"));
        }
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.payload.null"));
        }

        CatSurveyEquipment entity = repository.findByEquipmentIdAndIsDeleted(id, "N")
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("surveyEquipment.notFound", id)));

        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        validate(dto, id);

        mapper.updateEntityFromDto(dto, entity);

        entity.setUpdatedAt(now);
        entity.setUpdatedBy(userId);

        // isActive: nếu null thì giữ nguyên
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive().trim().toUpperCase());
        }

        repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public void delete(List<Long> equipmentIds) throws CustomException {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.id.list.null"));
        }

        List<CatSurveyEquipment> entities = repository.findAllByEquipmentIdInAndIsDeleted(equipmentIds, "N");
        if (entities == null || entities.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), msg("surveyEquipment.notFound", equipmentIds));
        }
        if (entities.size() != equipmentIds.size()) {
            List<Long> notFoundIds = equipmentIds.stream()
                    .filter(id -> entities.stream().noneMatch(e -> id.equals(e.getEquipmentId())))
                    .toList();
            throw new CustomException(HttpStatus.NOT_FOUND.value(), msg("surveyEquipment.notFound", notFoundIds));
        }

        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        entities.forEach(e -> {
            e.setIsDeleted("Y");
            e.setUpdatedAt(now);
            e.setUpdatedBy(userId);
        });

        repository.saveAll(entities);
    }

    @Override
    public Page<CatSurveyEquipmentDto> search(CatSurveyEquipmentSearchRequestDto request) throws CustomException {
        if (request == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.payload.null"));
        }

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        // sortBy
        String sortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = ALLOWED_SORT_FIELDS.get(sortBy);
        if (sortProperty == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.search.sortBy.invalid", ALLOWED_SORT_FIELDS.keySet()));
        }

        // sortDirection
        String sortDirectionRaw = StringUtils.defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.search.sortDirection.invalid"));
        }

        // validate isActive
        String isActive = StringUtils.defaultIfBlank(request.getIsActive(), "Y").trim().toUpperCase();
        if (!"Y".equals(isActive) && !"N".equals(isActive)) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.isActive.invalid"));
        }
        request.setIsActive(isActive);

        Sort sort = Sort.by(direction, sortProperty);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        var spec = CatSurveyEquipmentSpecifications.buildSpecification(request);
        Page<CatSurveyEquipment> result = repository.findAll(spec, pageRequest);

        List<CatSurveyEquipmentDto> dtoList = result.getContent().stream().map(mapper::toDto).toList();
        return new PageImpl<>(dtoList, pageRequest, result.getTotalElements());
    }

    private void validate(CatSurveyEquipmentDto dto, Long idForUpdate) throws CustomException {
        // REQUIRED: code/name/model/manageUnitId
        if (StringUtils.isBlank(dto.getEquipmentCode())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.code.required"));
        }
        if (dto.getEquipmentCode().trim().length() > 1000) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.code.length"));
        }

        if (StringUtils.isBlank(dto.getEquipmentName())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.name.required"));
        }
        if (dto.getEquipmentName().trim().length() > 1000) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.name.length"));
        }

        if (StringUtils.isBlank(dto.getModelCode())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.model.required"));
        }
        if (dto.getModelCode().trim().length() > 1000) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.model.length"));
        }

        if (dto.getManageUnitId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.manageUnitId.required"));
        }

        // CHECK: isActive
        String isActive = StringUtils.defaultIfBlank(dto.getIsActive(), "Y").trim().toUpperCase();
        if (!"Y".equals(isActive) && !"N".equals(isActive)) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.isActive.invalid"));
        }

        // optional lengths
        if (dto.getManufacturerName() != null && dto.getManufacturerName().trim().length() > 1000) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.manufacturerName.length"));
        }
        if (dto.getUomName() != null && dto.getUomName().trim().length() > 200) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.uomName.length"));
        }
        if (dto.getManageUnitName() != null && dto.getManageUnitName().trim().length() > 1000) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.manageUnitName.length"));
        }
        if (dto.getNote() != null && dto.getNote().trim().length() > 2000) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.note.length"));
        }

        if (dto.getManufactureYear() != null) {
            int year = dto.getManufactureYear();
            if (year < 0 || year > 9999) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.manufactureYear.invalid"));
            }
        }

        // UNIQUE: EQUIPMENT_CODE (UPPER(TRIM))
        String normalizedCode = normalize(dto.getEquipmentCode());
        boolean duplicateCode = (idForUpdate == null)
                ? repository.existsActiveByNormalizedEquipmentCode(normalizedCode)
                : repository.existsActiveByNormalizedEquipmentCodeAndNotId(normalizedCode, idForUpdate);
        if (duplicateCode) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("surveyEquipment.code.duplicate", dto.getEquipmentCode().trim()));
        }

        // UNIQUE: EQUIPMENT_NAME when IS_DELETED='N'
        String normalizedName = normalize(dto.getEquipmentName());
        boolean duplicateName = (idForUpdate == null)
                ? repository.existsActiveByNormalizedEquipmentName(normalizedName)
                : repository.existsActiveByNormalizedEquipmentNameAndNotId(normalizedName, idForUpdate);
        if (duplicateName) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("surveyEquipment.name.duplicate", dto.getEquipmentName().trim()));
        }

        // FK: MANUFACTURER_ID tồn tại (nếu có)
        if (dto.getManufacturerId() != null) {
            if (!existsManufacturer(dto.getManufacturerId())) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.manufacturerId.notFound", dto.getManufacturerId()));
            }
        }

        // FK: MANAGE_UNIT_ID tồn tại
        if (!existsManageUnit(dto.getManageUnitId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.manageUnitId.notFound", dto.getManageUnitId()));
        }
    }

    private boolean existsManufacturer(Long manufacturerId) {
        try {
            Object result = entityManager
                    .createNativeQuery("SELECT COUNT(1) FROM CAT_MANUFACTURER WHERE MANUFACTURER_ID = :id")
                    .setParameter("id", manufacturerId)
                    .getSingleResult();
            return toLong(result) > 0;
        } catch (Exception ex) {
            // Nếu hệ thống đã có entity/repo cho CAT_MANUFACTURER, bạn có thể thay bằng repo.existsById(...)
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg("database.error"));
        }
    }

    private boolean existsManageUnit(Long manageUnitId) {
        try {
            Object result = entityManager
                    .createNativeQuery("SELECT COUNT(1) FROM SYS_ORG WHERE ORG_ID = :id")
                    .setParameter("id", manageUnitId)
                    .getSingleResult();
            return toLong(result) > 0;
        } catch (Exception ex) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg("database.error"));
        }
    }

    private long toLong(Object result) {
        if (result == null) {
            return 0L;
        }
        if (result instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(result));
        } catch (Exception e) {
            return 0L;
        }
    }

    private String normalize(String input) {
        return StringUtils.defaultString(input).trim().toUpperCase();
    }

    private String msg(String key, Object... params) {
        return translator.getMessage(key, params);
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
}
