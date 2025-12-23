package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.OutstandingAlertConfig;

import java.util.List;

public interface OutstandingAlertConfigRepository extends JpaRepository<OutstandingAlertConfig, Long> {
    List<OutstandingAlertConfig> findAllByOutstandingIdInAndIsDeletedFalse(List<Long> outstandingIds);

    List<OutstandingAlertConfig> findByOutstandingIdAndIsDeletedFalse(Long outstandingId);
}
