package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.viettel.entities.WorkItem;

import java.util.List;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    List<WorkItem> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    @Query(value = "SELECT w.* FROM WORK_ITEM w INNER JOIN RECOMMENDATION_WORK_ITEM rwi ON w.WORK_ITEM_ID = rwi.WORK_ITEM_ID WHERE rwi.RECOMMENDATION_ID IN :recommendationIds AND w.IS_DELETED = 'N'", nativeQuery = true)
    List<WorkItem> findAllByRecommendationIdInAndIsDeletedFalse(@Param("recommendationIds") List<Long> recommendationIds);
}
