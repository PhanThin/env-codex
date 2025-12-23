package vn.com.viettel.repositories.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.viettel.entities.CatRecommendationSource;
@Repository
public interface CatRecommendationSourceRepository extends JpaRepository<CatRecommendationSource, Long> {
    Optional<CatRecommendationSource> findByIdAndIsDeletedFalse(Long id);

    List<CatRecommendationSource> findAllByIsDeletedFalse();
}
