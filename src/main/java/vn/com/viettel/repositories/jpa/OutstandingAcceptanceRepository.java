package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.OutstandingAcceptance;

import java.util.List;

public interface OutstandingAcceptanceRepository extends JpaRepository<OutstandingAcceptance, Long> {
    List<OutstandingAcceptance> findAllByOutstandingIdAndIsDeletedFalse(Long outstandingId);

    List<OutstandingAcceptance> findAllByOutstandingIdInAndIsDeletedFalse(List<Long> outstandingIds);
}
