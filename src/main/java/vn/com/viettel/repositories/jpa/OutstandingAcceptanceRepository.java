package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.OutstandingAcceptance;

import java.util.List;
import java.util.Optional;

public interface OutstandingAcceptanceRepository extends JpaRepository<OutstandingAcceptance, Long> {
    List<OutstandingAcceptance> findAllByOutstandingIdAndIsDeletedFalse(Long outstandingId);

    List<OutstandingAcceptance> findAllByOutstandingIdInAndIsDeletedFalse(List<Long> outstandingIds);

    Optional<OutstandingAcceptance> findByIdAndIsDeletedFalse(Long acceptanceId);

    boolean existsByIdAndIsDeletedFalse(Long id);
}
