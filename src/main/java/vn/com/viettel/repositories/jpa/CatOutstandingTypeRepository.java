package vn.com.viettel.repositories.jpa;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.viettel.entities.CatOutstandingType;

@Repository
public interface CatOutstandingTypeRepository extends JpaRepository<CatOutstandingType, Long> {
    Optional<CatOutstandingType> findByIdAndIsDeletedFalse(Long id);

    List<CatOutstandingType> findAllByIsDeletedFalse();
}
