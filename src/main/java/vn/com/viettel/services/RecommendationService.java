package vn.com.viettel.services;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.RecommendationDto;
import vn.com.viettel.dto.RecommendationResponseDto;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.utils.exceptions.CustomException;

import java.util.List;

public interface RecommendationService {
    RecommendationDto createRecommendation(RecommendationDto dto, MultipartFile[] files);

    @Transactional
    RecommendationDto updateRecommendation(Long id, RecommendationDto dto, MultipartFile[] files) throws CustomException;

    @Transactional
    void deleteRecommendations(List<Long> id) throws CustomException;

    Page<RecommendationDto> searchRecommendations(RecommendationSearchRequestDto request) throws CustomException;

    RecommendationDto getRecommendationById(Long id);

    @Transactional
    RecommendationDto closeRecommendation(Long id);

    @Transactional
    RecommendationResponseDto addRecommendationResponse(Long recommendationId, RecommendationResponseDto dto, MultipartFile[] files);

    List<RecommendationResponseDto> getRecommendationResponses(Long recommendationId);
}
