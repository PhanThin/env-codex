package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.OutstandingTypeDto;
import vn.com.viettel.dto.OutstandingTypeSearchRequestDto;
import vn.com.viettel.services.CatOutstandingTypeService;

import java.util.List;

/**
 * REST controller for CAT_OUTSTANDING_TYPE CRUD APIs.
 */
@Tag(name = "10. Quản lý loại tồn tại", description = "API quản lý loại tồn tại")
@RestController
@RequestMapping("/api/v1/cat-outstanding-type")
@RequiredArgsConstructor
public class CatOutstandingTypeController {

    private final CatOutstandingTypeService service;

    @Operation(summary = "Tìm kiếm loại tồn tại")
    @PostMapping("/search")
    public ResponseEntity<Page<OutstandingTypeDto>> search(@RequestBody OutstandingTypeSearchRequestDto request) {
        return ResponseEntity.ok(service.search(request));
    }

    @Operation(summary = "Tạo loại tồn tại")
    @PostMapping
    public ResponseEntity<OutstandingTypeDto> create(@Valid @RequestBody OutstandingTypeDto request) {
        return ResponseEntity.ok(service.create(request));
    }

    @Operation(summary = "Cập nhật loại tồn tại theo id")
    @PutMapping("/{id}")
    public ResponseEntity<OutstandingTypeDto> update(
            @PathVariable Long id,
            @Valid @RequestBody OutstandingTypeDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Lấy loại tồn tại theo id")
    @GetMapping("/{id}")
    public ResponseEntity<OutstandingTypeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Lấy tất cả loại tồn tại")
    @GetMapping
    public ResponseEntity<List<OutstandingTypeDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Xoá loại tồn tại theo id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
