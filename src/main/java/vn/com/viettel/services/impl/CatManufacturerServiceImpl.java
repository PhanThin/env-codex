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
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import vn.com.viettel.dto.*;
import vn.com.viettel.entities.CatManufacturer;
import vn.com.viettel.entities.ProjectType;
import vn.com.viettel.entities.SysOrg;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.CatManufacturerMapper;
import vn.com.viettel.repositories.jpa.CatManufacturerRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.CatManufacturerService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


import static vn.com.viettel.repositories.jpa.CatManufacturerSpecifications.buildSpecification;

@Service
@RequiredArgsConstructor
public class CatManufacturerServiceImpl implements CatManufacturerService {

    @Autowired
    private CatManufacturerRepository repository;
    @Autowired
    private CatManufacturerMapper mapper;
    @Autowired
    private SysUserRepository userRepository;
    @Autowired
    private Translator translator;
    private final CatManufacturerRepository catManufacturerRepository;
    private final CatManufacturerMapper catManufacturerMapper;
    private final org.modelmapper.ModelMapper modelMapper;
    private final vn.com.viettel.repositories.jpa.SysOrgRepository sysOrgRepo;

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "manufacturerCode", "manufacturerCode",
            "manufacturerName", "manufacturerName",
            "country", "country"
    );

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

    private Long getCurrentUserId() {
        SysUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;
    }

    private void validateDuplicateForCreate(CatManufacturerDto dto) {
        if (repository.existsActiveByCode(dto.getManufacturerCode())) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("catManufacturer.duplicate.code", dto.getManufacturerCode()));
        }
        if (repository.existsActiveByName(dto.getManufacturerName())) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("catManufacturer.duplicate.name", dto.getManufacturerName()));
        }
    }

    private void validateDuplicateForUpdate(Long id, CatManufacturerDto dto) {
        if (repository.existsActiveByCodeAndNotId(id, dto.getManufacturerCode())) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("catManufacturer.duplicate.code", dto.getManufacturerCode()));
        }
        if (repository.existsActiveByNameAndNotId(id, dto.getManufacturerName())) {
            throw new CustomException(HttpStatus.CONFLICT.value(), msg("catManufacturer.duplicate.name", dto.getManufacturerName()));
        }
    }

    @Override
    @Transactional
    public CatManufacturerDto create(CatManufacturerDto dto) throws CustomException {
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("INVALID_REQUEST"));
        }
        if (StringUtils.isBlank(dto.getManufacturerCode()) || StringUtils.isBlank(dto.getManufacturerName())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("VALIDATION_ERROR"));
        }

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        validateDuplicateForCreate(dto);

        CatManufacturer entity = mapper.toEntity(dto);
        entity.setManufacturerId(repository.getNextId());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setIsDeleted(Boolean.FALSE);
        entity.setIsActive(dto.getIsActive() == null ? Boolean.TRUE : dto.getIsActive());

        repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public CatManufacturerDto update(Long id, CatManufacturerDto dto) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("INVALID_REQUEST"));
        }
        if (dto == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("INVALID_REQUEST"));
        }
        if (StringUtils.isBlank(dto.getManufacturerCode()) || StringUtils.isBlank(dto.getManufacturerName())) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("VALIDATION_ERROR"));
        }

        CatManufacturer entity = repository.findByManufacturerIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("catManufacturer.notFound", id)));

        validateDuplicateForUpdate(id, dto);

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        mapper.updateEntityFromDto(dto, entity);
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(now);

        // MUST NOT change createdBy/createdAt
        repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public void delete(List<Long> manufacturerIds) throws CustomException {
        if (manufacturerIds == null || manufacturerIds.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("INVALID_REQUEST"));
        }

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        for (Long id : manufacturerIds) {
            if (id == null) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("INVALID_REQUEST"));
            }
            CatManufacturer entity = repository.findByManufacturerIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("catManufacturer.notFound", id)));
            entity.setIsDeleted(Boolean.TRUE);
            entity.setUpdatedBy(userId);
            entity.setUpdatedAt(now);
            repository.save(entity);
        }
    }

    @Override
    public CatManufacturerDto getDetail(Long id) throws CustomException {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), msg("INVALID_REQUEST"));
        }
        CatManufacturer entity = repository.findByManufacturerIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), msg("catManufacturer.notFound", id)));
        return mapper.toDto(entity);
    }

    @Override
    public Page<CatManufacturerDto> search(CatManufacturerSearchRequestDto request) throws CustomException {
        if (request == null) {
            request = CatManufacturerSearchRequestDto.builder().build();
        }

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        String sortBy = StringUtils.defaultIfBlank(request.getSortBy(), "createdAt");
        String sortProperty = ALLOWED_SORT_FIELDS.get(sortBy);
        if (sortProperty == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "sortBy không hợp lệ. Các giá trị được phép: " + ALLOWED_SORT_FIELDS.keySet());
        }

        String sortDirectionRaw = StringUtils.defaultIfBlank(request.getSortDirection(), "DESC");
        Sort.Direction direction;
        if ("ASC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("DESC".equalsIgnoreCase(sortDirectionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "sortDirection không hợp lệ. Chỉ chấp nhận ASC hoặc DESC");
        }

        var specification = buildSpecification(request);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        Page<CatManufacturer> resultPage = repository.findAll(specification, pageRequest);
        List<CatManufacturerDto> dtoList = resultPage.getContent().stream().map(mapper::toDto).toList();

        return new PageImpl<>(dtoList, pageRequest, resultPage.getTotalElements());
    }
    @Override
    public List<CatManufacturerDto> getAll(String sortBy, String sortDir) {
        String sortField = (sortBy == null || sortBy.trim().isEmpty()) ? "updatedAt" : sortBy.trim();
        String direction = (sortDir == null || sortDir.trim().isEmpty()) ? "desc" : sortDir.trim();

        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        List<CatManufacturer> entities = catManufacturerRepository.findAllByIsDeletedFalse(sort);
        List<CatManufacturerDto> dtos = entities.stream().map(catManufacturerMapper::toDto).toList();
        enrichCreatedUpdatedUsers(entities, dtos);
        return dtos;
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

    private void enrichCreatedUpdatedUsers(List<CatManufacturer> entities, List<CatManufacturerDto> dtos) {
        if (entities == null || entities.isEmpty() || dtos == null || dtos.isEmpty()) return;

        // lấy danh sách userId cần dùng (tránh N+1)
        Set<Long> userIds = new HashSet<>();
        for (CatManufacturer e : entities) {
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
            CatManufacturer e = entities.get(i);
            CatManufacturerDto dto = dtos.get(i);

            if (e.getCreatedBy() != null && userMap.containsKey(e.getCreatedBy())) {
                dto.setCreatedByUser(mapUserDto(userMap.get(e.getCreatedBy()), orgMap));
            }
            if (e.getUpdatedBy() != null && userMap.containsKey(e.getUpdatedBy())) {
                dto.setUpdatedByUser(mapUserDto(userMap.get(e.getUpdatedBy()), orgMap));
            }
        }
    }

}
