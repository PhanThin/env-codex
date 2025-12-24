package vn.com.viettel.services;

import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.entities.Attachment;
import vn.com.viettel.minio.dto.ObjectFileDTO;

import java.util.List;

public interface AttachmentService {
    AttachmentDto uploadAttachment(Long referenceId, String referenceType, MultipartFile file);

    List<AttachmentDto> getAttachments(Long referenceId, String referenceType);

    AttachmentDto getAttachmentDetail(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    byte[] downloadAttachment(Long attachmentId);

    String getFileName(Long attachmentId);

    List<Attachment> handleAttachment(MultipartFile[] files, Long entityId, String referenceType, String channel);

    void deleteAttachments(List<Long> referenceId, String referenceType, Long userId);

    void deleteAttachmentsById(List<Long> attachmentIds, Long userId);
}
