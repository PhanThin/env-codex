package vn.com.viettel.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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


    @PostMapping
    public ResponseEntity<RecommendationDto> createRecommendation(@RequestBody RecommendationDto dto) {
        RecommendationDto message = recommendService.createRecommendation(dto);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<RecommendationDto>> searchRecommendations(
            @RequestBody @Validated RecommendationSearchRequestDto request) {
        Page<RecommendationDto> pageResult = recommendService.searchRecommendations(request);
        return ResponseEntity.ok(pageResult);
    }
}
