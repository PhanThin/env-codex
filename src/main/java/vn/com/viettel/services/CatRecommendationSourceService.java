package vn.com.viettel.services;

import vn.com.viettel.dto.CatRecommendationSourceDto;

import java.util.List;


/**
 * Service interface for CAT_RECOMMENDATION_SOURCE CRUD operations.
 */
public interface CatRecommendationSourceService {

    CatRecommendationSourceDto create(CatRecommendationSourceDto request);

    CatRecommendationSourceDto update(Long id, CatRecommendationSourceDto request);

    CatRecommendationSourceDto getById(Long id);

    List<CatRecommendationSourceDto> getAll();

    void delete(Long id);
}
