package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.ProjectDto;
import vn.com.viettel.services.ProjectService;

import java.util.List;

/**
 * REST controller for PROJECT CRUD APIs.
 */
@RestController
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Create new project")
    @PostMapping("/create")
    public ResponseEntity<ProjectDto> create(
            @Valid @RequestBody ProjectDto request) {
        return ResponseEntity.ok(projectService.create(request));
    }

    @Operation(summary = "Update project by id")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @Operation(summary = "Get project by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @Operation(summary = "Get all projects")
    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAll() {
        return ResponseEntity.ok(projectService.getAll());
    }

    @Operation(summary = "Delete project by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
