package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.CatProjectPhase;

import java.util.List;
import java.util.Optional;

public interface CatProjectPhaseRepository extends JpaRepository<CatProjectPhase, Long> {
    List<CatProjectPhase>  findAllByIdInAndIsDeletedFalse(List<Long> ids);
    Optional<CatProjectPhase> findByIdAndIsDeletedFalse(Long phaseId);

    List<CatProjectPhase> findAllByProjectIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long projectId);

    boolean existsByProjectIdAndPhaseCodeAndIsDeletedFalse(Long projectId, String phaseCode);
}
