package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.viettel.entities.CatProjectPhase;

import java.util.List;
import java.util.Optional;

public interface CatProjectPhaseRepository extends JpaRepository<CatProjectPhase, Long> {
    List<CatProjectPhase> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    Optional<CatProjectPhase> findByIdAndIsDeletedFalse(Long phaseId);

    List<CatProjectPhase> findAllByProjectIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long projectId);

    boolean existsByProjectIdAndPhaseCodeAndIsDeletedFalse(Long projectId, String phaseCode);

    @Query(value = "SELECT pp.* FROM CAT_PROJECT_PHASE pp INNER JOIN PROJECT p ON pp.PROJECT_ID = p.PROJECT_ID WHERE p.PROJECT_TYPE_ID = :projectTypeId AND p.IS_DELETED = 'N' AND pp.IS_DELETED = 'N' AND pp.IS_ACTIVE = 'Y' AND p.IS_ACTIVE = 'Y'", nativeQuery = true)
    List<CatProjectPhase> findAllByProjectTypeId(@Param("projectTypeId") Long projectTypeId);
}
