package vn.com.viettel.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.services.CatProjectPhaseService;

@Tag(name = "12. Giai đoạn", description = "Các API giai đoạn triển khai của dự án ")
@RestController
@RequestMapping("/api/v1/projects-phases/{projectId}")
@RequiredArgsConstructor
public class CatProjectPhaseController {

    private final CatProjectPhaseService service;
    @Hidden
    @Operation(summary = "Create project phase")
    @PostMapping
    public ResponseEntity<CatProjectPhaseDto> create(
            @PathVariable Long projectId,
            @Valid @RequestBody CatProjectPhaseDto request) {
        return ResponseEntity.ok(service.create(projectId, request));
    }
    @Hidden
    @Operation(summary = "Update project phase")
    @PutMapping("/{phaseId}")
    public ResponseEntity<CatProjectPhaseDto> update(
            @PathVariable Long projectId,
            @PathVariable Long phaseId,
            @Valid @RequestBody CatProjectPhaseDto request) {
        return ResponseEntity.ok(service.update(projectId, phaseId, request));
    }
    @Hidden
    @Operation(summary = "Get project phase by id")
    @GetMapping("/{phaseId}")
    public ResponseEntity<CatProjectPhaseDto> getById(
            @PathVariable Long projectId,
            @PathVariable Long phaseId) {
        return ResponseEntity.ok(service.getById(projectId, phaseId));
    }

    @Operation(summary = "Get all project phases")
    @GetMapping
    public ResponseEntity<List<CatProjectPhaseDto>> getAll(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getAll(projectId));
    }
    @Hidden
    @Operation(summary = "Soft delete project phase")
    @DeleteMapping("/{phaseId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long phaseId) {
        service.delete(projectId, phaseId);
        return ResponseEntity.noContent().build();
    }
}
