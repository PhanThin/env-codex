package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.RecommendationWorkItem;

import java.util.List;

public interface RecommendationWorkItemRepository extends JpaRepository<RecommendationWorkItem, Long> {
    List<RecommendationWorkItem> findAllByRecommendationIdInAndIsDeletedFalse(List<Long> recommendationIds);
}
