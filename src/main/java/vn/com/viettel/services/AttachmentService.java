package vn.com.viettel.services;

import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.AttachmentDto;

import java.util.List;

public interface AttachmentService {
    AttachmentDto uploadAttachment(Long referenceId, String referenceType, MultipartFile file);

    List<AttachmentDto> getAttachments(Long referenceId, String referenceType);

    AttachmentDto getAttachmentDetail(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    byte[] downloadAttachment(Long attachmentId);

    String getFileName(Long attachmentId);
}