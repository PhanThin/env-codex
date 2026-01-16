package vn.com.viettel.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import vn.com.viettel.dto.CatRecommendationTypeDto;
import vn.com.viettel.dto.RecommendationTypeSearchRequestDto;
import vn.com.viettel.services.CatRecommendationTypeService;

/**
 * REST controller for CAT_RECOMMENDATION_TYPE CRUD APIs.
 */
@Tag(name = "08. Quản lý loại kiến nghị", description = "API quản lý loại kiến nghị")
@RestController
@RequestMapping("/api/v1/cat-recommendation-type")
@RequiredArgsConstructor
public class CatRecommendationTypeController {

    private final CatRecommendationTypeService service;


    @PostMapping("/search")
    @Operation(summary = "Search loại kiến nghị (phân trang + lọc)")
    public ResponseEntity<Page<CatRecommendationTypeDto>> search(@RequestBody RecommendationTypeSearchRequestDto request) {
        return ResponseEntity.ok(service.search(request));
    }
    @Hidden
    @Operation(summary = "Create CAT_RECOMMENDATION_TYPE")
    @PostMapping
    public ResponseEntity<CatRecommendationTypeDto> create(@Valid @RequestBody CatRecommendationTypeDto request) {
        return ResponseEntity.ok(service.create(request));
    }

    @Hidden
    @Operation(summary = "Update CAT_RECOMMENDATION_TYPE by id")
    @PutMapping("/{id}")
    public ResponseEntity<CatRecommendationTypeDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CatRecommendationTypeDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Hidden
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

    @Hidden
    @Operation(summary = "Soft delete CAT_RECOMMENDATION_TYPE by id")
    @DeleteMapping
    public ResponseEntity<Void> deleteMultiple(@RequestBody List<Long> ids) {
        service.delete(ids);
        return ResponseEntity.noContent().build();
    }
}
