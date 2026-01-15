package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.ProjectType;

import java.util.List;
import java.util.Optional;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, Long> {
    Optional<ProjectType> findByIdAndIsDeletedFalse(Long id);
    List<ProjectType> findAllByIdInAndIsDeletedFalse(List<Long> ids);
}
