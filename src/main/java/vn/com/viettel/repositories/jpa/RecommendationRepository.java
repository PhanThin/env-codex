package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.viettel.entities.Recommendation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long>, JpaSpecificationExecutor<Recommendation> {
    Optional<Recommendation> findByIdAndIsDeletedFalse(Long id);

    boolean existsByIdAndIsDeletedFalse(Long id);

    boolean existsByRecommendationTitleIgnoreCaseAndIsDeletedFalse(String recommendationTitle);

    boolean existsByRecommendationTitleIgnoreCaseAndIdNotAndIsDeletedFalse(String recommendationTitle, Long id);

    List<Recommendation> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    @Query("select r.id from Recommendation r where r.id in :ids and r.isDeleted = false")
    List<Long> findExistingIds(@Param("ids") Collection<Long> ids);
}
