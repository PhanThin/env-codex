package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.RecommendationDto;
import vn.com.viettel.dto.RecommendationSearchRequestDto;
import vn.com.viettel.utils.exceptions.CustomException;

public interface RecommendationService {
    RecommendationDto createRecommendation(RecommendationDto dto, MultipartFile[] files);

    Page<RecommendationDto> searchRecommendations(RecommendationSearchRequestDto request) throws CustomException;
}
