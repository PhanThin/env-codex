package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.com.viettel.entities.ProjectType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, Long>, JpaSpecificationExecutor<ProjectType> {

    Optional<ProjectType> findByIdAndIsDeletedFalse(Long id);
    List<ProjectType> findAllByIdInAndIsDeletedFalse(List<Long> ids);
    Optional<ProjectType> findByIdAndIsDeleted(Long id, String isDeleted);

    List<ProjectType> findAllByIdInAndIsDeleted(List<Long> ids, String isDeleted);

    @Query("select count(pt.id) > 0 from ProjectType pt " +
            "where pt.isDeleted = 'N' " +
            "and upper(trim(pt.projectTypeName)) = upper(trim(:name))")
    boolean existsDuplicateName(@Param("name") String name);

    @Query("select count(pt.id) > 0 from ProjectType pt " +
            "where pt.isDeleted = 'N' " +
            "and upper(trim(pt.projectTypeName)) = upper(trim(:name)) " +
            "and pt.id <> :id")
    boolean existsDuplicateNameExcludeId(@Param("name") String name, @Param("id") Long id);
}