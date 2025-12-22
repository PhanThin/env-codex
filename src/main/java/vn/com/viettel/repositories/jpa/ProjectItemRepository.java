package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.ProjectItem;

import java.util.List;
import java.util.Optional;

public interface ProjectItemRepository extends JpaRepository<ProjectItem, Long> {
    List<ProjectItem> findAllByIdInAndIsDeletedFalse(List<Long> ids);
    boolean existsByIdAndIsDeletedFalse(Long id);
    Optional<ProjectItem> findByIdAndIsDeletedFalse(Long itemId);

    List<ProjectItem> findAllByProjectIdAndIsDeletedFalse(Long projectId);

    boolean existsByProjectIdAndItemCodeAndIsDeletedFalse(Long projectId, String itemCode);

}
