package vn.com.viettel.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatOutstandingTypeDto;
import vn.com.viettel.dto.OutstandingTypeDto;
import vn.com.viettel.services.CatOutstandingTypeService;

/**
 * REST controller for CAT_OUTSTANDING_TYPE CRUD APIs.
 */
@Tag(name = "10. Quản lý loại tồn tại", description = "API quản lý loại tồn tại")
@RestController
@RequestMapping("/api/v1/cat-outstanding-type")
@RequiredArgsConstructor
public class CatOutstandingTypeController {

    private final CatOutstandingTypeService service;
    @Hidden
    @Operation(summary = "Create CAT_OUTSTANDING_TYPE")
    @PostMapping
    public ResponseEntity<OutstandingTypeDto> create(@Valid @RequestBody OutstandingTypeDto request) {
        return ResponseEntity.ok(service.create(request));
    }
    @Hidden
    @Operation(summary = "Update CAT_OUTSTANDING_TYPE by id")
    @PutMapping("/{id}")
    public ResponseEntity<OutstandingTypeDto> update(
            @PathVariable Long id,
            @Valid @RequestBody OutstandingTypeDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }
    @Hidden
    @Operation(summary = "Get CAT_OUTSTANDING_TYPE by id")
    @GetMapping("/{id}")
    public ResponseEntity<OutstandingTypeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get all CAT_OUTSTANDING_TYPE")
    @GetMapping
    public ResponseEntity<List<OutstandingTypeDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Hidden
    @Operation(summary = "Soft delete CAT_OUTSTANDING_TYPE by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
