package vn.com.viettel.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.ProjectItemDto;
import vn.com.viettel.services.ProjectItemService;

/**
 * REST controller for PROJECT_ITEM CRUD APIs under a Project.
 */
@RestController
@Tag(name = "05. Quản lý hạng mục của dự án", description = "API quản lý hạng mục của dự án")
@RequestMapping("/api/v1/projects-items")
@RequiredArgsConstructor
public class ProjectItemController {

    private final ProjectItemService service;

    @Hidden
    @Operation(summary = "Create project item under a project")
    @PostMapping("/{projectId}")
    public ResponseEntity<ProjectItemDto> create(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectItemDto request) {
        return ResponseEntity.ok(service.create(projectId, request));
    }

    @Hidden
    @Operation(summary = "Update project item under a project")
    @PutMapping("/{projectId}/{itemId}")
    public ResponseEntity<ProjectItemDto> update(
            @PathVariable Long projectId,
            @PathVariable Long itemId,
            @Valid @RequestBody ProjectItemDto request) {
        return ResponseEntity.ok(service.update(projectId, itemId, request));
    }

    @Hidden
    @Operation(summary = "Get project item by id under a project")
    @GetMapping("/{projectId}/{itemId}")
    public ResponseEntity<ProjectItemDto> getById(
            @PathVariable Long projectId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(service.getById(projectId, itemId));
    }

    @Operation(summary = "Lấy danh sách hạng mục dự án theo dự án")
    @GetMapping("/{projectId}")
    public ResponseEntity<List<ProjectItemDto>> getAll(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getAll(projectId));
    }

    @Operation(summary = "Lấy danh sách hạng mục dự án theo giai đoạn dự án")
    @GetMapping("/{phaseId}")
    public ResponseEntity<List<ProjectItemDto>> getAllByPhase(@PathVariable Long phaseId) {
        return ResponseEntity.ok(service.getAllByPhaseId(phaseId));
    }

    @Hidden
    @Operation(summary = "Soft delete project item under a project")
    @DeleteMapping("/{projectId}/{itemId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long itemId) {
        service.delete(projectId, itemId);
        return ResponseEntity.noContent().build();
    }
}
