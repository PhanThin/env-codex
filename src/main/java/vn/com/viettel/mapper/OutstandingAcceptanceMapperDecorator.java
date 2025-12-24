package vn.com.viettel.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.dto.OutstandingAcceptanceDto;
import vn.com.viettel.dto.UserDto;
import vn.com.viettel.entities.Attachment;
import vn.com.viettel.entities.OutstandingAcceptance;
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
public class OutstandingAcceptanceMapperDecorator implements OutstandingAcceptanceMapper {

    /**
     * Mapper gốc do MapStruct generate.
     * Sử dụng @Qualifier("delegate") theo config bạn đặt ở @Mapper (nếu có).
     */
    @Autowired
    @Qualifier("delegate")
    private OutstandingAcceptanceMapper delegate;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private SysUserRepository userRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;

    @Override
    public OutstandingAcceptance toEntity(OutstandingAcceptanceDto dto) {
        return delegate.toEntity(dto);
    }

    @Override
    public void updateEntity(OutstandingAcceptance entity, OutstandingAcceptanceDto dto) {
        delegate.updateEntity(entity, dto);
    }

    @Override
    public OutstandingAcceptanceDto toDto(OutstandingAcceptance entity) {
        return delegate.toDto(entity);
    }

    @Override
    public List<OutstandingAcceptanceDto> toDtoList(List<OutstandingAcceptance> entityList) {
        if (entityList == null) {
            return new java.util.ArrayList<>();
        }
        List<Long> userCreatedIds = entityList.stream().map(OutstandingAcceptance::getAcceptedBy).distinct().filter(Objects::nonNull).toList();
        List<Long> userUpdatedIds = entityList.stream().map(OutstandingAcceptance::getUpdatedBy).distinct().filter(Objects::nonNull).toList();
        List<Long> allUserIds = Stream.concat(userCreatedIds.stream(), userUpdatedIds.stream()).distinct().toList();
        Map<Long, SysUser> sysUserMap = userRepository.findAllById(allUserIds).stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));
        List<Long> logIds = entityList.stream().map(OutstandingAcceptance::getId).distinct().toList();
        Map<Long, List<Attachment>> attachmentMap = attachmentRepository.findAllByReferenceIdInAndReferenceTypeAndIsDeletedFalse(logIds, Constants.OUTSTANDING_ACCEPTANCE_REFERENCE_TYPE).stream().collect(Collectors.groupingBy(Attachment::getReferenceId));
        return entityList.stream().map(entity -> toDto(entity, sysUserMap, attachmentMap)).collect(Collectors.toList());
    }


    public OutstandingAcceptanceDto toDto(OutstandingAcceptance entity, Map<Long, SysUser> sysUserCache, Map<Long, List<Attachment>> attachmentMap) {
        // 1. Map các field đơn giản bằng MapStruct
        OutstandingAcceptanceDto dto = delegate.toDto(entity);
        if (dto == null || entity == null) {
            return dto;
        }

        // 2. Enrich acceptedBy
        if (entity.getAcceptedBy() != null && sysUserCache.containsKey(entity.getAcceptedBy())) {
            SysUser acceptedUser = sysUserCache.get(entity.getAcceptedBy());
            UserDto acceptedDto = modelMapper.map(acceptedUser, UserDto.class);
            dto.setAcceptedByUser(acceptedDto);
        }

        // 3. Enrich updatedBy
        if (entity.getUpdatedBy() != null && sysUserCache.containsKey(entity.getUpdatedBy())) {
            SysUser updatedUser = sysUserCache.get(entity.getUpdatedBy());
            UserDto updatedDto = modelMapper.map(updatedUser, UserDto.class);
            dto.setUpdatedByUser(updatedDto);
        }

        if (entity.getId() != null && attachmentMap.containsKey(entity.getId())) {
            dto.setAttachments(attachmentMap.get(entity.getId()).stream().map(attachment -> modelMapper.map(attachment, AttachmentDto.class)).collect(Collectors.toList()));
        }
        return dto;
    }
}
