package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.viettel.entities.DocInternal;

import java.util.Optional;

@Repository
public interface DocInternalRepositoryJPA extends JpaRepository<DocInternal, Long> {
    boolean existsByCodeAndIsDelete(String code, Long isDelete);

    Optional<DocInternal> findByCodeAndIsDelete(String code, Long isDelete);
}
