package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.OutstandingWorkItem;

import java.util.List;

public interface OutstandingWorkItemRepository extends JpaRepository<OutstandingWorkItem, Long> {
    List<OutstandingWorkItem> findAllByOutstandingIdInAndIsDeletedFalse(List<Long> outstandingIds);
}
