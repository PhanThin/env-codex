package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.WorkItemDto;
import vn.com.viettel.dto.WorkItemSearchRequest;
import vn.com.viettel.services.WorkItemService;

import java.util.List;

@RestController
@Tag(name = "06. Quản lý công việc", description = "API quản lý công việc")
@RequestMapping("/api/v1/work-items")
@RequiredArgsConstructor
public class WorkItemController {

    private final WorkItemService service;

    @Operation(summary = "Tạo mới công việc")
    @PostMapping
    public ResponseEntity<WorkItemDto> create(
            @Valid @RequestBody WorkItemDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(service.create(request));
    }

    @Operation(summary = "Cập nhật công việc")
    @PutMapping("/{id}")
    public ResponseEntity<WorkItemDto> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkItemDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Lấy thông tin chi tiết một công việc")
    @GetMapping("/{id}")
    public ResponseEntity<WorkItemDto> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Lấy danh sách công việc theo hạng mục dự án")
    @GetMapping(path = "/projectItem/{itemId}")
    public ResponseEntity<List<WorkItemDto>> getAllByItemId(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.getAllByItemId(itemId));
    }

    @Operation(summary = "Xoá danh sách công việc")
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestBody List<Long> ids) {
        service.delete(ids);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Tìm kiếm công việc")
    @PostMapping("/search")
    public ResponseEntity<Page<WorkItemDto>> search(@RequestBody WorkItemSearchRequest request) {
        return ResponseEntity.ok(service.search(request));
    }
}
