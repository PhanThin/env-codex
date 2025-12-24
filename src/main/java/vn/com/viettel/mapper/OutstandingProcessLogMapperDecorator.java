package vn.com.viettel.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.dto.OutstandingProcessLogDto;
import vn.com.viettel.dto.UserDto;
import vn.com.viettel.entities.Attachment;
import vn.com.viettel.entities.OutstandingProcessLog;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.repositories.jpa.AttachmentRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Decorator cho OutstandingProcessLogMapper.
 * Dùng để enrich DTO với thông tin User (createdBy/updatedBy) đầy đủ.
 */
@Component
public class OutstandingProcessLogMapperDecorator implements OutstandingProcessLogMapper {

    /**
     * Mapper gốc do MapStruct generate.
     * Sử dụng @Qualifier("delegate") theo config bạn đặt ở @Mapper (nếu có).
     */
    @Autowired
    @Qualifier("delegate")
    private OutstandingProcessLogMapper delegate;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private SysUserRepository userRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;

    @Override
    public OutstandingProcessLog toEntity(OutstandingProcessLogDto dto) {
        return delegate.toEntity(dto);
    }

    @Override
    public void updateEntity(OutstandingProcessLog entity, OutstandingProcessLogDto dto) {
        delegate.updateEntity(entity, dto);
    }

    @Override
    public OutstandingProcessLogDto toDto(OutstandingProcessLog entity) {
        return delegate.toDto(entity);
    }

    @Override
    public List<OutstandingProcessLogDto> toDtoList(List<OutstandingProcessLog> entityList) {
        if (entityList == null) {
            return new java.util.ArrayList<>();
        }
        List<Long> userCreatedIds = entityList.stream().map(OutstandingProcessLog::getCreatedBy).distinct().filter(Objects::nonNull).toList();
        List<Long> userUpdatedIds = entityList.stream().map(OutstandingProcessLog::getUpdatedBy).distinct().filter(Objects::nonNull).toList();
        List<Long> allUserIds = Stream.concat(userCreatedIds.stream(), userUpdatedIds.stream()).distinct().toList();
        Map<Long, SysUser> sysUserMap = userRepository.findAllById(allUserIds).stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Map<Long, List<Attachment>> attachmentMap;
        if (entityList.size() == 1) {
            attachmentMap = attachmentRepository.findAllByReferenceIdAndReferenceTypeAndIsDeletedFalse(entityList.get(0).getId(), Constants.OUTSTANDING_PROCESS_REFERENCE_TYPE).stream().collect(Collectors.groupingBy(Attachment::getReferenceId));
        } else {
            attachmentMap = new java.util.HashMap<>();
        }
        return entityList.stream().map(entity -> toDto(entity, sysUserMap, attachmentMap)).collect(Collectors.toList());
    }


    public OutstandingProcessLogDto toDto(OutstandingProcessLog entity, Map<Long, SysUser> sysUserCache, Map<Long, List<Attachment>> attachmentMap) {
        // 1. Map các field đơn giản bằng MapStruct
        OutstandingProcessLogDto dto = delegate.toDto(entity);
        if (dto == null || entity == null) {
            return dto;
        }

        // 2. Enrich createdBy
        if (entity.getCreatedBy() != null && sysUserCache.containsKey(entity.getCreatedBy())) {
            SysUser createdUser = sysUserCache.get(entity.getCreatedBy());
            UserDto createdDto = modelMapper.map(createdUser, UserDto.class);
            dto.setCreatedBy(createdDto);
        }

        // 3. Enrich updatedBy
        if (entity.getUpdatedBy() != null && sysUserCache.containsKey(entity.getUpdatedBy())) {
            SysUser updatedUser = sysUserCache.get(entity.getUpdatedBy());
            UserDto updatedDto = modelMapper.map(updatedUser, UserDto.class);
            dto.setUpdatedBy(updatedDto);
        }

        if (entity.getId() != null && attachmentMap.containsKey(entity.getId())) {
            dto.setAttachments(attachmentMap.get(entity.getId()).stream().map(attachment -> modelMapper.map(attachment, AttachmentDto.class)).collect(Collectors.toList()));
        }
        return dto;
    }
}
