package vn.com.viettel.controllers;


import java.util.List;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.OutstandingItemDto;
import vn.com.viettel.dto.OutstandingProcessLogDto;
import vn.com.viettel.services.OutstandingProcessLogService;

/**
 * REST controller for OUTSTANDING_PROCESS_LOG under an outstandingId.
 */
@RestController
@Tag(name = "12. Xử lý tồn tại", description = "API xử lý tồn tại")
@RequestMapping("/api/v1/outstandings-process-logs/{outstandingId}")
@RequiredArgsConstructor
public class OutstandingProcessLogController {

    private final OutstandingProcessLogService service;

    @Operation(summary = "Create outstanding process log")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutstandingProcessLogDto> create(
            @PathVariable Long outstandingId,
            @RequestPart(value = "dto") OutstandingProcessLogDto request,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        return ResponseEntity.ok(service.create(outstandingId, request, files));
    }

    @Operation(summary = "Update outstanding process log")
    @PutMapping(path = "/{processId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutstandingProcessLogDto> update(
            @PathVariable Long outstandingId,
            @PathVariable Long processId,
            @RequestPart(value = "dto") OutstandingProcessLogDto request,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        return ResponseEntity.ok(service.update(outstandingId, processId, request, files));
    }

    @Hidden
    @Operation(summary = "Get outstanding process log by id")
    @GetMapping("/{processId}")
    public ResponseEntity<OutstandingProcessLogDto> getById(
            @PathVariable Long outstandingId,
            @PathVariable Long processId) {
        return ResponseEntity.ok(service.getById(outstandingId, processId));
    }

    @Operation(summary = "Get all outstanding process logs")
    @GetMapping
    public ResponseEntity<List<OutstandingProcessLogDto>> getAll(@PathVariable Long outstandingId) {
        return ResponseEntity.ok(service.getAll(outstandingId));
    }

    @Hidden
    @Operation(summary = "Soft delete outstanding process log")
    @DeleteMapping("/{processId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long outstandingId,
            @PathVariable Long processId) {
        service.delete(outstandingId, processId);
        return ResponseEntity.noContent().build();
    }
}
