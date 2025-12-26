package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    List<Project> findAllByIsDeletedFalse();
    
    boolean existsByIdAndIsDeletedFalse(Long id);

   Optional<Project> findByIdAndIsDeletedFalse(Long projectId);
}
