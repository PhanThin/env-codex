package vn.com.viettel.services.impl;

import vn.com.viettel.dto.SysUserDto;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.SysUserMapper;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.SysUserService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lưu ý:
 * - Không validate SYS_ORG bằng cách query SYS_ORG (theo rule).
 * - Mọi query đều lọc isDeleted = 'N'.
 * - Không throw RuntimeException/ResponseStatusException.
 */
@Service
@Transactional
public class SysUserServiceImpl implements SysUserService {

    private static final Boolean NOT_DELETED = false;
    private static final Boolean DELETED = true;
    private static final Boolean ACTIVE = true;
    private static final Boolean DEACTIVE = false;
    private final SysUserRepository repository;
    private final SysUserMapper mapper;
    private Translator translator;
    public SysUserServiceImpl(SysUserRepository repository, SysUserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SysUserDto create(SysUserDto dto) {
        validateRequired(dto);

        String username = dto.getUsername().trim();
        if (repository.existsByUsernameIgnoreCaseAndIsDeletedFalse(username)) {
            throw new CustomException(
                    HttpStatus.CONFLICT.value(),
                    translator.getMessage("user.username.duplicate", username)
            );
        }

        SysUser entity = mapper.toEntity(dto);
        entity.setUsername(username);
        entity.setFullName(dto.getFullName().trim());

        // Soft delete flags
        entity.setIsDeleted(NOT_DELETED);
        entity.setIsActive(dto.getIsActive() == null || !dto.getIsActive().equalsIgnoreCase("0") ? ACTIVE : DEACTIVE);

        // Audit fields (chưa có auth => lấy từ dto nếu client truyền, hoặc null)
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy());

        SysUser saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public SysUserDto update(Long userId, SysUserDto dto) {
        if (userId == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("user.id.required")
            );
        }
        validateRequired(dto);

        SysUser entity = repository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("user.notfound", userId)
                ));

        String newUsername = dto.getUsername().trim();
        String oldUsername = entity.getUsername();
        boolean usernameChanged = oldUsername == null || !oldUsername.equalsIgnoreCase(newUsername);

        if (usernameChanged && repository.existsByUsernameIgnoreCaseAndIsDeletedFalse(newUsername)) {
            throw new CustomException(
                    HttpStatus.CONFLICT.value(),
                    translator.getMessage("user.username.duplicate", newUsername)
            );
        }

        entity.setUsername(newUsername);
        entity.setFullName(dto.getFullName().trim());
        entity.setTitle(dto.getTitle());
        entity.setOrgId(dto.getOrgId());
        entity.setIsActive(dto.getIsActive() == null || !dto.getIsActive().equalsIgnoreCase("0") ? ACTIVE : DEACTIVE);

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy());

        SysUser saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SysUserDto getById(Long userId) {
        if (userId == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("user.id.required")
            );
        }

        SysUser entity = repository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("user.notfound", userId)
                ));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysUserDto> getAll() {
        return repository.findAllByIsDeletedFalse()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        if (userId == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("user.id.required")
            );
        }

        SysUser entity = repository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        translator.getMessage("user.notfound", userId)
                ));

        entity.setIsDeleted(DELETED);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateRequired(SysUserDto dto) {
        if (dto == null) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("user.request.null")
            );
        }
        if (isBlank(dto.getUsername())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("user.username.required")
            );
        }
        if (isBlank(dto.getFullName())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST.value(),
                    translator.getMessage("user.fullname.required")
            );
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
