package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.RecommendationDto;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.services.RecommendationService;

@Tag(name = "01. Kiến nghị", description = "Các API cho chức năng kiến nghị")
@RestController
@RequestMapping("/api/v1/recommendation")
@Slf4j
public class RecommendationController {
    @Autowired
    private RecommendationService recommendService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecommendationDto> createRecommendation(@RequestPart(value = "dto") RecommendationDto dto, @RequestPart(value = "files", required = false) MultipartFile[] files) {
        RecommendationDto message = recommendService.createRecommendation(dto, files);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<RecommendationDto>> searchRecommendations(@RequestBody @Validated RecommendationSearchRequestDto request) {
        Page<RecommendationDto> pageResult = recommendService.searchRecommendations(request);
        return ResponseEntity.ok(pageResult);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{id}")
    public ResponseEntity<RecommendationDto> updateRecommendation(@PathVariable Long id, @RequestPart(value = "dto") RecommendationDto dto, @RequestPart(value = "files", required = false) MultipartFile[] files) {
        RecommendationDto message = recommendService.updateRecommendation(id, dto, files);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteRecommendation(@PathVariable Long id) {
        recommendService.deleteRecommendation(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<RecommendationDto> getRecommendation(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(recommendService.getRecommendationById(id));
    }
}
