package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.CatUnit;

import java.util.List;
import java.util.Optional;

public interface CatUnitRepository extends JpaRepository<CatUnit, Long> {
    Optional<CatUnit> findByIdAndIsDeletedFalse(Long id);

    List<CatUnit> findAllByIdInAndIsDeletedFalse(List<Long> ids);
}
