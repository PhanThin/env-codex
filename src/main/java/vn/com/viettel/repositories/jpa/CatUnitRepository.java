package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.com.viettel.entities.CatUnit;

import java.util.List;
import java.util.Optional;

public interface CatUnitRepository extends JpaRepository<CatUnit, Long>, JpaSpecificationExecutor<CatUnit> {

    Optional<CatUnit> findByIdAndIsDeletedFalse(Long id);
    List<CatUnit> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    boolean existsByUnitNameIgnoreCaseAndIsDeletedFalse(String unitName);

    boolean existsByUnitNameIgnoreCaseAndIdNotAndIsDeletedFalse(String unitName, Long id);

    List<CatUnit> findAllByUnitTypeAndIsDeletedFalse(String unitType);
}
