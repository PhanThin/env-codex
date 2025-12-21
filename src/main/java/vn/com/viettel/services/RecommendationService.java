package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.RecommendationDto;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.utils.exceptions.CustomException;

public interface RecommendationService {
    RecommendationDto createRecommendation(RecommendationDto dto);

    Page<RecommendationDto> searchRecommendations(RecommendationSearchRequestDto request) throws CustomException;
}
