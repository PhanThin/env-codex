package vn.com.viettel.services;

import org.springframework.data.domain.Page;
import vn.com.viettel.dto.CatRecommendationTypeDto;
import vn.com.viettel.dto.RecommendationTypeSearchRequestDto;

import java.util.List;



/**
 * Service interface for CAT_RECOMMENDATION_TYPE CRUD operations.
 */
public interface CatRecommendationTypeService {

    Page<CatRecommendationTypeDto> search(RecommendationTypeSearchRequestDto request);

    CatRecommendationTypeDto create(CatRecommendationTypeDto request);

    CatRecommendationTypeDto update(Long id, CatRecommendationTypeDto request);

    CatRecommendationTypeDto getById(Long id);

    List<CatRecommendationTypeDto> getAll();

    void delete(Long id);
}
