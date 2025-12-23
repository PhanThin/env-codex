package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.RecommendationResponse;

import java.util.List;

public interface RecommendationResponseRepository extends JpaRepository<RecommendationResponse, Long> {
    List<RecommendationResponse> findAllByRecommendationIdAndIsDeletedFalse(Long recommendationId);
}
