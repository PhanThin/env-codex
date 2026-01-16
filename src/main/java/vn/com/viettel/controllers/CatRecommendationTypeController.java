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
    @Operation(summary = "Tìm kiếm loại kiến nghị (phân trang + lọc)")
    public ResponseEntity<Page<CatRecommendationTypeDto>> search(@RequestBody RecommendationTypeSearchRequestDto request) {
        return ResponseEntity.ok(service.search(request));
    }

    @Operation(summary = "Tạo loại kiến nghị")
    @PostMapping
    public ResponseEntity<CatRecommendationTypeDto> create(@Valid @RequestBody CatRecommendationTypeDto request) {
        return ResponseEntity.ok(service.create(request));
    }

    @Operation(summary = "Cập nhật loại kiến nghị theo id")
    @PutMapping("/{id}")
    public ResponseEntity<CatRecommendationTypeDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CatRecommendationTypeDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Lấy loại kiến nghị theo id")
    @GetMapping("/{id}")
    public ResponseEntity<CatRecommendationTypeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Lấy tất cả loại kiến nghị")
    @GetMapping
    public ResponseEntity<List<CatRecommendationTypeDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Xoá loại kiến nghị theo id")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody List<Long> ids) {
        service.delete(ids);
        return ResponseEntity.noContent().build();
    }

}
