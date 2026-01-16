package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.services.CatProjectPhaseService;

import java.util.List;

@Tag(name = "04. Quản lý giai đoạn của dự án", description = "API quản lý giai đoạn của dự án")
@RestController
@RequestMapping("/api/v1/projects-phases")
@RequiredArgsConstructor
public class CatProjectPhaseController {

    private final CatProjectPhaseService service;

    @Hidden
    @Operation(summary = "Create project phase")
    @PostMapping("/{projectId}")
    public ResponseEntity<CatProjectPhaseDto> create(
            @PathVariable Long projectId,
            @Valid @RequestBody CatProjectPhaseDto request) {
        return ResponseEntity.ok(service.create(projectId, request));
    }

    @Hidden
    @Operation(summary = "Update project phase")
    @PutMapping("/{projectId}/{phaseId}")
    public ResponseEntity<CatProjectPhaseDto> update(
            @PathVariable Long projectId,
            @PathVariable Long phaseId,
            @Valid @RequestBody CatProjectPhaseDto request) {
        return ResponseEntity.ok(service.update(projectId, phaseId, request));
    }

    @Hidden
    @Operation(summary = "Get project phase by id")
    @GetMapping("/{projectId}/{phaseId}")
    public ResponseEntity<CatProjectPhaseDto> getById(
            @PathVariable Long projectId,
            @PathVariable Long phaseId) {
        return ResponseEntity.ok(service.getById(projectId, phaseId));
    }

    @Operation(summary = "Lấy dự giai đoạn dự án theo dự án")
    @GetMapping("/{projectId}")
    public ResponseEntity<List<CatProjectPhaseDto>> getAll(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getAll(projectId));
    }

    @Hidden
    @Operation(summary = "Soft delete project phase")
    @DeleteMapping("/{projectId}/{phaseId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long phaseId) {
        service.delete(projectId, phaseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lấy dự giai đoạn dự án theo loại dự án")
    @GetMapping("/type/{projectTypeId}")
    public ResponseEntity<List<CatProjectPhaseDto>> getAllByProjectType(@PathVariable Long projectTypeId) {
        return ResponseEntity.ok(service.getAllByProjectType(projectTypeId));
    }
}
