package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.CatRecommendationType;

import java.util.List;

public interface CatRecommendationTypeRepository extends JpaRepository<CatRecommendationType, Long> {
    List<CatRecommendationType> findAllByIdInAndIsDeletedFalse(List<Long> ids);

}
