package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.viettel.entities.CategoryWorkItem;
import vn.com.viettel.entities.WorkItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, Long>, JpaSpecificationExecutor<WorkItem> {
    List<WorkItem> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    @Query(value = "SELECT DISTINCT w.* FROM WORK_ITEM w INNER JOIN RECOMMENDATION_WORK_ITEM rwi ON w.WORK_ITEM_ID = rwi.WORK_ITEM_ID WHERE rwi.RECOMMENDATION_ID IN :recommendationIds AND w.IS_DELETED = 'N'", nativeQuery = true)
    List<WorkItem> findAllByRecommendationIdInAndIsDeletedFalse(@Param("recommendationIds") List<Long> recommendationIds);

    Optional<WorkItem> findByIdAndIsDeletedFalse(Long id);

    List<WorkItem> findAllByItemIdAndIsDeletedFalse(Long itemId);

    boolean existsByItemIdAndWorkItemNameAndIsDeletedFalse(Long itemId, String workItemName);

    // Kiểm tra còn work item đang còn hiệu lực trong 1 hạng mục công việc
    boolean existsByCatWorkItemIdAndIsDeletedFalseAndIsActiveTrue(Long catWorkItemId);

    Optional<WorkItem>
    findFirstByProjectTypeIdAndProjectPhaseIdAndItemIdAndCatWorkItemIdAndWorkItemCodeIgnoreCaseAndIsDeletedFalse(
            Long projectTypeId,
            Long projectPhaseId,
            Long projectItemId,
            Long catWorkItemId,
            String workItemCode
    );

    Optional<WorkItem>
    findFirstByProjectTypeIdAndProjectPhaseIdAndItemIdAndCatWorkItemIdAndWorkItemNameIgnoreCaseAndIsDeletedFalse(
            Long projectTypeId,
            Long projectPhaseId,
            Long projectItemId,
            Long catWorkItemId,
            String workItemName
    );

    List<Long> id(Long id);
}
