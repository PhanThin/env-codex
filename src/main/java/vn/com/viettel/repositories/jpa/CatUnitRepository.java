package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.com.viettel.entities.CatUnit;

import java.util.List;
import java.util.Optional;

public interface CatUnitRepository extends JpaRepository<CatUnit, Long>, JpaSpecificationExecutor<CatUnit> {

    Optional<CatUnit> findByIdAndIsDeleted(Long id, String isDeleted);
    Optional<CatUnit> findByIdAndIsDeletedFalse(Long id);
    List<CatUnit> findAllByIdInAndIsDeletedFalse(List<Long> ids);
    List<CatUnit> findAllByIdInAndIsDeleted(List<Long> ids, String isDeleted);

    boolean existsByUnitNameIgnoreCaseAndIsDeleted(String unitName, String isDeleted);

    boolean existsByUnitNameIgnoreCaseAndIdNotAndIsDeleted(String unitName, Long id, String isDeleted);
}
