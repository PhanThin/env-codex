package vn.com.viettel.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatRecommendationSourceDto;
import vn.com.viettel.services.CatRecommendationSourceService;

/**
 * REST controller for CAT_RECOMMENDATION_SOURCE CRUD APIs.
 */
@Tag(name = "07. Quản lý nguồn kiến nghị", description = "API quản lý nguồn kiến nghị")
@RestController
@RequestMapping("/api/v1/cat-recommendation-source")
@RequiredArgsConstructor
public class CatRecommendationSourceController {

    private final CatRecommendationSourceService service;

    @Hidden
    @Operation(summary = "Create CAT_RECOMMENDATION_SOURCE")
    @PostMapping
    public ResponseEntity<CatRecommendationSourceDto> create(@Valid @RequestBody CatRecommendationSourceDto request) {
        return ResponseEntity.ok(service.create(request));
    }

    @Hidden
    @Operation(summary = "Update CAT_RECOMMENDATION_SOURCE by id")
    @PutMapping("/{id}")
    public ResponseEntity<CatRecommendationSourceDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CatRecommendationSourceDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Hidden
    @Operation(summary = "Get CAT_RECOMMENDATION_SOURCE by id")
    @GetMapping("/{id}")
    public ResponseEntity<CatRecommendationSourceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get all CAT_RECOMMENDATION_SOURCE")
    @GetMapping
    public ResponseEntity<List<CatRecommendationSourceDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Hidden
    @Operation(summary = "Soft delete CAT_RECOMMENDATION_SOURCE by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
