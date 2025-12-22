package vn.com.viettel.controllers;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatProjectPhaseDto;
import vn.com.viettel.services.CatProjectPhaseService;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/phases")
@RequiredArgsConstructor
public class CatProjectPhaseController {

    private final CatProjectPhaseService service;

    @Operation(summary = "Create project phase")
    @PostMapping
    public ResponseEntity<CatProjectPhaseDto> create(
            @PathVariable Long projectId,
            @Valid @RequestBody CatProjectPhaseDto request) {
        return ResponseEntity.ok(service.create(projectId, request));
    }

    @Operation(summary = "Update project phase")
    @PutMapping("/{phaseId}")
    public ResponseEntity<CatProjectPhaseDto> update(
            @PathVariable Long projectId,
            @PathVariable Long phaseId,
            @Valid @RequestBody CatProjectPhaseDto request) {
        return ResponseEntity.ok(service.update(projectId, phaseId, request));
    }

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

    @Operation(summary = "Soft delete project phase")
    @DeleteMapping("/{phaseId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long phaseId) {
        service.delete(projectId, phaseId);
        return ResponseEntity.noContent().build();
    }
}
