package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.OutstandingProcessLog;

import java.util.List;
import java.util.Optional;

public interface OutstandingProcessLogRepository extends JpaRepository<OutstandingProcessLog, Long> {
    List<OutstandingProcessLog> findAllByOutstandingIdInAndIsDeletedFalse(List<Long> outstandingIds);

    List<OutstandingProcessLog> findAllByOutstandingIdAndIsDeletedFalse(Long outstandingId);
    Optional<OutstandingProcessLog> findByIdAndIsDeletedFalse(Long processId);

    List<OutstandingProcessLog> findAllByOutstandingIdAndIsDeletedFalseOrderByUpdatedAtDesc(Long outstandingId);
}
