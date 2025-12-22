package vn.com.viettel.services;

import vn.com.viettel.dto.CatRecommendationTypeDto;

import java.util.List;



/**
 * Service interface for CAT_RECOMMENDATION_TYPE CRUD operations.
 */
public interface CatRecommendationTypeService {

    CatRecommendationTypeDto create(CatRecommendationTypeDto request);

    CatRecommendationTypeDto update(Long id, CatRecommendationTypeDto request);

    CatRecommendationTypeDto getById(Long id);

    List<CatRecommendationTypeDto> getAll();

    void delete(Long id);
}
