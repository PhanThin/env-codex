package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.com.viettel.entities.CategoryWorkItem;

import java.util.List;
import java.util.Optional;

public interface CategoryWorkItemRepository extends JpaRepository<CategoryWorkItem, Long>,
        JpaSpecificationExecutor<CategoryWorkItem> {

    Optional<CategoryWorkItem> findByIdAndIsDeletedFalse(Long id);

    Optional<CategoryWorkItem>
    findFirstByProjectTypeIdAndProjectPhaseIdAndProjectItemIdAndCategoryWorkItemCodeIgnoreCaseAndIsDeletedFalse(
            Long projectTypeId,
            Long projectPhaseId,
            Long projectItemId,
            String categoryWorkItemCode
    );

    Optional<CategoryWorkItem>
    findFirstByProjectTypeIdAndProjectPhaseIdAndProjectItemIdAndCategoryWorkItemNameIgnoreCaseAndIsDeletedFalse(
            Long projectTypeId,
            Long projectPhaseId,
            Long projectItemId,
            String categoryWorkItemName
    );

    List<CategoryWorkItem> findAllByIdInAndIsDeletedFalse(List<Long> ids);
}
