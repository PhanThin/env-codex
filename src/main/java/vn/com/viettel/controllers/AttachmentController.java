package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.AttachmentDto;
import vn.com.viettel.services.AttachmentService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "00. Tệp đính kèm", description = "Các API quản lý tệp đính kèm chung (Attachment)")
@RestController
@RequestMapping("/api/v1/attachments")
@Slf4j
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Operation(summary = "Upload tệp đính kèm", description = "Upload file và lưu metadata gắn với một đối tượng (Reference)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload thành công",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AttachmentDto.class))}),
            @ApiResponse(responseCode = "400", description = "File hoặc tham số không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống hoặc lỗi lưu trữ MinIO")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDto> uploadAttachment(
            @Parameter(description = "File cần upload", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID tham chiếu (Optional)") @RequestParam(value = "referenceId", required = false) Long referenceId,
            @Parameter(description = "Loại tham chiếu (Optional)") @RequestParam(value = "referenceType", required = false) String referenceType) {
        AttachmentDto result = attachmentService.uploadAttachment(referenceId, referenceType, file);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Lấy danh sách tệp đính kèm", description = "Lấy danh sách file theo Reference ID và Reference Type")
    @GetMapping
    public ResponseEntity<List<AttachmentDto>> getAttachments(
            @Parameter(description = "ID tham chiếu") @RequestParam("referenceId") Long referenceId,
            @Parameter(description = "Loại tham chiếu") @RequestParam("referenceType") String referenceType) {

        List<AttachmentDto> result = attachmentService.getAttachments(referenceId, referenceType);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Xem chi tiết tệp đính kèm", description = "Lấy thông tin metadata của một file cụ thể")
    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentDto> getAttachmentDetail(
            @Parameter(description = "ID của tệp đính kèm") @PathVariable Long attachmentId) {

        return ResponseEntity.status(HttpStatus.OK).body(attachmentService.getAttachmentDetail(attachmentId));
    }

    @Operation(summary = "Xóa tệp đính kèm", description = "Xóa mềm (Soft delete) tệp đính kèm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tệp đính kèm")
    })
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(
            @Parameter(description = "ID của tệp đính kèm cần xóa") @PathVariable Long attachmentId) {

        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Tải xuống tệp đính kèm", description = "Download file vật lý từ hệ thống lưu trữ")
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<ByteArrayResource> downloadAttachment(
            @Parameter(description = "ID của tệp đính kèm") @PathVariable Long attachmentId) {

        byte[] data = attachmentService.downloadAttachment(attachmentId);
        String fileName = attachmentService.getFileName(attachmentId);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentLength(data.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}