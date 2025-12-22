package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.viettel.entities.CatRecommendationType;

import java.util.List;
import java.util.Optional;
@Repository
public interface CatRecommendationTypeRepository extends JpaRepository<CatRecommendationType, Long> {
    List<CatRecommendationType> findAllByIdInAndIsDeletedFalse(List<Long> ids);
 Optional<CatRecommendationType> findByIdAndIsDeletedFalse(Long id);

    List<CatRecommendationType> findAllByIsDeletedFalse();

}
