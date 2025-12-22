package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.CatOutstandingType;

import java.util.List;

public interface CatOutstandingTypeRepository extends JpaRepository<CatOutstandingType, Long> {
    List<CatOutstandingType> findAllByIdInAndIsDeletedFalse(List<Long> ids);
}
