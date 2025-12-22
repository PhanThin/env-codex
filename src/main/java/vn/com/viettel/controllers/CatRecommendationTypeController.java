package vn.com.viettel.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatRecommendationTypeDto;
import vn.com.viettel.services.CatRecommendationTypeService;

/**
 * REST controller for CAT_RECOMMENDATION_TYPE CRUD APIs.
 */
@RestController
@RequestMapping("/api/v1/cat-recommendation-type")
@RequiredArgsConstructor
public class CatRecommendationTypeController {

    private final CatRecommendationTypeService service;

    @Operation(summary = "Create CAT_RECOMMENDATION_TYPE")
    @PostMapping
    public ResponseEntity<CatRecommendationTypeDto> create(@Valid @RequestBody CatRecommendationTypeDto request) {
        return ResponseEntity.ok(service.create(request));
    }

    @Operation(summary = "Update CAT_RECOMMENDATION_TYPE by id")
    @PutMapping("/{id}")
    public ResponseEntity<CatRecommendationTypeDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CatRecommendationTypeDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Get CAT_RECOMMENDATION_TYPE by id")
    @GetMapping("/{id}")
    public ResponseEntity<CatRecommendationTypeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Get all CAT_RECOMMENDATION_TYPE")
    @GetMapping
    public ResponseEntity<List<CatRecommendationTypeDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Soft delete CAT_RECOMMENDATION_TYPE by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
