package vn.com.viettel.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.entities.Attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttachmentMapper {

    @Autowired
    private ModelMapper modelMapper;

    public List<AttachmentDto> mapToDtos(List<Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new ArrayList<>();
        }

        return attachments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AttachmentDto mapToDto(Attachment entity) {
        if (entity == null) {
            return null;
        }

        return modelMapper.map(entity, AttachmentDto.class);
    }
}