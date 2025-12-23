package vn.com.viettel.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.entities.Attachment;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.mapper.AttachmentMapper;
import vn.com.viettel.minio.dto.ObjectFileDTO;
import vn.com.viettel.minio.services.FileService;
import vn.com.viettel.repositories.jpa.AttachmentRepository;
import vn.com.viettel.repositories.jpa.SysUserRepository;
import vn.com.viettel.services.AttachmentService;
import vn.com.viettel.utils.Constants;
import vn.com.viettel.utils.Translator;
import vn.com.viettel.utils.exceptions.CustomException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final SysUserRepository userRepository;
    private final FileService fileService;
    private final Translator translator;
    private final AttachmentMapper attachmentMapper;

    @Value("${minio.bucketName:evn}")
    private String bucketName;

    private static final String ATTACHMENT_CHANNEL = "attachments";

    @Override
    @Transactional
    public AttachmentDto uploadAttachment(Long referenceId, String referenceType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("attachment.file.required"));
        }
        if (referenceId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("attachment.referenceId.null"));
        }
        if (StringUtils.isBlank(referenceType)) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("attachment.referenceType.required"));
        }

        List<ObjectFileDTO> uploadedFiles = fileService.uploadFiles(bucketName, ATTACHMENT_CHANNEL, new MultipartFile[]{file});

        if (uploadedFiles == null || uploadedFiles.isEmpty()) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), translator.getMessage("attachment.upload.failed"));
        }
        ObjectFileDTO minioFile = uploadedFiles.getFirst();

        SysUser currentUser = getCurrentUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;

        Attachment attachment = new Attachment();
        attachment.setReferenceId(referenceId);
        attachment.setReferenceType(referenceType);
        attachment.setFileName(file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String fileExt = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        attachment.setFileExt(fileExt);
        attachment.setFileSize(file.getSize());
        attachment.setFileUrl(minioFile.getLinkUrlPublic());
        attachment.setFilePath(minioFile.getFilePath());

        attachment.setUploadedAt(LocalDateTime.now());
        attachment.setUploadedBy(currentUserId);
        attachment.setIsDeleted(false);

        Attachment saved = attachmentRepository.save(attachment);
        return attachmentMapper.mapToDto(saved);
    }

    @Override
    public List<AttachmentDto> getAttachments(Long referenceId, String referenceType) {
        if (referenceId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("attachment.referenceId.null"));
        }
        List<Attachment> list = attachmentRepository.findAllByReferenceIdAndReferenceTypeAndIsDeletedFalse(referenceId, referenceType);
        return attachmentMapper.mapToDtos(list);
    }

    @Override
    public AttachmentDto getAttachmentDetail(Long attachmentId) {
        return attachmentMapper.mapToDto(findById(attachmentId));
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = findById(attachmentId);
        SysUser currentUser = getCurrentUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : Constants.DEFAULT_USER_ID;

        attachment.setIsDeleted(true);
        attachment.setUpdatedBy(currentUserId);
        attachment.setUpdatedAt(LocalDateTime.now());

        attachmentRepository.save(attachment);
    }

    @Override
    public byte[] downloadAttachment(Long attachmentId) {
        Attachment attachment = findById(attachmentId);
        try {
            return fileService.getFile(bucketName, attachment.getFilePath());
        } catch (Exception e) {
            log.error("Error downloading file id {}: {}", attachmentId, e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), translator.getMessage("attachment.download.failed"));
        }
    }

    @Override
    public String getFileName(Long attachmentId) {
        return findById(attachmentId).getFileName();
    }

    // --- Helpers ---

    private Attachment findById(Long id) {
        if (id == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), translator.getMessage("attachment.id.null"));
        }
        return attachmentRepository.findById(id)
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), translator.getMessage("attachment.notFound", id)));
    }

    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof SysUser user) {
                return user;
            }
            if (authentication.getPrincipal() instanceof String username) {
                return userRepository.findByUsername(username).orElse(null);
            }
        }
        return null;
    }
}