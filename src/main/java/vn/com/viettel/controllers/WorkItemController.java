package vn.com.viettel.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.services.WorkItemService;

@RestController
@Tag(name = "08. Công Việc", description = "Các API đơn vị công việc cụ thể trong hạng mục")
@RequestMapping("/api/v1/work-items/{itemId}")
@RequiredArgsConstructor
public class WorkItemController {

    private final WorkItemService service;
    @Hidden
    @Operation(summary = "Create work item")
    @PostMapping
    public ResponseEntity<WorkItemDto> create(
            @PathVariable Long itemId,
            @Valid @RequestBody WorkItemDto request) {
        return ResponseEntity.ok(service.create(itemId, request));
    }

    @Hidden
    @Operation(summary = "Update work item")
    @PutMapping("/{workItemId}")
    public ResponseEntity<WorkItemDto> update(
            @PathVariable Long itemId,
            @PathVariable Long workItemId,
            @Valid @RequestBody WorkItemDto request) {
        return ResponseEntity.ok(service.update(itemId, workItemId, request));
    }

    @Operation(summary = "Get work item by id")
    @GetMapping("/{workItemId}")
    public ResponseEntity<WorkItemDto> getById(
            @PathVariable Long itemId,
            @PathVariable Long workItemId) {
        return ResponseEntity.ok(service.getById(itemId, workItemId));
    }

    @Operation(summary = "Get all work items")
    @GetMapping
    public ResponseEntity<List<WorkItemDto>> getAll(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.getAll(itemId));
    }

    @Hidden
    @Operation(summary = "Soft delete work item")
    @DeleteMapping("/{workItemId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long itemId,
            @PathVariable Long workItemId) {
        service.delete(itemId, workItemId);
        return ResponseEntity.noContent().build();
    }
}
