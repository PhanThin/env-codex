package vn.com.viettel.services.impl;

import vn.com.viettel.dto.SysOrgDto;
import vn.com.viettel.entities.SysOrg;
import vn.com.viettel.mapper.SysOrgMapper;
import vn.com.viettel.repositories.jpa.SysOrgRepository;
import vn.com.viettel.services.SysOrgService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SysOrgServiceImpl implements SysOrgService {

    private static final Boolean NOT_DELETED = false;
    private static final Boolean DELETED = true;
    private static final Boolean ACTIVE = true;
     private static final Boolean DEACTIVE = false;

    private final SysOrgRepository repository;
    private final SysOrgMapper mapper;
    private final Translator translator;

    public SysOrgServiceImpl(SysOrgRepository repository, SysOrgMapper mapper, Translator translator) {
        this.repository = repository;
        this.mapper = mapper;
        this.translator = translator;
    }

    @Override
    public SysOrgDto create(SysOrgDto dto) {
        validateRequired(dto);

        String orgCode = dto.getOrgCode().trim();
        if (repository.existsByOrgCodeIgnoreCaseAndIsDeletedFalse(orgCode)) {
            throw new CustomException(
                    HttpStatus.CONFLICT.value(),
                    translator.getMessage("org.code.duplicate", orgCode)
            );
        }

        SysOrg entity = mapper.toEntity(dto);
        entity.setOrgCode(orgCode);
        entity.setOrgName(dto.getOrgName().trim());

        entity.setIsDeleted(NOT_DELETED);
        entity.setIsActive(
                dto.getIsActive() == null ? ACTIVE : dto.getIsActive()
        );


        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy());

        SysOrg saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public SysOrgDto update(Long orgId, SysOrgDto dto) {
        if (orgId == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("org.id.required")
            );
        }
        validateRequired(dto);

        SysOrg entity = repository.findByIdAndIsDeletedFalse(orgId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("org.notfound", orgId)
                ));

        String newOrgCode = dto.getOrgCode().trim();
        String oldOrgCode = entity.getOrgCode();
        boolean codeChanged = oldOrgCode == null || !oldOrgCode.equalsIgnoreCase(newOrgCode);

        if (codeChanged && repository.existsByOrgCodeIgnoreCaseAndIsDeletedFalse(newOrgCode)) {
            throw new CustomException(
                    HttpStatus.CONFLICT.value(),
                    translator.getMessage("org.code.duplicate", newOrgCode)
            );
        }

        entity.setOrgCode(newOrgCode);
        entity.setOrgName(dto.getOrgName().trim());
        entity.setIsActive(
                dto.getIsActive() == null ? entity.getIsActive() : dto.getIsActive()
        );


        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy());

        SysOrg saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SysOrgDto getById(Long orgId) {
        if (orgId == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("org.id.required")
            );
        }

        SysOrg entity = repository.findByIdAndIsDeletedFalse(orgId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("org.notfound", orgId)
                ));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysOrgDto> getAll() {
        return repository.findAllByIsDeletedFalse()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long orgId) {
        if (orgId == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    ("org.id.required")
            );
        }

        SysOrg entity = repository.findByIdAndIsDeletedFalse(orgId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("org.notfound", orgId)
                ));

        entity.setIsDeleted(DELETED);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateRequired(SysOrgDto dto) {
        if (dto == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("org.request.null")
            );
        }
        if (isBlank(dto.getOrgCode())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("org.code.required")
            );
        }
        if (isBlank(dto.getOrgName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("org.name.required")
            );
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
