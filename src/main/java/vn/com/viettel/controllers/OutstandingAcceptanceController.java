
package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.OutstandingAcceptanceDto;
import vn.com.viettel.services.OutstandingAcceptanceService;

import java.util.List;

/**
 * REST controller for OUTSTANDING_ACCEPTANCE.
 */
@Tag(name = "04. Nghiệm thu tồn tại", description = "Các API kết quả nghiệm thu xử lý tồn tại")
@RestController
@RequestMapping("/api/v1/outstandings-acceptance/{outstandingId}")
@RequiredArgsConstructor
public class OutstandingAcceptanceController {

    private final OutstandingAcceptanceService service;

    @Operation(summary = "Create acceptance for outstanding")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutstandingAcceptanceDto> create(
            @PathVariable Long outstandingId,
            @RequestPart(value = "dto") OutstandingAcceptanceDto request,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        return ResponseEntity.ok(service.create(outstandingId, request, files));
    }

    @Operation(summary = "Update acceptance")
    @PutMapping(path = "/{acceptanceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutstandingAcceptanceDto> update(
            @PathVariable Long outstandingId,
            @PathVariable Long acceptanceId,
            @RequestPart(value = "dto") OutstandingAcceptanceDto request,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        return ResponseEntity.ok(service.update(outstandingId, acceptanceId, request, files));
    }

    @Operation(summary = "Get a acceptance of outstanding")
    @GetMapping(path = "/{acceptanceId}")
    public ResponseEntity<OutstandingAcceptanceDto> get(@PathVariable Long outstandingId, @PathVariable Long acceptanceId) {
        return ResponseEntity.ok(service.get(outstandingId, acceptanceId));
    }


    @Operation(summary = "Get all outstanding acceptance")
    @GetMapping
    public ResponseEntity<List<OutstandingAcceptanceDto>> getAll(@PathVariable Long outstandingId) {
        return ResponseEntity.ok(service.getAll(outstandingId));
    }

    @Operation(summary = "Soft delete acceptance")
    @DeleteMapping("/{acceptanceId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long outstandingId,
            @PathVariable Long acceptanceId) {
        service.delete(outstandingId, acceptanceId);
        return ResponseEntity.noContent().build();
    }
}
