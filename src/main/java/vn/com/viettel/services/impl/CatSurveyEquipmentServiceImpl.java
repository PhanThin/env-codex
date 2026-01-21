package vn.com.viettel.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import vn.com.viettel.dto.*;
import vn.com.viettel.entities.*;
import vn.com.viettel.mapper.CatSurveyEquipmentMapper;
import vn.com.viettel.repositories.jpa.CatManufacturerRepository;
import vn.com.viettel.repositories.jpa.CatSurveyEquipmentRepository;
import vn.com.viettel.repositories.jpa.CatSurveyEquipmentSpecifications;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.CatSurveyEquipmentService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class CatSurveyEquipmentServiceImpl implements CatSurveyEquipmentService {

    @Autowired
    private CatSurveyEquipmentRepository repository;

    @Autowired
    private CatManufacturerRepository catManufacturerRepository;
    @Autowired
    private CatSurveyEquipmentMapper mapper;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private Translator translator;

    private final org.modelmapper.ModelMapper modelMapper;
    private final vn.com.viettel.repositories.jpa.SysOrgRepository sysOrgRepo;

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
        entity.setIsDeleted(Boolean.FALSE);
        if (entity.getIsActive() == null) {
            entity.setIsActive(Boolean.TRUE);
        }
        CatSurveyEquipment saved = repository.save(entity);
        CatSurveyEquipmentDto dto2 = mapper.toDto(saved);
        enrichCreatedUpdatedUsers(List.of(saved), List.of(dto2));
        return dto2;

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

        CatSurveyEquipment entity = repository.findByEquipmentIdAndIsDeleted(id, false)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("surveyEquipment.notFound", id)));

        SysUser currentUser = getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
        LocalDateTime now = LocalDateTime.now();

        validate(dto, id);

        mapper.updateEntityFromDto(dto, entity);

        entity.setUpdatedAt(now);
        entity.setUpdatedBy(userId);

        // isActive: nếu null thì giữ nguyên
        if (entity.getIsActive() == null) {
            entity.setIsActive(Boolean.TRUE);
        }

        CatSurveyEquipment saved = repository.save(entity);
        CatSurveyEquipmentDto dto2 = mapper.toDto(saved);
        enrichCreatedUpdatedUsers(List.of(saved), List.of(dto2));
        return dto2;

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
            e.setIsDeleted(Boolean.TRUE);
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
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    msg("surveyEquipment.search.sortBy.invalid", ALLOWED_SORT_FIELDS.keySet())
            );
        }

        // sortDirection
        String sortDirectionRaw = StringUtils.defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    msg("surveyEquipment.search.sortDirection.invalid")
            );
        }

        // validate isActive
        Boolean isActive = request.getIsActive();

        if (isActive == null) {
            isActive = Boolean.TRUE; // default = true
        }

        request.setIsActive(isActive);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        var spec = CatSurveyEquipmentSpecifications.buildSpecification(request);

        // 1) Query Page<Entity>
        Page<CatSurveyEquipment> resultPage = repository.findAll(spec, pageRequest);

        // 2) Convert -> DTO list
        List<CatSurveyEquipment> entities = resultPage.getContent();
        List<CatSurveyEquipmentDto> dtoList = entities.stream()
                .map(mapper::toDto)
                .toList();

        // 3) Enrich user info (created/updated) trước khi trả ra
        enrichCreatedUpdatedUsers(entities, dtoList);

        // 4) Wrap lại Page<DTO>
        return new PageImpl<>(dtoList, pageRequest, resultPage.getTotalElements());
    }

    @Override
    public CatSurveyEquipmentDto getDetail(Long id) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("surveyEquipment.id.null"));
        }

        CatSurveyEquipment entity = repository.findByEquipmentIdAndIsDeleted(id, false)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND.value(), msg("surveyEquipment.notFound", id)));

        CatSurveyEquipmentDto dto = mapper.toDto(entity);

        // vì method enrich hiện đang nhận list
        enrichCreatedUpdatedUsers(List.of(entity), List.of(dto));

        return dto;
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
        return catManufacturerRepository
                .findByManufacturerIdAndIsDeletedFalse(manufacturerId)
                .isPresent();
    }

    private boolean existsManageUnit(Long manageUnitId) {
        return sysOrgRepo.findById(manageUnitId)
                .map(org -> {
                    // tuỳ entity SysOrg của bạn dùng isDeleted = Boolean / Integer / String
                    try { return org.getIsDeleted() == null || !org.getIsDeleted(); } catch (Exception ignore) {}
                    return true; // fallback nếu không có field isDeleted
                })
                .orElse(false);
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
    private UserDto mapUserDto(SysUser sysUser, Map<Long, SysOrg> sysOrgMap) {
        if (sysUser == null) return null;
        UserDto userDto = modelMapper.map(sysUser, UserDto.class);

        // giống RecommendationMapper: gắn org vào user
        if (sysOrgMap != null && sysUser.getOrgId() != null && sysOrgMap.containsKey(sysUser.getOrgId())) {
            userDto.setOrg(modelMapper.map(sysOrgMap.get(sysUser.getOrgId()), OrgDto.class));
        }
        return userDto;
    }

    private void enrichCreatedUpdatedUsers(List<CatSurveyEquipment> entities, List<CatSurveyEquipmentDto> dtos) {
        if (entities == null || entities.isEmpty() || dtos == null || dtos.isEmpty()) return;

        // lấy danh sách userId cần dùng (tránh N+1)
        Set<Long> userIds = new HashSet<>();
        for (CatSurveyEquipment e : entities) {
            if (e.getCreatedBy() != null) userIds.add(e.getCreatedBy());
            if (e.getUpdatedBy() != null) userIds.add(e.getUpdatedBy());
        }

        // load user map
        Map<Long, SysUser> userMap;
        if (userIds.isEmpty()) {
            userMap = Collections.emptyMap();
        } else {
            // Nếu repo có findAllByIdInAndIsDeletedFalse thì dùng cái đó là tốt nhất
            // userMap = userRepository.findAllByIdInAndIsDeletedFalse(new ArrayList<>(userIds))...
            userMap = userRepository.findAllById(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        // load org map (nếu muốn giống Recommendation)
        Map<Long, SysOrg> orgMap = sysOrgRepo.findAllByIsDeletedFalse().stream()
                .collect(Collectors.toMap(SysOrg::getId, o -> o));

        // enrich theo đúng index (dtos tạo từ entities theo order)
        for (int i = 0; i < entities.size(); i++) {
            CatSurveyEquipment e = entities.get(i);
            CatSurveyEquipmentDto dto = dtos.get(i);

            if (e.getCreatedBy() != null && userMap.containsKey(e.getCreatedBy())) {
                dto.setCreatedByUser(mapUserDto(userMap.get(e.getCreatedBy()), orgMap));
            }
            if (e.getUpdatedBy() != null && userMap.containsKey(e.getUpdatedBy())) {
                dto.setUpdatedByUser(mapUserDto(userMap.get(e.getUpdatedBy()), orgMap));
            }
        }
    }

    @Override
    public List<CatSurveyEquipmentDto> getAll(String sortBy, String sortDir) {
        String sortField = (sortBy == null || sortBy.trim().isEmpty()) ? "updatedAt" : sortBy.trim();
        String direction = (sortDir == null || sortDir.trim().isEmpty()) ? "desc" : sortDir.trim();

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        List<CatSurveyEquipment> entities = repository.findAllByIsDeletedFalse(sort);
        List<CatSurveyEquipmentDto> dtos = entities.stream().map(mapper::toDto).toList();
        enrichCreatedUpdatedUsers(entities, dtos);
        return dtos;
    }

}
