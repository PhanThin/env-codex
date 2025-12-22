package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.RecommendationAssignment;

import java.util.List;

public interface RecommendationAssignmentRepository extends JpaRepository<RecommendationAssignment, Long> {
    List<RecommendationAssignment> findAllByRecommendationIdAndIsDeletedFalse(Long recommendationId);

    List<RecommendationAssignment> findAllByRecommendationIdInAndIsDeletedFalse(List<Long> recommendationIds);
}
