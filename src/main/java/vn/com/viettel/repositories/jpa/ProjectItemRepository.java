package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.ProjectItem;

import java.util.List;

public interface ProjectItemRepository extends JpaRepository<ProjectItem, Long> {
    List<ProjectItem> findAllByIdInAndIsDeletedFalse(List<Long> ids);
}
