package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.com.viettel.entities.Recommendation;

import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long>, JpaSpecificationExecutor<Recommendation> {

    @Query("SELECT r FROM Recommendation r WHERE r.recommendationTitle = :recommendationTitle")
    Optional<Recommendation> findByRecommendationTitle(String recommendationTitle);
}
