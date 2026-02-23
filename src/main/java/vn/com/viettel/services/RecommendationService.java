package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.RecommendationDto;
import vn.com.viettel.dto.RecommendationResponseDto;
import vn.com.viettel.dto.RecommendationSearchRequestDto;

import java.util.List;

public interface RecommendationService {
    RecommendationDto create(RecommendationDto dto, MultipartFile[] files);

    RecommendationDto update(Long id, RecommendationDto dto, MultipartFile[] files);

    void delete(List<Long> ids);

    RecommendationDto getById(Long id);

    Page<RecommendationDto> search(RecommendationSearchRequestDto request);

    RecommendationDto close(Long id);

    RecommendationDto accept(Long id);

    RecommendationDto reject(Long id);

    RecommendationResponseDto addResponse(Long id, RecommendationResponseDto dto, MultipartFile[] files);

    List<RecommendationResponseDto> getResponses(Long id);
}
