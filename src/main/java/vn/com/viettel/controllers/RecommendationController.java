package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.RecommendationDto;
import vn.com.viettel.dto.RecommendationResponseDto;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.services.RecommendationService;

import java.util.List;

@Tag(name = "12. Quản lý kiến nghị", description = "API quản lý kiến nghị")
@RestController
@RequestMapping("/api/v1/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo mới kiến nghị")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Thành công")})
    public ResponseEntity<RecommendationDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(encoding = @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE))
            )
            @RequestPart("dto") @Valid RecommendationDto dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return ResponseEntity.ok(recommendationService.create(dto, files));
    }

    @PostMapping("/search")
    @Operation(summary = "Tìm kiếm kiến nghị")
    public ResponseEntity<Page<RecommendationDto>> search(@RequestBody RecommendationSearchRequestDto request) {
        return ResponseEntity.ok(recommendationService.search(request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật kiến nghị")
    public ResponseEntity<RecommendationDto> update(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(encoding = @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE))
            )
            @RequestPart("dto") @Valid RecommendationDto dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return ResponseEntity.ok(recommendationService.update(id, dto, files));
    }

    @DeleteMapping
    @Operation(summary = "Xóa mềm danh sách kiến nghị")
    public ResponseEntity<Void> delete(@RequestBody List<Long> ids) {
        recommendationService.delete(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết kiến nghị")
    public ResponseEntity<RecommendationDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.getById(id));
    }

    @GetMapping("/response/{id}")
    @Operation(summary = "Danh sách phản hồi")
    public ResponseEntity<List<RecommendationResponseDto>> getResponses(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.getResponses(id));
    }

    @PutMapping("/close/{id}")
    @Operation(summary = "Đóng kiến nghị")
    public ResponseEntity<RecommendationDto> close(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.close(id));
    }

    @PutMapping(value = "/response/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Thêm phản hồi")
    public ResponseEntity<RecommendationResponseDto> addResponse(
            @PathVariable Long id,
            @RequestPart("dto") RecommendationResponseDto dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return ResponseEntity.ok(recommendationService.addResponse(id, dto, files));
    }

    @PutMapping("/reject/{id}")
    @Operation(summary = "Từ chối kiến nghị")
    public ResponseEntity<RecommendationDto> reject(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.reject(id));
    }

    @PutMapping("/accept/{id}")
    @Operation(summary = "Tiếp nhận kiến nghị")
    public ResponseEntity<RecommendationDto> accept(@PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.accept(id));
    }
}
